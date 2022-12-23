package com.example.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javafx.fxml.FXML;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;

public class MainModel {

    final String xml_config_path = "flightsimulator/src/main/config/config.xml";
    final String csv_config_path = "flightsimulator/src/main/config/flight.csv";
    List<String> xmlColumns;
    List<String> xmlNodes;
    public ListProperty<String> attributesListProperty;
    public FlightSimulatorConnector fsc;

    public MainModel() {
        this.attributesListProperty = new SimpleListProperty<>();
        this.fsc = new FlightSimulatorConnector();
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

}