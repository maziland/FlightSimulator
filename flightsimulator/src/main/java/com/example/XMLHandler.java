package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javafx.fxml.FXML;
import javafx.scene.shape.Path;
import javafx.stage.FileChooser;

public class XMLHandler {

    final String xml_config_path = "src/main/config/config.xml";
    @FXML
    private void uploadXML() throws IOException {

        FileChooser file = new FileChooser();
        file.setTitle("Choose XML file");  
        File file1 = file.showOpenDialog(null);

        // TODO: add support for the file validation
        boolean validate = true;

        // Save file 
        if (validate)
        {
            Files.copy(file1.toPath(), (new File(xml_config_path)).toPath(), StandardCopyOption.REPLACE_EXISTING);

        }
        // Show errors
        else{

        }
    }
}
