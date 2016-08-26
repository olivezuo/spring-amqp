package com.jin.queue;

public class QueueMessage <T> {

	private T payload;
	public String type;


	public T getPayload() {
		return payload;
	}

	public void setPayload(T payload) {
		setPayload(payload, false);
	}
	
	public void setPayload(T payload, boolean updateType) {
		this.payload = payload;
		if (true == updateType) {
			this.type = payload.getClass().getName();
		}
	}
	
	public QueueMessage(T payload) {
		setPayload(payload, true);
	}

	public QueueMessage() {
		
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


}
