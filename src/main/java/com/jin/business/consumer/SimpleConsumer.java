package com.jin.business.consumer;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin.business.message.SimpleMessage;
import com.jin.business.service.SimpleService;
import com.jin.queue.QueueMessage;
import com.jin.queue.consumer.AbsJinConsumerImpl;

@Service
public class SimpleConsumer extends AbsJinConsumerImpl{

	private static final Logger logger = LoggerFactory.getLogger(SimpleConsumer.class);
	
	@Autowired
	SimpleService simpleService;

	@PostConstruct
	public void init() {
		String queueName = "jin.message.simple";
		String routingKey = "jin.simple";
		retryPrefix = "simple";
		String retryRoutingKey = retryPrefix + "." + routingKey;
		maxConcurrentConsumers = 20;
		concurrentConsumers = 3; 
		startMessageListenerContainer(queueName, routingKey, retryRoutingKey, maxConcurrentConsumers, concurrentConsumers);
		logger.info("I am a new SimpleConsumer");
	}

	@Override
	protected <T> T getMessageObj(T decodedMessage){
		
		SimpleMessage message = null;
		ObjectMapper objectMapper = new ObjectMapper();
		QueueMessage<?> queueMessage = objectMapper.convertValue(decodedMessage, QueueMessage.class);
		
		switch (queueMessage.type) {
		case "com.jin.business.message.SimpleMessage":
			message = objectMapper.convertValue(queueMessage.getPayload(), SimpleMessage.class);
			break;		
		default:
			logger.error("We receive the wrong message, the type of the message is " + queueMessage.type + ". But we are expecting com.jin.business.message.SimpleMessage");
		}
		return (T)message;
	}

	@Override
	protected <T> void process(T message) {
		
		simpleService.receive((SimpleMessage)message);
		
	}

}
