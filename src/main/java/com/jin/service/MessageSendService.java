package com.jin.service;

import org.springframework.amqp.core.MessageProperties;

public interface MessageSendService {
	
	//void send(Message message) throws AmqpException;

	public <T> void send(String routingKey, T message);
	
	public <T> void sendDeadLetter(String routingKey, T message, MessageProperties messageProperites);

	public <T >void send(String exchange, String routingKey, T message);
	
	public <T> void send(String exchange, String routingKey, T message, MessageProperties messageProperites);
}
