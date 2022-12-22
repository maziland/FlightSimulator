package com.example.viewmodel;

import java.io.File;
import java.io.IOException;
import com.example.model.MainModel;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MainViewModel {

    MainModel m;
    public ListProperty<String> attributesListProperty;

    public MainViewModel(MainModel m) {
        this.m = m;
        this.attributesListProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
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
        return this.m.validateCSV(file);
    }

}