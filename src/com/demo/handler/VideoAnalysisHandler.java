package com.demo.handler;

import java.util.List;

import com.demo.entity.FrameAnalysisResult;
import com.demo.entity.NormalizedLabel;

import javafx.scene.image.Image;

public interface VideoAnalysisHandler {
	
	boolean isAnalysisContinue();
	
	FrameAnalysisResult getCurrentFrameAnalyisResult(List<NormalizedLabel> labelList);
	
	Image getFirstFrame();
}
