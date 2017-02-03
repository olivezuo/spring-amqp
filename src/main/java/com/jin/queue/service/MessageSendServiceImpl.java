package com.jin.queue.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.jin.mail.FailedMessageMailer;
import com.jin.queue.QueueMessage;
import com.jin.queue.config.MQConfig;

@Configuration
@ConfigurationProperties(prefix = "com.jin.queue")
public class MessageSendServiceImpl implements MessageSendService {
	
	private static final Logger logger = LoggerFactory.getLogger(MessageSendServiceImpl.class);
	
	@Autowired
	MQConfig mqConfig;
	
	@Autowired
	FailedMessageMailer failedMessageMailer;
	
	@Autowired
	MessagePersistService messagePersistService;

	@Override
	public <T> void send(String exchange, String routingKey, T message, MessageProperties messageProperites) {
		QueueMessage<T> jinMessage = new QueueMessage<T>(message);
		Message rabbitMessage = mqConfig.rabbitTemplate().getMessageConverter().toMessage(jinMessage, messageProperites);
		
		try {
			mqConfig.rabbitTemplate().send(exchange, routingKey, rabbitMessage);
			logger.info("Successfully sent the message " + message.toString());
		} catch (AmqpException e) {
			messagePersistService.save(exchange, routingKey, message, e.getMessage(), "unsend");
			logger.error("failed to sent the message " + message.toString());
			failedMessageMailer.send("We are not able to send the message", e.getMessage(), message);
			
		}			
	}
	
	@Override
	public <T> void send(String exchange, String routingKey, T message) {		
		MessageProperties messageProperites = new MessageProperties();
		messageProperites.setDeliveryMode(MessageDeliveryMode.PERSISTENT);	
		send(exchange, routingKey, message, messageProperites);
	}
	
	@Override
	public  <T> void send(String routingKey, T message) {
		
		send(mqConfig.getExchange(),routingKey, message);

	}

	@Override
	public <T> void sendDeadLetter(String routingKey, T message, MessageProperties messageProperites) {
		String queueName = messageProperites.getConsumerQueue();
		String deadLetterQueueName = "deadLetter." + queueName;
		declareDeadLetterQueue(deadLetterQueueName, routingKey);		
		send(mqConfig.getDeadLetterExchange(), routingKey, message, messageProperites);	
	}
	
	public void  declareDeadLetterQueue(String queueName, String routingKey) {
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("x-dead-letter-exchange", mqConfig.getExchange());
		Queue queue= new Queue(queueName, true, false, false, args);
		Binding binding = BindingBuilder.bind(queue).to(mqConfig.deadLetterExchange()).with(routingKey);
		mqConfig.rabbitAdmin().declareQueue(queue);
		mqConfig.rabbitAdmin().declareBinding(binding);
		
	}

	

}
