package com.jin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jin.consumer.AnotherConsumer;
import com.jin.service.AnotherService;

@RestController
public class AnotherController {

	@Autowired
	private AnotherService anotherService;
	
	@Autowired
	private AnotherConsumer anotherConsumer;

	
	@RequestMapping("/another/send")
	public void send() {
		anotherService.send();
	}
	
	@RequestMapping("/another/stop")	
	public void stop() {
		anotherConsumer.stop();
	}
	
	@RequestMapping("/another/start")	
	public void start() {
		anotherConsumer.start();
	}

}
