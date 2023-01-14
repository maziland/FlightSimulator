package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

import com.example.model.MainModel;
import com.example.view.MainView;
import com.example.viewmodel.MainViewModel;
import com.example.model.anomalies.ZScoreAnomalyDetector;
import com.example.model.anomalies.TimeSeries;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        ZScoreAnomalyDetector zs = new ZScoreAnomalyDetector();
        TimeSeries ts = new TimeSeries("flightsimulator/src/main/config/flight.csv");
        zs.learnNormal(ts);
        
        
        try {

            MainModel m = new MainModel(); // Model
            MainViewModel vm = new MainViewModel(m); // View Model

            FXMLLoader fxl = new FXMLLoader();
            // BorderPane root = (BorderPane)
            // fxl.load(getClass().getResource("MainWindow.fxml").openStream());
            VBox root = (VBox) fxl.load(this.getClass().getResource("MainView.fxml").openStream());

            MainView view = fxl.getController(); // View
            view.setViewModel(vm);

            Scene scene = new Scene(root, 1200, 800);
            String css = this.getClass().getResource("myStyle.css").toExternalForm();
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