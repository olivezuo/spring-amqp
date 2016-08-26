package com.jin.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.jin.config.mq.MQConfiguration;
import com.jin.queue.QueueMessage;

@Configuration
@ConfigurationProperties(prefix = "com.jin.queue")
public class MessageSendServiceImpl implements MessageSendService {
	
	private static final Logger logger = LoggerFactory.getLogger(MessageSendServiceImpl.class);
	
	@Autowired
	MQConfiguration mqConf;

	@Override
	public <T> void send(String exchange, String routingKey, T message, MessageProperties messageProperites) {
		QueueMessage<T> jinMessage = new QueueMessage<T>(message);
		Message rabbitMessage = mqConf.rabbitTemplate().getMessageConverter().toMessage(jinMessage, messageProperites);
		
		mqConf.rabbitTemplate().send(exchange, routingKey, rabbitMessage);

		logger.info("Successfully sent the message " + message.toString());	
	}
	
	@Override
	public <T> void send(String exchange, String routingKey, T message) {		
		MessageProperties messageProperites = new MessageProperties();
		messageProperites.setDeliveryMode(MessageDeliveryMode.PERSISTENT);	
		send(exchange, routingKey, message, messageProperites);
	}
	
	@Override
	public  <T> void send(String routingKey, T message) {
		
		send(mqConf.getExchange(),routingKey, message);

	}

	@Override
	public <T> void sendDeadLetter(String routingKey, T message, MessageProperties messageProperites) {
		String queueName = messageProperites.getConsumerQueue();
		String deadLetterQueueName = "deadLetter." + queueName;
		declareDeadLetterQueue(deadLetterQueueName, routingKey);		
		send(mqConf.getDeadLetterExchange(), routingKey, message, messageProperites);	
	}
	
	public void  declareDeadLetterQueue(String queueName, String routingKey) {
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("x-dead-letter-exchange", mqConf.getExchange());
		Queue queue= new Queue(queueName, true, false, false, args);
		Binding binding = BindingBuilder.bind(queue).to(mqConf.deadLetterExchange()).with(routingKey);
		mqConf.rabbitAdmin().declareQueue(queue);
		mqConf.rabbitAdmin().declareBinding(binding);
		
	}

	

}
