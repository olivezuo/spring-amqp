package com.jin.queue.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@Document
public class FailedMessage {
	
	@Id
	private String id;
	
	@Field
	private String messageType;
	
	@Field
	private String message;
	
	@Field
	private String routingKey;
	
	@Field
	private String exchange;
	
	@Field
	private String errorDetails;
	
	@Field
	private String failType;
	
	@Field
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date dateAdded;

	@Field
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date dateRetried;
	
	@Field
	@Indexed
	private boolean retried;
		
	public FailedMessage(String messageType, String message, String routingKey, String exchange, String errorDetails, String failType) {
		super();
		this.messageType = messageType;
		this.message = message;
		this.routingKey = routingKey;
		this.exchange = exchange;
		this.errorDetails = errorDetails;
		this.failType = failType;
		this.dateAdded = new Date();
		this.retried = false;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			String dateInString = "01-01-2000 00:00:00";
			this.dateRetried = sdf.parse(dateInString);
		} catch (ParseException e) {
			
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getRoutingKey() {
		return routingKey;
	}

	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getErrorDetails() {
		return errorDetails;
	}

	public void setErrorDetails(String errorDetails) {
		this.errorDetails = errorDetails;
	}
	
	public String getFailType() {
		return failType;
	}

	public void setFailType(String failType) {
		this.failType = failType;
	}

	public Date getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(Date dateAdded) {
		this.dateAdded = dateAdded;
	}

	public Date getDateRetried() {
		return dateRetried;
	}

	public void setDateRetried(Date dateRetried) {
		this.dateRetried = dateRetried;
	}

	public boolean isRetried() {
		return retried;
	}

	public void setRetried(boolean retried) {
		this.retried = retried;
	}

	@Override
	public String toString() {
		return "FailedMessage [id=" + id + ", messageType=" + messageType + ", message=" + message + ", routingKey="
				+ routingKey + ", exchange=" + exchange + ", errorDetails=" + errorDetails + "]";
	}
}
