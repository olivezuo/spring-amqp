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
				
		startContainer(queueName, routingKey, retryRoutingKey, maxConcurrentConsumers, concurrentConsumers);
	}

	@Override
	protected <T> T getMessageObj(QueueMessage<T> queueMessage) {
		
		AnotherMessage message = null;
		switch (queueMessage.type) {
		case "com.jin.message.AnotherMessage":
			ObjectMapper objectMapper = new ObjectMapper();
			message = objectMapper.convertValue(queueMessage.getPayload(), AnotherMessage.class);
			break;		
		default:
			logger.error("We receive the wrong message, the type of the message is " + queueMessage.type + ". But we are expecting com.jin.message.AnotheMessage");
		}
		return (T)message;
	}

	@Override
	protected <T> void process(T message) {
		anotherService.receive((AnotherMessage)message);
		
	}

}
