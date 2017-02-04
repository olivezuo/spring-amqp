package com.jin.queue.consumer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;

import com.jin.queue.QueueMessage;
import com.jin.queue.config.MQConfig;
import com.jin.queue.service.MessagePersistService;
import com.jin.queue.service.MessageReceiveServiceImpl;
import com.jin.queue.service.MessageSendService;
import com.rabbitmq.client.Channel;

public abstract class AbsJinConsumerImpl extends MessageReceiveServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(AbsJinConsumerImpl.class);

	protected String retryPrefix ;
	
	@Autowired 
	MQConfig mqConf;
	
	@Autowired
	MessageSendService messageSender;
	
	//@Autowired
	//FailedMessageMailer failedMessageMailer;
	
	@Autowired
	MessagePersistService messagePersistService;
	
	protected abstract <T> T getMessageObj(T decodedMessage);
	
	protected abstract <T> void process(T message) throws Exception;
	
	@Override
	public void receive(Message message, Channel channel) {
		logger.info("The received message is " + message.toString());
		
		Object decodedMessage = mqConf.rabbitTemplate().getMessageConverter().fromMessage(message);
		MessageProperties messageProperites = message.getMessageProperties();
		Object messageObj = getMessageObj(decodedMessage);
		try{
			channel.basicAck(messageProperites.getDeliveryTag(), false);
			process(messageObj);
		} catch(IOException e1){
				logger.error("Failed to Acknowledge message with type: " + messageObj.getClass().getName() + ". The message is: " + messageObj.toString() + " The Exceptions is:  " + e1.getMessage(),e1);
				return;
		} catch (Exception e) {
			logger.error("Failed to process the message with type: " + messageObj.getClass().getName() + ". The message is: " + messageObj.toString() + " The Exceptions is:  " + e.getMessage(),e);
			retry(decodedMessage, messageProperites, e.getMessage());
		}
	}
	
	protected <T> void retry(T message, MessageProperties messageProperties, String errorDetails) {
		int currentRetryCount = 0;
		String routingKey = messageProperties.getReceivedRoutingKey();
		if (messageProperties.getHeaders().get("retry_count") != null){
			currentRetryCount =(int)messageProperties.getHeaders().get("retry_count"); 
		} else {
			routingKey = retryPrefix + "." + routingKey;
		}
		int newRetryCount = currentRetryCount + 1;		
		if (newRetryCount <= mqConf.getMaxRetryCount()){
			int currentExpiration = mqConf.getInitialExpiration();
			if( messageProperties.getHeaders().get("expiration") != null){
				currentExpiration =  Integer.parseInt( (String)messageProperties.getHeaders().get("expiration"));
			}
			String newExpiration = Integer.toString(newRetryCount * mqConf.getRetryFactor() * currentExpiration);
			MessageProperties newMessageProperties = new MessageProperties();
			newMessageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
			newMessageProperties.setExpiration(newExpiration);
			newMessageProperties.setHeader("retry_count", newRetryCount);
			newMessageProperties.setHeader("expiration", newExpiration);
			newMessageProperties.setConsumerQueue(messageProperties.getConsumerQueue());
			newMessageProperties.setContentType(messageProperties.getContentType());
			messageSender.sendDeadLetter(routingKey, message, newMessageProperties);
		} else {
			messagePersistService.save(messageProperties.getReceivedExchange(), messageProperties.getReceivedRoutingKey(), message, errorDetails, "unprocess");
			logger.error("Still can not process the message after all retries, abort. The message is :" + message.toString());
			//failedMessageMailer.send("We have retry the message 5 times but failed",errorDetails, message);
		}
	}
	
}
