package com.demo.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.demo.entity.NormalizedLabel;
import com.demo.task.LogTask;
import com.demo.task.VideoAnalysisTask;
import com.demo.ui.LabelRect;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
public class MainController {

	@FXML
	private ImageView labelImageView;
	
	@FXML
	private ImageView originImageView;
	
	@FXML
	private Pane labelPane;
	
	@FXML
	private TextField textField;
	
	@FXML
	private Pane inputPane;
	
	@FXML
	private Pane editPane;
	
	@FXML
	private Pane runPane;
	
	private LabelRect editLabel;
	
	private boolean editStatus = false;
	
	private String filePath = null;
	
	private VideoAnalysisTask task;
	
	private LogTask logTask = null;
	
	public void showImage(Image image) {
		labelImageView.setImage(image);
		originImageView.setImage(image);
	}
	
	public void showText(String result) {
		if(result == null || result.isEmpty()) {
			return;
		}
		logTask.showMessage(result);
	}
	
	@FXML
	private void onLabelPaneMousePressed() {
		if(editStatus) {
			editLabel = new LabelRect(labelPane);
			editLabel.startEdit();
		}

	}
	
	@FXML
	private void onLabelPaneMouseReleased() {
		if(editStatus) {
			editLabel.endEdit();
		}

	}
	
	@FXML
	private void onLabelPaneMouseDragged() {
		if(editStatus) {
			editLabel.update();
		}

	}
	
	@FXML
	private void onOpenButtonMouseClicked() {
		
		filePath = getAnalysisVideoPath();
		if(filePath == null) {
			return;
		}
		showEditPane();
		task = createVideoAnalysisTask(filePath);
		Image image = task.getFirstFrame();
		showImage(image);
		labelPane.setPrefHeight(labelImageView.getFitWidth()/image.getWidth()*image.getHeight());
		editStatus = true;
		
		if(logTask == null) {
			logTask = new LogTask(textField);
			logTask.start();
		}

	}
	
	@FXML
	private void onStartButtonMouseClicked() {
		showRunPane();
		task.start();
		editStatus = false;
	}
	
	@FXML
	private void onBackButtonMouseClicked() {
		showInputPane();
		labelPane.getChildren().clear();
		originImageView.setImage(null);
		labelImageView.setImage(null);
		task = null;
		editStatus = false;
	}
	
	@FXML
	private void onCancelButtonMouseClicked() {
		
		task.interrupt();
		editStatus = false;
		showInputPane();
		labelPane.getChildren().clear();
		showImage(null);
		
	}
	
	private String getAnalysisVideoPath() {
		FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Video", "*.avi")
            );
		File file = fileChooser.showOpenDialog(null);
		return file.getAbsolutePath();
	}
	
	private VideoAnalysisTask createVideoAnalysisTask(String filePath) {
		return  new VideoAnalysisTask(filePath, this);
	}
	
	private void hideAllPane() {
		inputPane.setVisible(false);
		runPane.setVisible(false);
		editPane.setVisible(false);
	}
	
	public void showInputPane() {
		hideAllPane();
		inputPane.setVisible(true);
	}
	
	public void showRunPane() {
		hideAllPane();
		runPane.setVisible(true);
	}
	
	public void showEditPane() {
		hideAllPane();
		editPane.setVisible(true);
	}
	
	public List<NormalizedLabel> getLabelList() {
		double paneHeight = labelPane.getHeight();
		double paneWidth = labelPane.getWidth();
		List<NormalizedLabel> labelList = new ArrayList<NormalizedLabel>();
		for(Node node : labelPane.getChildren()) {
			Rectangle label = (Rectangle) node;
			double labelX = label.getTranslateX();
			double labelY = label.getTranslateY();
			double labelWidth = label.getWidth();
			double labelHeight = label.getHeight();
			if(labelWidth == 0.0 || labelHeight == 0.0) {
				continue;
			}
			NormalizedLabel normalizedLabel = new NormalizedLabel();
			normalizedLabel.setX(labelX/paneWidth);
			normalizedLabel.setY(labelY/paneHeight);
			normalizedLabel.setHeight(labelHeight/paneHeight);
			normalizedLabel.setWidth(labelWidth/paneWidth);
			labelList.add(normalizedLabel);
		}
		return labelList;
	}
	
}
