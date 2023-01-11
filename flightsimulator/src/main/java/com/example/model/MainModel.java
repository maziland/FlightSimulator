package com.example.model;

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
import com.example.model.anomalies.ZScoreAnomalyDetector;

import javafx.beans.property.IntegerProperty;

import javax.xml.parsers.*;

public class MainModel {

    private class CurrentAlgorithm {
        private TimeSeriesAnomalyDetector alg;
        private String name;

        public CurrentAlgorithm(TimeSeriesAnomalyDetector alg, String name) {
            this.name = name;
            this.alg = alg;
        }
    }

    public static Object HashMap;

    public final String xml_config_path = "flightsimulator/src/main/config/config.xml";
    final String csv_config_path = "flightsimulator/src/main/config/flight.csv";
    List<String> xmlColumns;
    List<String> xmlNodes;

    private CurrentAlgorithm currentAlg;
    private HashMap<String, TimeSeriesAnomalyDetector> algorithmsMap;
    private TimeSeries timeSeries;
    public FlightSimulatorConnector fsc;

    public MainModel() {
        this.algorithmsMap = new HashMap<>();
        this.fsc = new FlightSimulatorConnector();
        createDefaultAnoamalyList();
    }

    public int getfilesize() {
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(csv_config_path));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            return 0;
        }

        String line;
        int index = 0;
        try {

            while ((line = in.readLine()) != null) {
                index += 1;
            }
        } catch (Exception e) {
            return 0;
        }
        return index;
    }

    private void createDefaultAnoamalyList()
    {
        TimeSeriesAnomalyDetector simpleDetector = new SimpleAnomalyDetector();
        this.algorithmsMap.put("SimpleAnomalyDetector", simpleDetector);
        TimeSeriesAnomalyDetector zscoreDetector = new ZScoreAnomalyDetector();
        this.algorithmsMap.put("ZScoreAnomalyDetector", zscoreDetector);
        this.currentAlg = new CurrentAlgorithm(simpleDetector, "SimpleAnomalyDetector");
    }

    public void uploadAlgorithm(File file) throws Exception {
        String className = "com.example.model.anomalies." + getFileNameWithoutExtension(file.toPath().toString());
        String dirName = file.getParent().replaceAll("\\\\", "/"); // getFileDirectory(file.toPath().toString());

        URLClassLoader loader = URLClassLoader.newInstance(new URL[] { new URL("file://" + dirName) });
        Class<?> c = loader.loadClass(className);
        TimeSeriesAnomalyDetector newAlg = (TimeSeriesAnomalyDetector) c.getDeclaredConstructor().newInstance();
        algorithmsMap.put(className, newAlg);
    }

    public void changeSpeedTime(int newTime) {
        fsc.control.delay = newTime;
    }

    public void setCurrentAlgorithm(String name) {
        TimeSeriesAnomalyDetector alg = this.algorithmsMap.get(name);
        this.currentAlg.name = name;
        this.currentAlg.alg = alg;
    }

    public String getCurrentAlgorithm() {
        return this.currentAlg.name;
    }

    public List<String> getAlgorithms() {
        return new ArrayList<String>(this.algorithmsMap.keySet());
    }

    public List<String> getXmlColumns() {
        return this.xmlColumns;
    }

    public void setTimeSeries(String path) {
        this.timeSeries = new TimeSeries(path);
    }

    public HashMap<String, float[]> getTimeSeriesHashMap() {
        return this.timeSeries.map;
    }

    public String getCorrelatedFeature(String attr) {
        if (this.timeSeries.correlatedFeatures.get(attr) != null)
            return this.timeSeries.correlatedFeatures.get(attr).correlatedFeature;
        return null;
    }

    public boolean validateXML(File file) {
        /**
         *
         * This method gets a File object of a XML file and checks if it satisfies:
         * 1. The XML has `chunk` tags with a `name` sub-tag
         * If so, the function returns true and adds all the `name` tags to the
         * `xmlColumnNames` set
         * 
         */
        try {
            boolean verified = true;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("input");
            Node dnode = nList.item(0);
            Element elementA = (Element) dnode;

            // Define the wanted Nodes list
            ArrayList<String> wantedNodes = new ArrayList<>(Arrays.asList("/controls/flight/aileron[0]",
                    "/controls/flight/elevator", "/controls/flight/rudder", "/controls/flight/flaps",
                    "/controls/flight/slats", "/controls/flight/speedbrake", "/controls/engines/engine[0]/throttle",
                    "/controls/engines/engine[1]/throttle", "/controls/hydraulic/system[0]/engine-pump",
                    "/controls/hydraulic/system[1]/engine-pump", "/controls/hydraulic/system[0]/electric-pump",
                    "/controls/hydraulic/system[1]/electric-pump", "/controls/electric/external-power",
                    "/controls/electric/APU-generator", "/position/latitude-deg", "/position/longitude-deg",
                    "/position/altitude-ft", "/orientation/roll-deg", "/orientation/pitch-deg",
                    "/orientation/heading-deg", "/orientation/side-slip-deg", "/velocities/airspeed-kt",
                    "/velocities/glideslope", "/velocities/vertical-speed-fps",
                    "/instrumentation/airspeed-indicator/indicated-speed-kt",
                    "/instrumentation/altimeter/indicated-altitude-ft", "/instrumentation/altimeter/pressure-alt-ft",
                    "/instrumentation/attitude-indicator/indicated-pitch-deg",
                    "/instrumentation/attitude-indicator/indicated-roll-deg",
                    "/instrumentation/attitude-indicator/internal-pitch-deg",
                    "/instrumentation/attitude-indicator/internal-roll-deg",
                    "/instrumentation/encoder/indicated-altitude-ft", "/instrumentation/encoder/pressure-alt-ft",
                    "/instrumentation/gps/indicated-altitude-ft", "/instrumentation/gps/indicated-ground-speed-kt",
                    "/instrumentation/gps/indicated-vertical-speed",
                    "/instrumentation/heading-indicator/indicated-heading-deg",
                    "/instrumentation/magnetic-compass/indicated-heading-deg",
                    "/instrumentation/slip-skid-ball/indicated-slip-skid",
                    "/instrumentation/turn-indicator/indicated-turn-rate",
                    "/instrumentation/vertical-speed-indicator/indicated-speed-fpm", "/engines/engine/rpm"));

            // Configs from XML file, fill while reading and parsing the xml
            xmlColumns = new ArrayList<String>();
            xmlNodes = new ArrayList<String>();
            String nodeTag, nameTag, typeTag;

            for (int i = 0; i < elementA.getElementsByTagName("chunk").getLength(); i++) {

                // Get relevant tags from INPUT tag of the simulator
                Element eElement = (Element) elementA.getElementsByTagName("chunk").item(i);
                nodeTag = eElement.getElementsByTagName("node").item(0).getTextContent();
                nameTag = eElement.getElementsByTagName("name").item(0).getTextContent();
                typeTag = eElement.getElementsByTagName("type").item(0).getTextContent();

                if (nameTag == "" || nodeTag == "" || typeTag == "")
                    verified = false;

                xmlColumns.add(nameTag);
                xmlNodes.add(nodeTag);

            }
            if (xmlNodes.equals(wantedNodes)) {
                verified = true;

            } else
                verified = false;

            return verified;

        } catch (

        Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean validateCSV(File file) throws IOException {
        List<String> csvColumns = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String header = br.readLine();
        if (header != null) {
            csvColumns = Arrays.asList(header.split(","));
        }
        br.close();

        // Check if uniqueColumnsSet is equal to the Settings file columns
        if (csvColumns.equals(this.xmlColumns)) {
            return true;
        } else {
            return false;
        }
    }

    public void runSimulator() throws IOException {
        /*
         * The function connects to the simulator and sends the flight data with the
         * given delay
         */
        if (fsc.control.state == "") {
            fsc.executeFlight();
        }
        fsc.control.state = fsc.run;
    }

    public void pauseSimulator() {
        fsc.control.state = fsc.pause;
    }

    public void stopSimulator() {
        fsc.control.state = fsc.stop;
    }

    public void forwardSimulator() {
        fsc.control.state = fsc.forward;
    }

    public void backwardSimulator() {
        fsc.control.state = fsc.backward;
    }

    public void tostartSimulator() {
        fsc.control.state = fsc.tostart;
    }

    public void toendSimulator() {
        fsc.control.state = fsc.toend;
    }

    public void changeTime(int index) {
        // fsc.control.index = index;
        fsc.control.currentTimeStep.set(index);
    }

    public float[] stabilizers_pose(IntegerProperty time)
    {
        // returns a float[] with all data in line of time
        
        float[] pos = new float[10];
        int index = time.get();
        pos[0] = this.timeSeries.map.get(xmlColumns.get(xmlNodes.indexOf("/controls/flight/aileron[0]")))[index]; 
        pos[1] = this.timeSeries.map.get(xmlColumns.get(xmlNodes.indexOf("/controls/flight/elevator")))[index];
        pos[2] = this.timeSeries.map.get(xmlColumns.get(xmlNodes.indexOf("/controls/flight/rudder")))[index];
        pos[3] = this.timeSeries.map.get(xmlColumns.get(xmlNodes.indexOf("/controls/engines/engine[0]/throttle")))[index];
        pos[4] = this.timeSeries.map.get(xmlColumns.get(xmlNodes.indexOf("/position/latitude-deg")))[index];
        pos[5] = this.timeSeries.map.get(xmlColumns.get(xmlNodes.indexOf("/position/longitude-deg")))[index];
        pos[6] = this.timeSeries.map.get(xmlColumns.get(xmlNodes.indexOf("/position/altitude-ft")))[index];
        pos[7] = this.timeSeries.map.get(xmlColumns.get(xmlNodes.indexOf("/orientation/roll-deg")))[index];
        pos[8] = this.timeSeries.map.get(xmlColumns.get(xmlNodes.indexOf("/orientation/pitch-deg")))[index];
        pos[9] = this.timeSeries.map.get(xmlColumns.get(xmlNodes.indexOf("/orientation/heading-deg")))[index];
        
        return pos;
    }

    public static String getFileNameWithoutExtension(String fileName) {
        String[] splittedFileName = fileName.split("\\\\");
        String simpleFileName = splittedFileName[splittedFileName.length - 1];
        return simpleFileName.substring(0, simpleFileName.lastIndexOf('.'));
    }

}