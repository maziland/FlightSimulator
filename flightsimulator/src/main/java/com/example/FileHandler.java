package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

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
        /*
         * This method gets a File object of a XML file and checks if it satisfies:
         * 1. The file contains all required 'flight gear simulator nodes'
         * 2. All 'name' tags aren't empty
         */
        // TODO: add functionality
        return true;
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