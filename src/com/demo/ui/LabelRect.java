package com.demo.ui;

import java.awt.MouseInfo;
import java.awt.Point;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public class LabelRect{
	
	private Pane labelPane;
	
	private Rectangle editLabel;
	
	private Point2D rectStartLocalPoint;
	
	public LabelRect(Pane parentPane) {
		this.labelPane = parentPane;
	}
	
	public void startEdit() {
		initRect();
		saveRectStartPoint();
	}
	
	public void update() {
		Point2D rectEndPoint = getCurrentRectEndPointInLabelPanel();
		updateCurrentRect(rectEndPoint);
	}
	
	public void endEdit() {
		setMouseClickHandlerForCurrentRect();
	}
	
	private void initRect() {
		editLabel = new Rectangle(0, 0);
		editLabel.setStyle("-fx-fill:#1f93ffaa");
		labelPane.getChildren().add(editLabel);
		
	}
	
	private void saveRectStartPoint() {
		Point currentMouseLocation = MouseInfo.getPointerInfo().getLocation();
		rectStartLocalPoint = labelPane.screenToLocal(currentMouseLocation.getX(), currentMouseLocation.getY());
		
	}
	
	private Point2D getCurrentRectEndPointInLabelPanel() {
		Point currentMouseLocation = MouseInfo.getPointerInfo().getLocation();
		Point2D mouseLocalPosition = labelPane.screenToLocal(currentMouseLocation.getX(), currentMouseLocation.getY());
		return validMouseLocalPosition(mouseLocalPosition);

	}
	
	private Point2D validMouseLocalPosition(Point2D mouseLocalPosition) {
		double rectEndLocalPointX = mouseLocalPosition.getX() > 0 ?  mouseLocalPosition.getX() : 0;
		rectEndLocalPointX = rectEndLocalPointX < labelPane.getWidth()? rectEndLocalPointX : labelPane.getWidth();
		
		double rectEndLocalPointY = mouseLocalPosition.getY() > 0 ?  mouseLocalPosition.getY() : 0;
		rectEndLocalPointY = rectEndLocalPointY < labelPane.getHeight() ? rectEndLocalPointY : labelPane.getHeight();
		
		return new Point2D(rectEndLocalPointX, rectEndLocalPointY);
	}
	
	private void updateCurrentRect(Point2D rectEndLocalPoint) {
		double rectHeight = Math.abs(rectStartLocalPoint.getY()-rectEndLocalPoint.getY());
		double rectWidth = Math.abs(rectStartLocalPoint.getX()-rectEndLocalPoint.getX());
		double rectXPosition = rectStartLocalPoint.getX()<rectEndLocalPoint.getX()?rectStartLocalPoint.getX() : rectEndLocalPoint.getX();
		double rectYPosition = rectStartLocalPoint.getY()<rectEndLocalPoint.getY()?rectStartLocalPoint.getY() : rectEndLocalPoint.getY();
		editLabel.setHeight(rectHeight);
		editLabel.setWidth(rectWidth);	
		editLabel.setTranslateX(rectXPosition);
		editLabel.setTranslateY(rectYPosition);
	}
	
	private void setMouseClickHandlerForCurrentRect() {
		editLabel.setOnMouseClicked(new EventHandler<MouseEvent>(){
			
			@Override
			public void handle(MouseEvent event) {
				labelPane.getChildren().remove(event.getTarget());
			}});
	}
}
