package com.example.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.*;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javax.xml.parsers.*;

public class MainModel {

    List<String> xmlColumns;
    List<String> xmlNodes;
    public ListProperty<String> attributesListProperty;
    public DoubleProperty dp;

    public MainModel() {
        this.attributesListProperty = new SimpleListProperty<>();
        this.dp = new SimpleDoubleProperty(4.0);
        this.dp.set(5.0);
        this.attributesListProperty.addListener((o, ov, nv) -> System.out.println("Changed on model"));
        ChangeListener<ObservableList<String>> changeListener = new ChangeListener<ObservableList<String>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<String>> observable,
                    ObservableList<String> oldValue, ObservableList<String> newValue) {
                System.out.println("Changed on m");
            }
        };
        this.attributesListProperty.addListener(changeListener);
    }

    public List<String> getXmlColumns() {
        return this.xmlColumns;
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
            this.dp.set(5.0);
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

}