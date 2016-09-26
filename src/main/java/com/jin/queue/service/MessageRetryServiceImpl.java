package com.jin.queue.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin.domain.FailedMessage;
import com.jin.repository.FailedMessageRepository;

@Service
public class MessageRetryServiceImpl implements MessageRetryService {
	private static final Logger logger = LoggerFactory.getLogger(MessageRetryServiceImpl.class);
	
	@Autowired
	FailedMessageRepository failedMessageRepository;
	
	@Autowired
	MessageSendService messageSendService;
	
	@Override
	public int retryAll() {		
		List<FailedMessage> failedMessages = failedMessageRepository.findByRetried(false);
		return retryAll(failedMessages);
	}
	
	public int retryByMessageType(String messageType) {
		List<FailedMessage> failedMessages = failedMessageRepository.findByMessageTypeAndRetried(messageType, false);
		return retryAll(failedMessages);
	}
	
	private int retryAll(List<FailedMessage> failedMessages) {
		if (failedMessages.size() > 0){
			for (FailedMessage failedMessage : failedMessages) {
				try {
					retry(failedMessage);
					updateFailedMessage(failedMessage);
				} catch (Exception e) {					
					logger.error("Can not resend the message with id " + failedMessage.getId() + " The error is " + e.getMessage());
				}
				
			}
		}
		return failedMessages.size();
	}
	
	private void updateFailedMessage(FailedMessage failedMessage) {
		failedMessage.setRetried(true);
		failedMessage.setDateRetried(new Date());
		failedMessageRepository.save(failedMessage);
	}
	
	private void retry(FailedMessage failedMessage) throws Exception {
		String rawMessage = failedMessage.getMessage();
			Class<?> messageClass = Class.forName(failedMessage.getMessageType());
			ObjectMapper objectMapper = new ObjectMapper();
			Object message = objectMapper.readValue(rawMessage, messageClass);

			messageSendService.send(failedMessage.getExchange(), failedMessage.getRoutingKey(), message);
			
	}
}