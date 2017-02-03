package com.jin.business.message;

public class SimpleMessage {
	private String name;
	private String gender;
	private String address;

	
	public SimpleMessage() {
		super();
	}

	public SimpleMessage(String name, String gender, String address) {
		super();
		this.name = name;
		this.gender = gender;
		this.address = address;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "SimpleMessage [name=" + name + ", gender=" + gender + ", address=" + address + "]";
	}

}
