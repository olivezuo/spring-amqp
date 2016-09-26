package com.jin.queue.service;

public interface MessagePersistService {

	public<T> void save(String exchange, String routingKey, T message, String errorDetails, String failType);
}
