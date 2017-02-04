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
import org.springframework.stereotype.Service;

import com.jin.queue.QueueMessage;
import com.jin.queue.config.MQConfig;

@Service
public class MessageSendServiceImpl implements MessageSendService {
	
	private static final Logger logger = LoggerFactory.getLogger(MessageSendServiceImpl.class);
	
	@Autowired
	MQConfig mqConf;
	
	//@Autowired
	//FailedMessageMailer failedMessageMailer;
	
	@Autowired
	MessagePersistService messagePersistService;

	@Override
	public <T> void send(String exchange, String routingKey, T message, MessageProperties messageProperties) {
		/*
		 * All internal message Entitiy will be send as json message
		 * Others will be plain text.
		 */
		if (message.getClass().getName().equals(String.class.getName())) {
			messageProperties.setContentType("text/plain");
			this.sendMessage(exchange, routingKey, message, messageProperties);
		} else {
			messageProperties.setContentType("application/json");
			QueueMessage<T> queueMessage = new QueueMessage<T>(message);
			this.sendMessage(exchange, routingKey, queueMessage, messageProperties);	
		}
	}
	
	public <T> void sendMessage(String exchange, String routingKey, T message, MessageProperties messageProperties){
		Message rabbitMessage = mqConf.rabbitTemplate().getMessageConverter().toMessage(message, messageProperties);
		try {			
			mqConf.rabbitTemplate().send(exchange, routingKey, rabbitMessage);
			logger.info("Successfully sent the message " + rabbitMessage.toString());
		} catch (AmqpException e) {
			messagePersistService.save(exchange, routingKey, message, e.getMessage(), "unsend");
			logger.error("failed to sent the message " + rabbitMessage.toString());
			//failedMessageMailer.send("We are not able to send the message", e.getMessage(), message);
		}			
	}
	
	@Override
	public <T> void send(String exchange, String routingKey, T message) {
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
		
		send(exchange, routingKey, message, messageProperties);		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.jin.queue.service.MessageSendService#send(java.lang.String, java.lang.Object)
	 * Current we can send internal Message Entity or String as messages.
	 */
	@Override
	public  <T> void send(String routingKey, T message) {
		
		send(mqConf.getExchange(),routingKey, message);

	}

	@Override
	public <T> void sendDeadLetter(String routingKey, T message, MessageProperties messageProperites) {
		String queueName = messageProperites.getConsumerQueue();
		String deadLetterQueueName = "deadLetter." + queueName;
		declareDeadLetterQueue(deadLetterQueueName, routingKey);		
		sendMessage(mqConf.getDeadLetterExchange(), routingKey, message, messageProperites);	
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
