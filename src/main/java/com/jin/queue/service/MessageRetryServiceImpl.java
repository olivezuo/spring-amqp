package com.jin.queue.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin.queue.domain.FailedMessage;
import com.jin.queue.repository.FailedMessageRepository;

@Service
public class MessageRetryServiceImpl implements MessageRetryService {
	private static final Logger logger = LoggerFactory.getLogger(MessageRetryServiceImpl.class);
	
	@Autowired
	FailedMessageRepository failedMessageRepository;
	
	@Autowired
	MessageSendService messageSendService;
	
	@Override
	public int retryAll() {
		boolean working = true;
		int count = 0;
		while (working) {
			List<FailedMessage> failedMessages = failedMessageRepository.findByRetried(false, new PageRequest(0, 3));
			if (failedMessages.size() != 0){
				logger.info("We get " + failedMessages.size() + " records");
				retryAll(failedMessages);
				count += failedMessages.size();
			} else {
				working = false;
			}
		}	
		return count;
	}
	
	public int retryByMessageType(String messageType) {
		boolean working = true;
		int count = 0;
		while (working) {
			List<FailedMessage> failedMessages = failedMessageRepository.findByMessageTypeAndRetried(messageType, false, new PageRequest(0, 3));
			if (failedMessages.size() != 0){
				logger.info("We get " + failedMessages.size() + " records");
				retryAll(failedMessages);
				count += failedMessages.size();
			} else {
				working = false;
			}
		}		
		return count;	
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
