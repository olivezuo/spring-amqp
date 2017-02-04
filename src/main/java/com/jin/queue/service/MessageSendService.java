package com.jin.queue.service;

import org.springframework.amqp.core.MessageProperties;

public interface MessageSendService {
	
	public <T> void send(String routingKey, T message);
	
	public <T> void sendDeadLetter(String routingKey, T message, MessageProperties messageProperties);

	public <T> void send(String exchange, String routingKey, T message);
	
	public <T> void send(String exchange, String routingKey, T message, MessageProperties messageProperties);
	
	public <T> void sendMessage(String exchange, String routingKey, T message, MessageProperties messageProperties);
	
}
