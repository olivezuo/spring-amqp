package com.jin.queue.service;

import org.springframework.amqp.core.Message;

import com.rabbitmq.client.Channel;

public interface MessageReceiveService {

	public void receive(Message message, Channel channel);
}
