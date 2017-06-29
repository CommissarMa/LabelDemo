package com.demo.task;

import java.util.List;

import com.demo.controller.MainController;
import com.demo.entity.FrameAnalysisResult;
import com.demo.entity.NormalizedLabel;
import com.demo.handler.VideoAnalysisHandler;
import com.demo.handler.VideoAnalysisHandlerImpl;

import javafx.scene.image.Image;

public class VideoAnalysisTask extends Thread {
	
	private VideoAnalysisHandler handler;
	
	private volatile boolean taskContinue = true;
	
	private MainController mainController;
	
	public VideoAnalysisTask(String videoPath, MainController mainController) {
		handler = new VideoAnalysisHandlerImpl(videoPath);
		this.mainController = mainController;
	}
	
	public Image getFirstFrame() {
		return handler.getFirstFrame();
	}
	
	@Override
	public void run() {
		mainController.showText("=====开始分析=====");
		while(isInterrupted()) {
			List<NormalizedLabel> labelList = mainController.getLabelList();
			FrameAnalysisResult result = handler.getCurrentFrameAnalyisResult(labelList);
			mainController.showImage(result.getFrame());
			mainController.showText(result.toString());
		}
		
		mainController.showText("=====分析结束=====");
	}

	@Override
	public boolean isInterrupted() {
		return taskContinue && handler.isAnalysisContinue();
	}
	
	@Override
	public void interrupt() {
		taskContinue = false;
	}

}
