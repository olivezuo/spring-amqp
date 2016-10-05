package com.jin.queue.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.jin.config.MQConfig;
import com.jin.consumer.JinConsumer;
import com.rabbitmq.client.Channel;

@Configuration
public class MessageReceiveServiceImpl implements MessageReceiveService {
	private static final Logger logger = LoggerFactory.getLogger(MessageReceiveServiceImpl.class);
	
	@Autowired
	MQConfig mqConfig;
		
	@Override
	public SimpleMessageListenerContainer simpleMessageListenerContainer(String queueName, String routingKey, String retryRoutingKey, int maxConcurrentConsumers, int concurrentConsumers, JinConsumer consumer ) {
		
		SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
		
		simpleMessageListenerContainer.setConnectionFactory(mqConfig.defaultConnectionFactory());
		simpleMessageListenerContainer.setQueues(queue(queueName,routingKey, retryRoutingKey));
		simpleMessageListenerContainer.setMaxConcurrentConsumers(maxConcurrentConsumers);
		simpleMessageListenerContainer.setConcurrentConsumers(concurrentConsumers);
		simpleMessageListenerContainer.setMessageListener(messageListener(consumer));
		simpleMessageListenerContainer.setMessageConverter(mqConfig.messageConverter());
		simpleMessageListenerContainer.setAutoDeclare(true);
		simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		simpleMessageListenerContainer.setPrefetchCount(1);
		logger.info("Successfully create Message Listener Container for queue " + queueName + " with routing key " + routingKey);				
		return simpleMessageListenerContainer;		
	}
	
	public Queue queue(String queueName, String routingKey, String retryRoutingKey) {
		Queue queue= new Queue(queueName, true);
		Binding binding = BindingBuilder.bind(queue).to(mqConfig.workingExchange()).with(routingKey);
		Binding retryBinding = BindingBuilder.bind(queue).to(mqConfig.workingExchange()).with(retryRoutingKey);
		mqConfig.rabbitAdmin().declareQueue(queue);
		mqConfig.rabbitAdmin().declareBinding(binding);
		mqConfig.rabbitAdmin().declareBinding(retryBinding);
		return queue;
		
	}
	
    protected ChannelAwareMessageListener messageListener(JinConsumer consumer) {
        return new ChannelAwareMessageListener() {
            public void onMessage(Message message, Channel channel) {
            	consumer.receive(message, channel);
            }
        };
    }

	

}
