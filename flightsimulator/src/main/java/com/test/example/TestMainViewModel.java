package com.test.example;

import java.io.File;

import com.example.model.MainModel;
import java.nio.file.FileSystems;
import com.example.viewmodel.MainViewModel;

import com.test.TestRunner;

public class TestMainViewModel implements TestRunner {

    @Override
    public void Run() throws Exception {
        MainModel model1 = new MainModel();
        MainViewModel model = new MainViewModel(model1);
        if (model.getfilesize() != 2174) {
            throw new Exception("test failed - filesize is wrong");
        }

        // test validate csv
        if (model.validateCSV(new File(FileSystems.getDefault().getPath(".").toString() + "\\" + "flight.csv")) != true)
            throw new Exception("test - the func validateCSV is wrong");

        // test validate xml
        if (model.validateXML(new File(FileSystems.getDefault().getPath(".").toString() + "\\" + "config.xml")) != true)
            throw new Exception("test - the func validateXML is wrong");

        // need to test
        // public String getCorrelatedFeature(String attr) {
    }

}
