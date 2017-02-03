package com.jin.queue.service;

import javax.annotation.PreDestroy;

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

import com.jin.queue.config.MQConfig;
import com.rabbitmq.client.Channel;

@Configuration
public abstract class MessageReceiveServiceImpl implements MessageReceiveService {
	private static final Logger logger = LoggerFactory.getLogger(MessageReceiveServiceImpl.class);
	
	@Autowired
	MQConfig mqConfig;
	
	protected SimpleMessageListenerContainer simpleMessageListenerContainer;
	
	protected int maxConcurrentConsumers = 20;
	protected int concurrentConsumers = 3; 
	
	protected void startContainer(String queueName, String routingKey, String retryRoutingKey, int maxConcurrentConsumers, int concurrentConsumers){
		initSimpleMessageListenerContainer(queueName, routingKey, retryRoutingKey, maxConcurrentConsumers, concurrentConsumers);
		simpleMessageListenerContainer.start();
	}
	
	protected void initSimpleMessageListenerContainer(String queueName, String routingKey, String retryRoutingKey, int maxConcurrentConsumers, int concurrentConsumers) {
		
		SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
		
		simpleMessageListenerContainer.setConnectionFactory(mqConfig.defaultConnectionFactory());
		simpleMessageListenerContainer.setQueues(queue(queueName,routingKey, retryRoutingKey));
		simpleMessageListenerContainer.setMaxConcurrentConsumers(maxConcurrentConsumers);
		simpleMessageListenerContainer.setConcurrentConsumers(concurrentConsumers);
		simpleMessageListenerContainer.setMessageListener(messageListener());
		simpleMessageListenerContainer.setMessageConverter(mqConfig.messageConverter());
		simpleMessageListenerContainer.setAutoDeclare(true);
		simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		simpleMessageListenerContainer.setPrefetchCount(1);
		logger.info("Successfully create Message Listener Container for queue " + queueName + " with routing key " + routingKey);				
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
	
    protected ChannelAwareMessageListener messageListener() {
        return new ChannelAwareMessageListener() {
            public void onMessage(Message message, Channel channel) {
            	receive(message, channel);
            }
        };
    }

	
	public void start() {
		simpleMessageListenerContainer.start();
	}

	@PreDestroy
	public void stop() {
		logger.info("We will stop the consumer: " + this.getClass());
		simpleMessageListenerContainer.stop();
	}
	
	public void setConcurrentConsumer(int concurrentConsumers) {
		if (concurrentConsumers < maxConcurrentConsumers) {
			simpleMessageListenerContainer.setConcurrentConsumers(concurrentConsumers);
			this.concurrentConsumers = concurrentConsumers;
		} else {
			simpleMessageListenerContainer.setConcurrentConsumers(maxConcurrentConsumers);
			this.concurrentConsumers = maxConcurrentConsumers;
		}
	}

	public void setMaxConcurrentConsumers(int maxConcurrentConsumers) {
		if (maxConcurrentConsumers > concurrentConsumers) {
			simpleMessageListenerContainer.setMaxConcurrentConsumers(maxConcurrentConsumers);
			this.maxConcurrentConsumers = maxConcurrentConsumers;
		} else {
			simpleMessageListenerContainer.setMaxConcurrentConsumers(this.concurrentConsumers);
			this.maxConcurrentConsumers = this.concurrentConsumers;
		}
	}

}
