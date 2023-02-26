package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

import com.example.model.MainModel;
import com.example.view.MainView;
import com.example.viewmodel.MainViewModel;

/**
 * JavaFX App
 */
public class App extends Application {

	private static Scene scene;

	@Override
	public void start(Stage stage) throws IOException {

		try {

			MainModel m = new MainModel(); // Model
			MainViewModel vm = new MainViewModel(m); // View Model

			FXMLLoader fxl = new FXMLLoader();
			InputStream is = this.getClass().getResource("/MainView.fxml").openStream();
			Object obj = fxl.load(is);
			VBox root = (VBox) obj;

			MainView view = fxl.getController(); // View
			view.setViewModel(vm);

			Scene scene = new Scene(root, 1200, 800);
			String css = this.getClass().getResource("/myStyle.css").toExternalForm();
			scene.getStylesheets().add(css);
			stage.setScene(scene);
			stage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static void setRoot(String fxml) throws IOException {
		scene.setRoot(loadFXML(fxml));
	}

	private static Parent loadFXML(String fxml) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
		return fxmlLoader.load();
	}

	public static void main(String[] args) {
		launch();
	}
}