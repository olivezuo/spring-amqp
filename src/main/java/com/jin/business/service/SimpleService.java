package com.jin.business.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jin.business.message.SimpleMessage;
import com.jin.queue.service.MessageSendServiceImpl;

@Service
public class SimpleService {
	private static final Logger logger = LoggerFactory.getLogger(SimpleService.class);
	
	private int messageCount;
	
	@Autowired
	MessageSendServiceImpl messageSender;
	
	SimpleMessage message = new SimpleMessage("jin", "m", "123 Disney");
	
	@PostConstruct
    public void init() {

	}
	
	public void send() {

		messageSender.send("jin.simple", message);
	}
	
	public void receive(SimpleMessage message) {
		messageCount++;
		logger.info("Total Number received " + messageCount);
		logger.info("Receive a message: " + message.toString());
	}
	

}
