package com.jin.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jin.message.SimpleMessage;
import com.jin.queue.QueueMessage;

@Service
public class SimpleService {
	private static final Logger logger = LoggerFactory.getLogger(SimpleService.class);
	
	@Autowired
	MessageSendServiceImpl messageSender;
	
	SimpleMessage message = new SimpleMessage("jin", "m", "122 denault");
	
	@PostConstruct
    public void init() {
		//send();
	}
	
	public void send() {
		messageSender.send("jin.simple", message);
	}
	
	public void receive(SimpleMessage message) {
		logger.info("Receive a message: " + message.toString());
	}
	

}
