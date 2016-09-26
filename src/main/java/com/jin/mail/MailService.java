package com.jin.mail;

import javax.activation.DataSource;
import javax.mail.MessagingException;

public interface MailService {
	public void send(String from, String to, String replyTo, String subject, String text, String fileName, DataSource ds) throws MessagingException;
	
	public void send(String from, String to, String replyTo, String subject, String text, String fileName, String content, String mimeType) throws MessagingException;
	
	public void send(String from, String to, String replyTo, String subject, String text) throws MessagingException;
	
	public void send(String from, String to, String subject, String text) throws MessagingException;

}
