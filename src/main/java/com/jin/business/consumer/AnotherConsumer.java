package com.jin.business.consumer;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin.business.message.AnotherMessage;
import com.jin.business.service.AnotherService;
import com.jin.queue.QueueMessage;
import com.jin.queue.consumer.AbsJinConsumerImpl;

@Service
public class AnotherConsumer extends AbsJinConsumerImpl {

	private static final Logger logger = LoggerFactory.getLogger(AnotherConsumer.class);
	
	@Autowired
	AnotherService anotherService;

	@PostConstruct
	public void init() {
		String queueName = "jin.message.another";
		String routingKey = "jin.another";
		retryPrefix = "another";
		String retryRoutingKey = retryPrefix + "." + routingKey;
		int maxConcurrentConsumers = 2;
		int concurrentConsumers = 2; 
				
		startMessageListenerContainer(queueName, routingKey, retryRoutingKey, maxConcurrentConsumers, concurrentConsumers);
	}

	@Override
	protected <T> T getMessageObj(T decodedMessage) {
		
		AnotherMessage message = null;
		ObjectMapper objectMapper = new ObjectMapper();
		QueueMessage<?> queueMessage = objectMapper.convertValue(decodedMessage, QueueMessage.class);

		switch (queueMessage.type) {
		case "com.jin.business.message.AnotherMessage":
			message = objectMapper.convertValue(queueMessage.getPayload(), AnotherMessage.class);
			break;		
		default:
			logger.error("We receive the wrong message, the type of the message is " + queueMessage.type + ". But we are expecting com.jin.business.message.AnotheMessage");
		}
		return (T)message;
	}

	@Override
	protected <T> void process(T message) {
		anotherService.receive((AnotherMessage)message);
		
	}

}
