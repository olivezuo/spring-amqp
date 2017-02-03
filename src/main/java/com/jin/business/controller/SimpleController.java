package com.jin.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jin.business.consumer.SimpleConsumer;
import com.jin.business.service.SimpleService;

@RestController
public class SimpleController {
	
	@Autowired
	private SimpleService simpleService;
	
	@Autowired
	private SimpleConsumer simpleConsumer;

	
	@RequestMapping("/simple/send")
	public void send() {
		simpleService.send();
	}
	
	@RequestMapping("/simple/stop")	
	public void stop() {
		simpleConsumer.stop();
	}
	
	@RequestMapping("/simple/start")	
	public void start() {
		simpleConsumer.start();
	}
	
	@RequestMapping("/simple/concurrent/{concurrentConsumers}")
	public void concurrentConsumer (@PathVariable("concurrentConsumers") int concurrentConsumers) {
		
		simpleConsumer.setConcurrentConsumer(concurrentConsumers);
	}



}
