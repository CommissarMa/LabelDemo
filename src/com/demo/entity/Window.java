package com.demo.entity;

public class Window implements Comparable<Window>{
	
	private int no;
	
	private WindowStatus status;
	
	public Window(int no, WindowStatus status) {
		this.no = no;
		this.status = status;
	}
	
	@Override
	public String toString() {
		return "´°" + no + ":" + status.getValue();
	}

	@Override
	public int compareTo(Window o) {
		// TODO Auto-generated method stub
		return no - o.no;
	}
}
