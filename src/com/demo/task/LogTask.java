package com.demo.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.animation.AnimationTimer;
import javafx.scene.control.TextField;

public class LogTask extends AnimationTimer{
	
	private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<String>();
	
	private TextField textField;
	
	public LogTask(TextField textField) {
		this.textField = textField;
	}
	
	
	public void showMessage(String message) {
		try {
			messageQueue.put(message);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void handle(long now) {
        List<String> messages = new ArrayList<>();
        int receivedMessageNumber = messageQueue.drainTo(messages);
        if (receivedMessageNumber == 0 ) {
        	return;
        }
        textField.setText(messages.get(receivedMessageNumber-1));
	}
}
