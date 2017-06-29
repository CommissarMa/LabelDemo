package com.demo.entity;

public enum WindowStatus {
	
	OPEN("��"),
	CLOSE("��");
	
	private String value;
	
	private WindowStatus(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
}
