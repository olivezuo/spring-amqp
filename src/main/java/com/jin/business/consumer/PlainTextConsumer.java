package com.jin.business.consumer;

import org.springframework.beans.factory.annotation.Autowired;

import com.jin.business.service.PlainTextService;
import com.jin.queue.consumer.AbsJinConsumerImpl;

public class PlainTextConsumer extends AbsJinConsumerImpl {

	@Autowired
	PlainTextService plainTextService;
	
	@Override
	protected <T> T getMessageObj(T decodedMessage) {
		return decodedMessage;
	}

	@Override
	protected <T> void process(T message) throws Exception {
		plainTextService.receive((String)message);

	}

}
