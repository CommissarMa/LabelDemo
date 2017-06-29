package com.demo.entity;

import java.util.List;

import com.demo.util.TimeUtil;

import javafx.scene.image.Image;

public class FrameAnalysisResult {
	
	private Image frame;
	
	private PeopleStatus people;
	
	private List<Window> windowList;
	
	private long time;
	
	public FrameAnalysisResult(Image frame, PeopleStatus people, List<Window> windowList, long time) { 
		this.frame = frame;
		this.people = people;
		this.windowList = windowList;
		this.time = time;
	}
	
	public Image getFrame() {
		return this.frame;
	}

	public PeopleStatus getPeople() {
		return this.people;
	}

	public List<Window> getWindowList() {
		return this.windowList;
	}

	public long getTime() {
		return this.time;
	}
	
	@Override
	public String toString() {
		StringBuilder strb = new StringBuilder();
		String analysisTime =  TimeUtil.millionSecondToClockTime(time);
		strb.append(analysisTime).append(" ");
		strb.append(people.getValue()).append(" ");
		for(Window window : windowList) {
			strb.append(window.toString()).append(" ");
		}
		return strb.toString();
	}

}
