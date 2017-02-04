package com.jin.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jin.business.service.PlainTextService;

@RestController
public class PlainTextController {

	@Autowired
	PlainTextService plainTextService;
	
	@RequestMapping("/plaintext/send")
	public void send() {
		plainTextService.send();
	}

}
