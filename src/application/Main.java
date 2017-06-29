package application;
	
import com.demo.controller.MainController;
import com.demo.handler.VideoAnalysisHandlerImpl;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
			Scene scene = new Scene(root,1030.0,637.0);
			primaryStage.initStyle(StageStyle.UTILITY);  
			primaryStage.setScene(scene);  
			primaryStage.setTitle("demo");
			primaryStage.show();
	        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	            @Override
	            public void handle(WindowEvent event) {
	            	VideoAnalysisHandlerImpl.executor.shutdownNow();
	            }
	        });
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
