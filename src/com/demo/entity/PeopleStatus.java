package com.demo.entity;

public enum PeopleStatus {
	
	EXIST("����"),
	NOT_EXIST("����");
	
	private String value;
	
	private PeopleStatus(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
}
