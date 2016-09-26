package com.jin.queue.service;

public interface MessageRetryService {

	public int retryAll();
	
	public int retryByMessageType(String messageType);
}
