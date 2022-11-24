package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;

public class FileHandler {

    // final String xml_config_path = "src/main/config/config.xml";

    // After pulling from git there are 2 Dirs named flightSimulator, a little
    // patch:
    final String xml_config_path = "flightsimulator/src/main/config/config.xml";
    HashSet<String> xmlColumnsNames;

    @FXML
    private void uploadXML() throws IOException {

        // Setting a file chooser of XML files only
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        chooser.setTitle("Choose XML file");
        chooser.getExtensionFilters().add(extFilter);
        File file = chooser.showOpenDialog(null);

        if (file == null)
            return;

        boolean validated = validateXML(file);

        // Save file
        if (validated) {
            Files.copy(file.toPath(), (new File(xml_config_path)).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        // Show errors
        else {

        }
    }

    private boolean validateXML(File file) {
        try {
            boolean flag = true;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            Document org = dBuilder.parse(new File(xml_config_path)); //Add manualy the path to the xml
            org.getDocumentElement().normalize();
            doc.getDocumentElement().normalize(); 
            NodeList nList = doc.getElementsByTagName("generic");
            NodeList oList = org.getElementsByTagName("generic");
            Set<String> names = new HashSet<String>();
            Node dnode = nList.item(0);  
            Node onode = oList.item(0);  
            Element elementA = (Element) dnode;
            Element elementB = (Element) onode;
            
            for (int i = 0; i < elementA.getElementsByTagName("chunk").getLength(); i++) {
               
                    // Makes sure all elements are present
                    Element eElement = (Element) elementA.getElementsByTagName("chunk").item(i);
                    Element oElement = (Element) elementB.getElementsByTagName("chunk").item(i);
                    if ((eElement.getElementsByTagName("node").item(0).getTextContent().compareTo(eElement.getElementsByTagName("node").item(0).getTextContent())) != 0) {
                        flag = false;
                    }
                    if (eElement.getElementsByTagName("name").item(0) == null) {
                        
                        flag = false;
                    }
                    if ((eElement.getElementsByTagName("node").item(0).getTextContent()).length() == 0) {
                        
                        flag = false;
                    }
                    if (eElement.getElementsByTagName("type").item(0) == null) {
                        flag = false;
                    }
                    names.add(eElement.getElementsByTagName("name").item(0).getTextContent());
               
            }
            /*if (names.size() != 84) // Number og name tags
            {
                System.out.println(names);  // in the example xml there are chunks with the same name
                flag = false;
            }*/

            return flag;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private void updateXML(String node, String value) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document org = dBuilder.parse(xml_config_path);
            org.getDocumentElement().normalize();
            NodeList oList = org.getElementsByTagName("chunk");
            for (int i = 0; i < oList.getLength(); i++) {
                Node oNode = oList.item(i);
                if (oNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element oElement = (Element) oNode;
                    if (oElement.getAttribute("node") == node) {
                        // this function will edit the xml according to function parameters
                    }

                }
            }
        } catch (Exception e) {

        }
    }

    @FXML
    private void uploadCSV() throws IOException {
        /**
         *
         * This method gets a File object of a CSV file and checks if it satisfies:
         * 1. The CSV contains all required on the 'csvHeaders' variable
         * 
         */
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        chooser.setTitle("Choose CSV file");
        chooser.getExtensionFilters().add(extFilter);
        File file = chooser.showOpenDialog(null);

        if (file == null)
            return;

        boolean validated = validateCSV(file);
        if (validated) {
            // TODO: upload csv to server
        } else {
            // Show Errors...
        }

    }

    private boolean validateCSV(File file) throws IOException {
        HashSet<String> uniqueColumnsSet = new HashSet<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String header = br.readLine();
        if (header != null) {
            // Create a distinct set of strings (remove dupilcates)
            uniqueColumnsSet = Arrays.stream(header.split(",")).distinct()
                    .collect(Collectors.toCollection(HashSet::new));
        }
        br.close();

        // Check if uniqueColumnsSet is equal to the Settings file columns
        if (uniqueColumnsSet.equals(this.xmlColumnsNames)) {
            return true;
        } else {
            return false;
        }
    }
}