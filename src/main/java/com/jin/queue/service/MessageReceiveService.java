package com.jin.queue.service;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import com.jin.business.consumer.JinConsumer;
import com.rabbitmq.client.Channel;

public interface MessageReceiveService {

	public void receive(Message message, Channel channel);
}
