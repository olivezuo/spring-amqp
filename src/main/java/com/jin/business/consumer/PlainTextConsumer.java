package com.jin.business.consumer;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jin.business.service.PlainTextService;
import com.jin.queue.consumer.AbsJinConsumerImpl;

@Service
public class PlainTextConsumer extends AbsJinConsumerImpl {
	
	private static final Logger logger = LoggerFactory.getLogger(PlainTextConsumer.class);
	
	@Autowired
	PlainTextService plainTextService;
	
	@PostConstruct
	public void init() {
		String queueName = "jin.message.plaintext";
		String routingKey = "jin.plaintext";
		retryPrefix = "plaintext";
		String retryRoutingKey = retryPrefix + "." + routingKey;
		maxConcurrentConsumers = 20;
		concurrentConsumers = 3; 
		startMessageListenerContainer(queueName, routingKey, retryRoutingKey, maxConcurrentConsumers, concurrentConsumers);
		logger.info("I am a new plaintext consumer");
	}

	
	@Override
	protected <T> T getMessageObj(T decodedMessage) {
		return decodedMessage;
	}

	@Override
	protected <T> void process(T message) throws Exception {
		plainTextService.receive((String)message);

	}

}
