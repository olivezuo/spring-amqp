package com.jin.mail;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService{
	private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);
	
	@Autowired
	private JavaMailSender javaMailSender;
		
	@Override
	public void send(String from, String to, String replyTo, String subject, String text, String fileName, DataSource ds) throws MessagingException {
		
		MimeMessage mail = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, true);
            helper.setFrom(from);
            
            String[] tos = to.split("\\s*,\\s*");
            helper.setTo(tos);
            helper.setReplyTo(replyTo);
            helper.setSubject(subject);
            if(null != text){
            	helper.setText(text);
            }
            
            if (null != ds && null != fileName) {
            	helper.addAttachment(fileName, ds);
            }
            
            javaMailSender.send(mail);
	}
	
	@Override
	public void send(String from, String to, String replyTo, String subject, String text, String fileName, String content, String mimeType) throws MessagingException {
		if (fileName != null && content != null && mimeType!= null){
			DataSource ds = new ByteArrayDataSource(content.getBytes(), mimeType);
			this.send(from, to, replyTo, subject, text, fileName, ds);
		} else {
			this.send(from, to, replyTo, subject, text, null, null);
		}
		
	}
	
	@Override
	public void send(String from, String to, String replyTo, String subject, String text) throws MessagingException {
		this.send(from, to, replyTo, subject, text, null, null);
		
	}

	@Override
	public void send(String from, String to, String subject, String text) throws MessagingException {
		this.send(from, to, from, subject, text, null, null);
		
	}

		
}
