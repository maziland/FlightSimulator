package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.*;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;

public class XMLHandler {

    // final String xml_config_path = "src/main/config/config.xml";

    // After pulling from git there are 2 Dirs named flightSimulator, a little
    // patch:
    final String xml_config_path = "flightsimulator/src/main/config/config.xml";

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

        // TODO: add support for the file validation
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
         * This method get a File object of a XML file and checks if it satisfies:
         * 1. The file contains all required 'flight gear simulator nodes'
         * 2. All 'name' tags aren't empty
         */
        return true;
    }

    @FXML
    private void uploadCSV() throws IOException {

        // Setting a file chooser of XML files only
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        chooser.setTitle("Choose XML file");
        chooser.getExtensionFilters().add(extFilter);
        File file = chooser.showOpenDialog(null);

        if (file == null)
            return;

        // TODO: add support for the file validation
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
}