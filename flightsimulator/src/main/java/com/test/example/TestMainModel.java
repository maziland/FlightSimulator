package com.test.example;

import java.io.File;

import java.nio.file.FileSystems;
import com.example.model.MainModel;

import com.test.TestRunner;

public class TestMainModel implements TestRunner {

    @Override
    public void Run() throws Exception {
        MainModel model = new MainModel();
        if (model.getfilesize() != 2174) {
            throw new Exception("test failed - filesize is wrong");
        }

        model.setTimeSeries(FileSystems.getDefault().getPath(".").toString() + "\\" + "config.xml");
        if (model.getTimeSeriesHashMap() != MainModel.HashMap)
            throw new Exception("test - the time is wrong");

        if (model.getXmlColumns() != MainModel.HashMap)
            throw new Exception("test - the func getXmlColumns is wrong");

        // test validate csv
        if (model.validateCSV(new File(FileSystems.getDefault().getPath(".").toString() + "\\" + "flight.csv")) != true)
            throw new Exception("test - the func validateCSV is wrong");

        // test validate xml
        if (model.validateXML(new File(FileSystems.getDefault().getPath(".").toString() + "\\" + "config.xml")) != true)
            throw new Exception("test - the func validateXML is wrong");

    }

}
