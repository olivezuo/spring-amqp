package com.jin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.jin.config.mq.MQConfiguration;
import com.jin.consumer.JinConsumer;

@Configuration
public class MessageReceiveServiceImpl implements MessageReceiveService {
	private static final Logger logger = LoggerFactory.getLogger(MessageReceiveServiceImpl.class);
	
	@Autowired
	MQConfiguration mqConf;
		
	@Override
	public SimpleMessageListenerContainer simpleMessageListenerContainer(String queueName, String routingKey, int maxConcurrentConsumers, int concurrentConsumers, JinConsumer consumer ) {
		
		SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
		
		simpleMessageListenerContainer.setConnectionFactory(mqConf.defaultConnectionFactory());
		simpleMessageListenerContainer.setQueues(queue(queueName,routingKey));
		simpleMessageListenerContainer.setMaxConcurrentConsumers(maxConcurrentConsumers);
		simpleMessageListenerContainer.setConcurrentConsumers(concurrentConsumers);
		simpleMessageListenerContainer.setMessageListener(messageListener(consumer));
		simpleMessageListenerContainer.setMessageConverter(mqConf.messageConverter());
		simpleMessageListenerContainer.setAutoDeclare(true);
		logger.info("Successfully create Message Listener Container for queue " + queueName + " with routing key " + routingKey);
				
		return simpleMessageListenerContainer;		
	}
	
	public Queue queue(String queueName, String routingKey) {
		Queue queue= new Queue(queueName, true);
		Binding binding = BindingBuilder.bind(queue).to(mqConf.workingExchange()).with(routingKey);
		mqConf.rabbitAdmin().declareQueue(queue);
		mqConf.rabbitAdmin().declareBinding(binding);
		return queue;
		
	}
	
    protected MessageListener messageListener(JinConsumer consumer) {
        return new MessageListener() {
            public void onMessage(Message message) {
            	consumer.receive(message);
            }
        };
    }

	

}
