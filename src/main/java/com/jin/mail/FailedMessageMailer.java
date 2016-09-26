package com.jin.mail;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FailedMessageMailer {
	private static final Logger logger = LoggerFactory.getLogger(FailedMessageMailer.class);

	@Autowired
	MailServiceImpl mailService;
	
	@Value("${message.failed.mail.from}")
	String from;

	@Value("${message.failed.mail.to}")
	String to;

	@Value("${message.failed.mail.replyTo}")
	String replyTo;


	private void send(String subject, String text, String fileName, String content) throws MessagingException{
		mailService.send(from, to, replyTo, subject, text, fileName, content, "application/json");
	}
	
	public <T> void send(String subject, String exceptionDetails, T message) {
		String fileName = message.getClass().getName() + ".json";
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			String jsonMessage = mapper.writeValueAsString(message);
			this.send(subject, exceptionDetails, fileName, jsonMessage);
		} catch (Exception e) {
			logger.error("Can not send the email. The error is: " + e.getMessage());
		}
	
	}

}
