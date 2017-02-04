package com.jin.queue.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin.queue.QueueMessage;
import com.jin.queue.domain.FailedMessage;
import com.jin.queue.repository.FailedMessageRepository;

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
		FailedMessage failedMessage = new FailedMessage();
		
		// We need to retrieve the real message type before save it.
		if (messageType.equals(QueueMessage.class.getName())) {			
			ObjectMapper objectMapper = new ObjectMapper();
			QueueMessage<?> queueMessage = objectMapper.convertValue(message, QueueMessage.class);
			String entityType = queueMessage.getType();
			String jsonMessage = objectMapper.writeValueAsString(message);
			failedMessage = new FailedMessage(messageType, entityType, jsonMessage, routingKey, exchange, errorDetails, failType);
		} else {
			failedMessage = new FailedMessage(String.class.getName(), String.class.getName(), (String)message, routingKey, exchange, errorDetails, failType);
		}
		
		return failedMessage;
	}
}
