package com.jin.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jin.queue.service.MessageRetryService;

@RestController
public class FailedMessageController {
	
	@Autowired
	MessageRetryService messageRetryService;
	
	
	@RequestMapping("/failedmessage/retryall")
	public void retryAll() {
		messageRetryService.retryAll();
	}

	@RequestMapping("/failedmessage/retrybymessagetype/{messageType:.+}")
	public void retryByMessageType(@PathVariable("messageType") String messageType) {
		messageRetryService.retryByMessageType(messageType);
		
	}
}
