package com.example.viewmodel;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.List;

import com.example.model.MainModel;
import com.example.util.Line;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class MainViewModel {

    MainModel m;
    final String xml_config_path = FileSystems.getDefault().getPath(".").toString() + "\\" + "config.xml";
    final String csv_config_path = FileSystems.getDefault().getPath(".").toString() + "\\" + "flight.csv";
    public ListProperty<String> attributesListProperty, algorithmsListProperty;
    public StringProperty selectedAttribute, selectedAlgorithm;
    public MapProperty<String, float[]> hashMap;
    public IntegerProperty currentTimeStepProperty;

    public MainViewModel(MainModel m) {
        this.m = m;
        initCurrentTimeStepProperty();
        initAttributesListProperty();
        initAlgorithmsListProperty();
    }

    public void initCurrentTimeStepProperty() {
        this.currentTimeStepProperty = new SimpleIntegerProperty();
        this.currentTimeStepProperty.bind(this.m.fsc.control.currentTimeStep);
    }

    public int getfilesize() {
        return this.m.getfilesize();
    }

    public List<Float> getZscoresForFeature(String feature) {
        return this.m.getZscoresForFeature(feature);
    }

    public void updateHashMap() {
        if (this.hashMap == null)
            this.hashMap = new SimpleMapProperty<>(FXCollections.observableHashMap());
        ObservableMap<String, float[]> map = FXCollections.observableHashMap();
        map.putAll(this.m.getTimeSeriesHashMap());
        this.hashMap.set(map);
    }

    public String getCorrelatedFeature(String attr) {
        return this.m.getCorrelatedFeature(attr);
    }

    public Line getCorrelatedLinearRegression(String selected, String correlated) {
        return this.m.getCorrelatedLinearRegression(selected, correlated);
    }

    public void initAttributesListProperty() {
        this.attributesListProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.selectedAttribute = new SimpleStringProperty();
        this.selectedAttribute.addListener((o, ov, nv) -> System.out.println("Selected Attribute Changed"));
    }

    public void initAlgorithmsListProperty() {
        this.selectedAlgorithm = new SimpleStringProperty();
        this.algorithmsListProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
        ObservableList<String> algorithmsList = FXCollections.observableArrayList(this.m.getAlgorithms());
        algorithmsList.add("Upload Algorithm...");
        this.algorithmsListProperty.set(algorithmsList);
        this.selectedAlgorithm.addListener((o, ov, nv) -> {
            this.m.setCurrentAlgorithm(nv);
            System.out.println("Selected Algorithm Changed");
        });
    }

    public boolean validateXML(File file) {
        if (this.m.validateXML(file)) {
            ObservableList<String> observableList = FXCollections.observableArrayList(this.m.getXmlColumns());
            this.attributesListProperty.set(observableList);
            return true;
        }
        return false;
    }

    public boolean validateCSV(File file) throws IOException {
        if (this.m.validateCSV(file)) {
            this.m.setTimeSeries(csv_config_path);
            updateHashMap();
            return true;
        } else
            return false;
    }

    public List<Integer> detectAnomalies(String description) {
        return this.m.detectAnomalies(description);
    }

    public void uploadAlgorithm(File file) throws Exception {
        this.m.uploadAlgorithm(file);
        ObservableList<String> algorithmsList = FXCollections.observableArrayList(this.m.getAlgorithms());
        algorithmsList.add("Upload Algorithm...");
        this.algorithmsListProperty.set(algorithmsList);
    }

    public void timeSliderHandler(int currentTime) {
        this.m.changeTime(currentTime);
    }

    public void setSpeedTime(int newTime) {
        this.m.changeSpeedTime(newTime);
    }

    public float[] joyStickPos() {
        return this.m.stabilizers_pose();
    }

    public void mediaCommand(String buttonId) {
        try {
            switch (buttonId) {
                case ("BackToStartButton"):
                    this.m.tostartSimulator();
                    break;
                case ("BackwardButton"):
                    this.m.backwardSimulator();
                    break;
                case ("stopButton"):
                    this.m.stopSimulator();
                    break;
                case ("PlayButton"):
                    this.m.runSimulator();
                    break;
                case ("PauseButton"):
                    this.m.pauseSimulator();
                    break;
                case ("ForwardButton"):
                    this.m.forwardSimulator();
                    break;
                case ("endButton"):
                    this.m.toendSimulator();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }

    }

}