package com.example.view;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.FileChooser;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Control;

import com.example.viewmodel.MainViewModel;

public class MainView implements Initializable {

    // regular variables
    final String xml_config_path = "flightsimulator/src/main/config/config.xml";
    final String csv_config_path = "flightsimulator/src/main/config/flight.csv";
    List<String> xmlColumnsNames;
    List<String> xmlNodes;
    StringProperty selectedAttribute, selectedAlgorithm;

    // FXML variables
    @FXML
    private ProgressBar myProgressBar;
    @FXML
    private Button uploadCSV, uploadXML;
    @FXML
    private ListView<String> attributeList;
    @FXML
    private ComboBox<String> algorithmsDropdown;

    private MainViewModel vm;

    public void setViewModel(MainViewModel vm) {
        this.vm = vm;
        this.selectedAttribute = new SimpleStringProperty();
        this.selectedAlgorithm = new SimpleStringProperty();
        this.attributeList.itemsProperty().bind(this.vm.attributesListProperty);
        this.algorithmsDropdown.itemsProperty().bind(this.vm.algorithmsListProperty);
        this.algorithmsDropdown.getSelectionModel().selectFirst();
        this.vm.selectedAttribute.bind(this.selectedAttribute);
        this.vm.selectedAlgorithm.bind(this.selectedAlgorithm);
        this.set_startup_xml();
        this.set_startup_csv();
    }

    private void set_startup_xml() {
        File start_xml = new File(xml_config_path);
        boolean validated = this.vm.validateXML(start_xml);
        if (validated) {
            System.out.println("Startup XML verified successfully");
        } else {
            System.out.println("Cannot verify XML");
        }
    }

    private void set_startup_csv() {
        File start_csv = new File(csv_config_path);
        boolean validated = false;
        try {
            validated = this.vm.validateCSV(start_csv);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        if (validated) {
            System.out.println("Startup CSV verified successfully");
        } else {
            System.out.println("Cannot verify CSV");
        }
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
            Files.copy(file.toPath(), (new File(csv_config_path)).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            System.out.println("CSV Validated successfully");
        } else {
            // Show Errors...
            System.out.println("Cannot validate CSV");
        }
    }

    @FXML
    public void controlButtonHandler(ActionEvent e) {
        String id = ((Button) e.getSource()).getId();
        this.vm.mediaCommand(id);
    }

    @FXML
    public void listMouseClick(MouseEvent mevent) {
        String id = ((Control) mevent.getSource()).getId();
        switch (id) {
            case ("algorithmsDropdown"):
                System.out.println("click!");
                String selected = algorithmsDropdown.getSelectionModel().getSelectedItem();
                if (selected.equals("Upload Algorithm...")) {
                    try {
                        this.uploadAlgorithm();
                    } catch (Exception e) {
                        System.err.println("Failed uploading the algorithm");
                        System.err.println(e.toString());
                    }
                } else {
                    this.selectedAlgorithm.set(selected);
                }
                break;
            case ("attributeList"):
                this.selectedAttribute.set(attributeList.getSelectionModel().getSelectedItem());
                break;
            default:
                break;
        }

    }

    private void uploadAlgorithm() throws IOException {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Class files (*.class)", "*.class");
        chooser.setTitle("Choose Class file");
        chooser.getExtensionFilters().add(extFilter);
        File file = chooser.showOpenDialog(null);

        if (file == null) {
            this.algorithmsDropdown.getSelectionModel().selectFirst();
            return;
        }

        try {
            this.vm.uploadAlgorithm(file);
            System.out.println("Uploaded the algorithm successfuly");
            this.algorithmsDropdown.getSelectionModel().selectFirst();
        } catch (Exception e) {
            System.out.println(("There"));
            this.algorithmsDropdown.getSelectionModel().selectFirst();
        }
    }
}