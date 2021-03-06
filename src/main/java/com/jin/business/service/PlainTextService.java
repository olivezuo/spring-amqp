package com.jin.business.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jin.queue.service.MessageSendServiceImpl;

@Service
public class PlainTextService {
	private static final Logger logger = LoggerFactory.getLogger(PlainTextService.class);

	@Autowired
	MessageSendServiceImpl messageSender;
	
	public void send() {

		messageSender.send("jin.plaintext", "This is a testing plain text as a message.");
	}
	
	public void receive(String message) {
		logger.info("Receive a plain text string message: " + message);
	}

}
