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
import org.springframework.stereotype.Service;

import com.jin.queue.config.MQConfig;
import com.rabbitmq.client.Channel;

@Service
public abstract class MessageReceiveServiceImpl implements MessageReceiveService {
	private static final Logger logger = LoggerFactory.getLogger(MessageReceiveServiceImpl.class);
	
	@Autowired
	MQConfig mqConf;
	
	protected int maxConcurrentConsumers = 10;
	protected int concurrentConsumers = 3; 

	protected SimpleMessageListenerContainer simpleMessageListenerContainer;

	protected void startMessageListenerContainer(String queueName, String routingKey, String retryRoutingKey, int maxConcurrentConsumers, int concurrentConsumers){
		initSimpleMessageListenerContainer(queueName, routingKey, retryRoutingKey, maxConcurrentConsumers, concurrentConsumers);
		// TODO add a flag to instantiate an consumer but not to start.
		simpleMessageListenerContainer.start();
	}
	
	protected void initSimpleMessageListenerContainer(String queueName, String routingKey, String retryRoutingKey, int maxConcurrentConsumers, int concurrentConsumers) {		
		simpleMessageListenerContainer = new SimpleMessageListenerContainer();		
		simpleMessageListenerContainer.setConnectionFactory(mqConf.defaultConnectionFactory());
		simpleMessageListenerContainer.setQueues(queue(queueName, routingKey, retryRoutingKey));
		simpleMessageListenerContainer.setMaxConcurrentConsumers(maxConcurrentConsumers);
		simpleMessageListenerContainer.setConcurrentConsumers(concurrentConsumers);
		simpleMessageListenerContainer.setMessageListener(messageListener());
		simpleMessageListenerContainer.setMessageConverter(mqConf.messageConverter());
		simpleMessageListenerContainer.setAutoDeclare(true);
		simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		simpleMessageListenerContainer.setPrefetchCount(1);
		logger.info("Successfully create Message Listener Container for queue " + queueName + " with routing key " + routingKey);
	}
	
	public Queue queue(String queueName, String routingKey, String retryRoutingKey) {
		Queue queue= new Queue(queueName, true);
		Binding binding = BindingBuilder.bind(queue).to(mqConf.workingExchange()).with(routingKey);
		Binding retryBinding = BindingBuilder.bind(queue).to(mqConf.workingExchange()).with(retryRoutingKey);
		mqConf.rabbitAdmin().declareQueue(queue);
		mqConf.rabbitAdmin().declareBinding(binding);
		mqConf.rabbitAdmin().declareBinding(retryBinding);
		logger.info("Successfully declare queue " + queueName + " with routing key " + routingKey);
		return queue;		
	}
	
    public ChannelAwareMessageListener messageListener() {
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
