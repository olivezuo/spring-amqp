package com.jin.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jin.message.AnotherMessage;
import com.jin.queue.service.MessageSendServiceImpl;

@Service
public class AnotherService {
	private static final Logger logger = LoggerFactory.getLogger(AnotherService.class);
	
	@Autowired
	MessageSendServiceImpl messageSender;
	
	AnotherMessage message = new AnotherMessage("soccer", 22, "Retriever");
	
	@PostConstruct
    public void init() {
	}
	
	public void send() {
		messageSender.send("jin.another", message);
	}
	
	public void receive(AnotherMessage message) {
		logger.info("Receive a message: " + message.toString());
	}

}
