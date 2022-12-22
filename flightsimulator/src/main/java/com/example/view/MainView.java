package com.example.view;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.FileChooser;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import com.example.viewmodel.MainViewModel;

public class MainView implements Initializable {

    // regular variables
    final String xml_config_path = "flightsimulator/src/main/config/config.xml";
    List<String> xmlColumnsNames;
    List<String> xmlNodes;

    // FXML variables
    @FXML
    private ProgressBar myProgressBar;
    @FXML
    private Button uploadCSV, uploadXML;
    @FXML
    private ListView<String> attributesList;

    // Properties to bind:
    public ListProperty<String> attributesListProperty = new SimpleListProperty<>(FXCollections.observableArrayList());

    private MainViewModel vm;

    public void setViewModel(MainViewModel vm) {
        this.vm = vm;
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

    }

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

        boolean validated = this.vm.validateXML(file);

        // Save file
        if (validated) {
            System.out.println("XML verified successfully");
            Files.copy(file.toPath(), (new File(xml_config_path)).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        // Show errors
        else {
            System.out.println("Cannot verify XML");
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

        boolean validated = this.vm.validateCSV(file);
        if (validated) {
            // TODO: upload csv to server
            System.out.println("CSV Validated successfully");
        } else {
            // Show Errors...
            System.out.println("Cannot validate CSV");
        }

    }
}