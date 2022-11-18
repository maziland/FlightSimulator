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
        try{
            boolean flag = true;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            Document org = dBuilder.parse(xml_config_path);
            org.getDocumentElement().normalize();
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("chunk");
            NodeList oList = org.getElementsByTagName("chunk");
            Set<String> names = new HashSet<String>();


            for(int i=0; i<nList.getLength();i++)
            {
                Node nNode = nList.item(i);
                Node oNode = oList.item(i);
                if(nNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    // Makes sure all elements are present
                    Element eElement = (Element) nNode;
                    Element oElement = (Element) oNode;
                    if(eElement.getAttribute("node")!=oElement.getAttribute("node"))
                    {flag = false;}
                    if(!(eElement.hasAttribute("name")))
                    {flag = false;}
                    if((eElement.getAttribute("name")) == "")
                    {flag = false;}
                    if(!(eElement.hasAttribute("type")))
                    {flag = false;}
                    names.add(eElement.getAttribute("name"));
                }
                else{flag = false;}
                
            }
            if(names.size()!=168 ) // Number og name tags
            {
                flag = false; 
            }

            return flag;
        }
        catch(Exception e)
        {
            return false;
        }
    }

    private void updateXML(String node, String value)
    {
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document org = dBuilder.parse(xml_config_path);
            org.getDocumentElement().normalize();
            NodeList oList = org.getElementsByTagName("chunk");
            for(int i=0;i<oList.getLength();i++)
            {
                Node oNode = oList.item(i);
                if(oNode.getNodeType() == Node.ELEMENT_NODE)
                {   
                    Element oElement = (Element) oNode;
                    if(oElement.getAttribute("node") == node)
                    {
                        // this function will edit the xml according to function parameters
                    }
                    
                }
            }
        }
        catch(Exception e)
        {

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
        HashSet uniqueColumnsSet = new HashSet<>();
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