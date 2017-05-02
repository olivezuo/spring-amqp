package com.jin.queue.config;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@ConfigurationProperties(prefix = "com.jin.async")
public class AsyncConfig implements AsyncConfigurer {
	private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);
	
	private int corePoolSize = 7;
	private int maxPoolSize = 50;
	private int queueCapacity = 100;
	private String threadNamePrefix = "Jin-Exec-";
	
	
    @Override
    public Executor getAsyncExecutor() {
    	logger.info("We are about to create new ThreadPoolTaskExecutor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(this.corePoolSize);
        executor.setMaxPoolSize(this.maxPoolSize);
        executor.setQueueCapacity(this.queueCapacity);
        executor.setThreadNamePrefix(this.threadNamePrefix);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler () {
			@Override
			public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
				StringBuilder log = new StringBuilder("Exception message - " + throwable.getMessage() + "; ");
				
				log.append("Class name - " + method.getDeclaringClass().getName() + "; ");
				log.append("Method name - " + method.getName() + "\n");
				for (Object param : obj) {
					log.append("Parameter value - " + param + "\n");
				}
				logger.info(log.toString());
			}
        };
    }

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public String getThreadNamePrefix() {
		return threadNamePrefix;
	}

	public void setThreadNamePrefix(String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix;
	}
    
}