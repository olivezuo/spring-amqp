package com.jin.queue.service;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import com.jin.consumer.JinConsumer;

public interface MessageReceiveService {

	public SimpleMessageListenerContainer simpleMessageListenerContainer(String queueName, String routingKey, int maxConcurrentConsumers, int concurrentConsumers, JinConsumer consumer );
}
