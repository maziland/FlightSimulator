package com.test.example;



import java.io.File;
import java.io.FileReader;

import com.example.model.MainModel;
import com.example.viewmodel.MainViewModel;

import com.test.TestRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.*;

import com.example.model.anomalies.SimpleAnomalyDetector;
import com.example.model.anomalies.TimeSeries;
import com.example.model.anomalies.TimeSeriesAnomalyDetector;

import javax.xml.parsers.*;


public class TestMainViewModel implements TestRunner {

    @Override
    public void Run() throws Exception {
        MainModel model1 = new MainModel();
        MainViewModel model = new MainViewModel(model1);
        if (model.getfilesize() != 2175) {
            throw new Exception("test failed - filesize is wrong");
        }


        // test validate csv
        if (model.validateCSV(new File("flightsimulator/src/main/config/flight.csv")) != true)
            throw new Exception("test - the func validateCSV is wrong");


        // test validate xml
        if (model.validateXML(new File("flightsimulator/src/main/config/config.xml")) != true)
            throw new Exception("test - the func validateXML is wrong");

        // need to test 
        //    public String getCorrelatedFeature(String attr) {
    }

    




}
