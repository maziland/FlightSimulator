package com.example.viewmodel;

import java.io.File;
import java.io.IOException;
import com.example.model.MainModel;

import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class MainViewModel {

    MainModel m;
    final String xml_config_path = "flightsimulator/src/main/config/config.xml";
    final String csv_config_path = "flightsimulator/src/main/config/flight.csv";
    public ListProperty<String> attributesListProperty, algorithmsListProperty;
    public StringProperty selectedAttribute, selectedAlgorithm;
    public MapProperty<String, float[]> hashMap;

    public MainViewModel(MainModel m) {
        this.m = m;
        initAttributesListProperty();
        initAlgorithmsListProperty();
    }

    public int getfilesize()
    {
        return this.m.getfilesize();
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
        this.selectedAlgorithm.addListener((o, ov, nv) -> System.out.println("Selected Algorithm Changed"));
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

    public void uploadAlgorithm(File file) throws Exception {
        this.m.uploadAlgorithm(file);
        ObservableList<String> algorithmsList = FXCollections.observableArrayList(this.m.getAlgorithms());
        algorithmsList.add("Upload Algorithm...");
        this.algorithmsListProperty.set(algorithmsList);
    }

    public void timeSliderHandler(int currentTime)
    {
        this.m.changeTime(currentTime);
    }

    public void setSpeedTime(int newTime)
    {
        this.m.changeSpeedTime(newTime);
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