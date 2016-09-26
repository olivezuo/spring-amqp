package com.jin.consumer;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import com.rabbitmq.client.Channel;

public interface JinConsumer {
	
	public void receive(Message message, Channel channel);
	
	public <T> void retry(T message,  MessageProperties messageProperties, String errorDetails);

}
