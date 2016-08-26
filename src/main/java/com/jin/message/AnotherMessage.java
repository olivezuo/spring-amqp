package com.jin.message;

public class AnotherMessage {
	private String sport;
	private int groupNumber;
	private String teamName;
	
	public AnotherMessage() {
		super();
	}

	public AnotherMessage(String sport, int groupNumber, String teamName) {
		this.sport = sport;
		this.groupNumber = groupNumber;
		this.teamName = teamName;
	}
	
	public String getSport() {
		return sport;
	}
	public void setSport(String sport) {
		this.sport = sport;
	}
	public int getGroupNumber() {
		return groupNumber;
	}
	public void setGroupNumber(int groupNumber) {
		this.groupNumber = groupNumber;
	}
	public String getTeamName() {
		return teamName;
	}
	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	@Override
	public String toString() {
		return "AnotherMessage [sport=" + sport + ", groupNumber=" + groupNumber + ", teamName=" + teamName + "]";
	}
	

}
