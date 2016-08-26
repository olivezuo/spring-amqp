package com.jin.consumer;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin.config.mq.MQConfiguration;
import com.jin.queue.QueueMessage;
import com.jin.service.MessageReceiveServiceImpl;
import com.jin.service.MessageSendServiceImpl;

public abstract class AbsJinConsumerImpl implements JinConsumer {

	private static final Logger logger = LoggerFactory.getLogger(AbsJinConsumerImpl.class);
	
	private int messageCount;
	
	private SimpleMessageListenerContainer simpleMessageListenerContainer;
	
	protected int maxConcurrentConsumers = 10;
	protected int concurrentConsumers = 3; 

	
	@Autowired 
	MQConfiguration mqConf;
	
	@Autowired
	MessageSendServiceImpl messageSender;
	
	@Autowired
	MessageReceiveServiceImpl messageReceiver;
	
	protected abstract <T> T getMessageObj(QueueMessage<T> queueMessage);
	
	protected abstract <T> void process(T message);

	protected void startContainer(String queueName, String routingKey, int maxConcurrentConsumers, int concurrentConsumers){
		simpleMessageListenerContainer = messageReceiver.simpleMessageListenerContainer(queueName, routingKey,maxConcurrentConsumers, concurrentConsumers, this);
		simpleMessageListenerContainer.start();
	}
	
	public SimpleMessageListenerContainer getSimpleMessageListenerContainer() {
		return simpleMessageListenerContainer;
	}

	@Override
	public void receive(Message message) {
		ObjectMapper objectMapper = new ObjectMapper();
		Object decodedMessage = mqConf.rabbitTemplate().getMessageConverter().fromMessage(message);
		QueueMessage<?> queueMessage = objectMapper.convertValue(decodedMessage, QueueMessage.class);		
		MessageProperties messageProperites = message.getMessageProperties();
		Object messageObj = getMessageObj(queueMessage);
		try{
			messageCount++;
			if (messageCount%3 == 0) {
				throw new Exception("We fail one message out of three.");
			}
			
			process(messageObj);
		} catch(Exception e){
			logger.error(e.getMessage());
			retry(messageObj, messageProperites);
		}
	}

	@Override
	public <T> void retry(T message, MessageProperties messageProperties) {
		int currentRetryCount = 0;
		if (messageProperties.getHeaders().get("retry_count") != null){
			currentRetryCount =(int)messageProperties.getHeaders().get("retry_count"); 
		}
		int newRetryCount = currentRetryCount + 1;		
		if (newRetryCount <= 5){
			String routingKey = messageProperties.getReceivedRoutingKey();
			String currentExpiration = "1000"; 
			if( messageProperties.getExpiration() != null){
				currentExpiration = messageProperties.getExpiration();
			}
			int expiration = Integer.parseInt(currentExpiration);
			String newExpiration = Integer.toString(newRetryCount * 5 * expiration);
			MessageProperties newMessageProperties = new MessageProperties();
			newMessageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
			newMessageProperties.setExpiration(newExpiration);
			newMessageProperties.setHeader("retry_count", newRetryCount);
			newMessageProperties.setConsumerQueue(messageProperties.getConsumerQueue());
			messageSender.sendDeadLetter(routingKey, message, newMessageProperties);
		} else {
			logger.error("Still can not process the message after 5 retry. The message is :" + message.toString());
		}

	}
	
	public void start() {
		simpleMessageListenerContainer.start();
	}

	@PreDestroy
	public void stop() {
		logger.info("We will stop the consumer: " + this.getClass());
		simpleMessageListenerContainer.stop();
	}
	
	public void setConcurrentConsumer(int concurrentConsumers) {
		if (concurrentConsumers < maxConcurrentConsumers) {
			simpleMessageListenerContainer.setConcurrentConsumers(concurrentConsumers);
		} else {
			simpleMessageListenerContainer.setConcurrentConsumers(maxConcurrentConsumers);
		}
	}

}
