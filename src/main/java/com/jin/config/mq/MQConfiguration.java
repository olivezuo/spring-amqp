package com.jin.config.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@ConfigurationProperties(prefix = "com.jin.queue")
public class MQConfiguration 
{	
	private static final Logger logger = LoggerFactory.getLogger(MQConfiguration.class);

	@Autowired
	private RabbitProperties props;
	
	private String exchange;
	private String deadLetterExchange;
	
	@Bean
	public ConnectionFactory defaultConnectionFactory() {
	    CachingConnectionFactory cf = new CachingConnectionFactory();
	    cf.setAddresses(this.props.getAddresses());
	    cf.setUsername(this.props.getUsername());
	    cf.setPassword(this.props.getPassword());
	    cf.setVirtualHost(this.props.getVirtualHost());
	    cf.setConnectionTimeout(1000);
	    return cf;
	}
		
	@Bean
	public RabbitTemplate rabbitTemplate() {
	    RabbitTemplate template = new RabbitTemplate(defaultConnectionFactory());
	    RetryTemplate retryTemplate = new RetryTemplate();
	    ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
	    backOffPolicy.setInitialInterval(500);
	    backOffPolicy.setMultiplier(10.0);
	    backOffPolicy.setMaxInterval(100000);
	    retryTemplate.setBackOffPolicy(backOffPolicy);
	    template.setRetryTemplate(retryTemplate);
	    
	    template.setRecoveryCallback(recoveryCallBack);
	    
        template.setMessageConverter(messageConverter());
	    
	    return template;
	}
	
	@Bean
	public RabbitAdmin rabbitAdmin() {
		return new RabbitAdmin(defaultConnectionFactory());
		
	}
	
	@Bean
	public Jackson2JsonMessageConverter messageConverter(){
	    Jackson2JsonMessageConverter messageConverter = new Jackson2JsonMessageConverter();
	    messageConverter.setClassMapper(new DefaultJackson2JavaTypeMapper());
    
	    return messageConverter;
	}

	
	public RecoveryCallback<String> recoveryCallBack = new RecoveryCallback<String>() {
		 public String recover(RetryContext context) throws Exception {
	         //Object message = context.getLastThrowable();
	         Throwable t = context.getLastThrowable();
	         // Do something with message
	         logger.info("We will retry message: " + t.getMessage());
	         return "retry";
	     }
	};
	
	@Bean
	public TopicExchange workingExchange() {
		TopicExchange topicExchange = new TopicExchange(exchange);
		return topicExchange;		
	}

	@Bean
	public TopicExchange deadLetterExchange() {
		TopicExchange topicExchange = new TopicExchange(deadLetterExchange);
		return topicExchange;		
	}

	public String getExchange() {
		return exchange;
	}
	
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getDeadLetterExchange() {
		return deadLetterExchange;
	}

	public void setDeadLetterExchange(String deadLetterExchange) {
		this.deadLetterExchange = deadLetterExchange;
	}


}