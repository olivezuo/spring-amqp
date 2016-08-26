package com.jin.consumer;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin.message.SimpleMessage;
import com.jin.queue.QueueMessage;
import com.jin.service.SimpleService;

@Service
public class SimpleConsumer extends AbsJinConsumerImpl{

	private static final Logger logger = LoggerFactory.getLogger(SimpleConsumer.class);
	
	@Autowired
	SimpleService simpleService;

	@PostConstruct
	public void init() {
		String queueName = "jin.message.simple";
		String routingKey = "jin.simple";
		maxConcurrentConsumers = 10;
		concurrentConsumers = 3; 
				
		startContainer(queueName, routingKey, maxConcurrentConsumers, concurrentConsumers);
	}

	@Override
	protected <T> T getMessageObj(QueueMessage<T> queueMessage) {
		
		SimpleMessage message = null;
		switch (queueMessage.type) {
		case "com.jin.message.SimpleMessage":
			ObjectMapper objectMapper = new ObjectMapper();
			message = objectMapper.convertValue(queueMessage.getPayload(), SimpleMessage.class);
			break;		
		default:
			logger.error("We receive the wrong message, the type of the message is " + queueMessage.type + ". But we are expecting com.jin.message.SimpleMessage");
		}
		return (T)message;
	}

	@Override
	protected <T> void process(T message) {
		simpleService.receive((SimpleMessage)message);
		
	}

}
