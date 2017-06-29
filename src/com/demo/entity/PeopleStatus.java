package com.demo.entity;

public enum PeopleStatus {
	
	EXIST("有人"),
	NOT_EXIST("无人");
	
	private String value;
	
	private PeopleStatus(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
}
