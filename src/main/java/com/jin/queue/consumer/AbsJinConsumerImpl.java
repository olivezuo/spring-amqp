package com.jin.queue.consumer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin.mail.FailedMessageMailer;
import com.jin.queue.QueueMessage;
import com.jin.queue.config.MQConfig;
import com.jin.queue.service.MessagePersistService;
import com.jin.queue.service.MessageReceiveServiceImpl;
import com.jin.queue.service.MessageSendService;
import com.rabbitmq.client.Channel;

public abstract class AbsJinConsumerImpl extends MessageReceiveServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(AbsJinConsumerImpl.class);
	
	private int messageCount;
	
	protected String retryPrefix ;
	
	@Autowired 
	MQConfig mqConfig;
	
	@Autowired
	MessageSendService messageSender;
	
	@Autowired
	FailedMessageMailer failedMessageMailer;
	
	@Autowired
	MessagePersistService messagePersistService;
	
	protected abstract <T> T getMessageObj(QueueMessage<T> queueMessage);
	
	protected abstract <T> void process(T message);

	@Override
	public void receive(Message message, Channel channel) {
		ObjectMapper objectMapper = new ObjectMapper();
		Object decodedMessage = mqConfig.rabbitTemplate().getMessageConverter().fromMessage(message);
		QueueMessage<?> queueMessage = objectMapper.convertValue(decodedMessage, QueueMessage.class);		
		MessageProperties messageProperites = message.getMessageProperties();
		Object messageObj = getMessageObj(queueMessage);
		try{
			messageCount++;
			logger.error("Total Number processed " + messageCount);
			if (messageCount%3 == 0) {
				channel.basicAck(messageProperites.getDeliveryTag(), false);
				throw new Exception("We fail one message out of three.");
			}
			/* Keep in mind that if the message is not acked it will remain in unacked status.
			 *  Once we restart the channel for the consumer, it will go back to the queue in ready status. 
			 *  And will be consumed by the consumer again.
			 */
			channel.basicAck(messageProperites.getDeliveryTag(), false);
			process(messageObj);
			
		}  catch(IOException e1){
			logger.error("Failed to Acknowledge the message " + queueMessage.getPayload().toString() + " The Exceptions is:  " + e1.getMessage());
			return;
		} catch (Exception e) {
			logger.error("Failed to process the message " + queueMessage.getPayload().toString() + " The Exceptions is:  " + e.getMessage());
			retry(messageObj, messageProperites, e.getMessage());
		}
	}

	protected <T> void retry(T message, MessageProperties messageProperties, String errorDetails) {
		int currentRetryCount = 0;
		String routingKey = messageProperties.getReceivedRoutingKey();
		if (messageProperties.getHeaders().get("retry_count") != null){
			currentRetryCount =(int)messageProperties.getHeaders().get("retry_count"); 			
		} else{
			routingKey = retryPrefix + "." + routingKey;
		}
		int newRetryCount = currentRetryCount + 1;		
		if (newRetryCount <= 5){
			String currentExpiration = "2000"; 
			if( messageProperties.getHeaders().get("expiration") != null){
				currentExpiration = (String)messageProperties.getHeaders().get("expiration");
			}
			int expiration = Integer.parseInt(currentExpiration);
			String newExpiration = Integer.toString(newRetryCount * 5 * expiration);
			MessageProperties newMessageProperties = new MessageProperties();
			newMessageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
			newMessageProperties.setExpiration(newExpiration);
			newMessageProperties.setHeader("retry_count", newRetryCount);
			newMessageProperties.setHeader("expiration", newExpiration);
			newMessageProperties.setConsumerQueue(messageProperties.getConsumerQueue());
			messageSender.sendDeadLetter(routingKey, message, newMessageProperties);		
			//failedMessageMailer.send("We have retry the message 5 times but failed",errorDetails, message);			
			messagePersistService.save(messageProperties.getReceivedExchange(), messageProperties.getReceivedRoutingKey(), message, errorDetails, "unprocess");

		} else {
			//failedMessageMailer.send("We have retry the message 5 times but failed",errorDetails, message);			
			messagePersistService.save(messageProperties.getReceivedExchange(), messageProperties.getReceivedRoutingKey(), message, errorDetails, "unprocess");
			logger.error("Still can not process the message after 5 retry. The message is :" + message.toString());
		}
	}
	
}