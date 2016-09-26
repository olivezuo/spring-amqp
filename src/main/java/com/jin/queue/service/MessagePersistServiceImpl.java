package com.jin.queue.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin.domain.FailedMessage;
import com.jin.repository.FailedMessageRepository;

@Service
public class MessagePersistServiceImpl implements MessagePersistService{
	private static final Logger logger = LoggerFactory.getLogger(MessagePersistServiceImpl.class);
	
	@Autowired
	FailedMessageRepository failedMessageRepository;
	
	@Override
	@Async
    public<T> void save(String exchange, String routingKey, T message, String errorDetails, String failType)  {
		try {			
			FailedMessage failedMessage = this.buildFailedMessageMongoEntity(exchange, routingKey, message, errorDetails, failType);
			
			failedMessageRepository.save(failedMessage);
			logger.info("We successfully save the message to db.");
			
		} catch (Exception e) {
			logger.error("We cannot save the message to db. The Exception is " + e.getMessage());
		}
		
	}
	
	
	private <T> FailedMessage buildFailedMessageMongoEntity(String exchange, String routingKey, T message, String errorDetails, String failType) throws JsonProcessingException{
		
		String messageType = message.getClass().getName();
		
		ObjectMapper mapper = new ObjectMapper();

		String jsonMessage = mapper.writeValueAsString(message);
		
		FailedMessage failedMessage = new FailedMessage(messageType, jsonMessage, routingKey, exchange, errorDetails, failType);
		
		return failedMessage;
	}
}
