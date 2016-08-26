package com.jin.consumer;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

public interface JinConsumer {
	
	public void receive(Message message);
	
	public <T> void retry(T message,  MessageProperties messageProperties);

}
