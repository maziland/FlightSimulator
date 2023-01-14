package com.example.view;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.FileChooser;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Control;
import javafx.scene.control.Label;

import com.example.viewmodel.MainViewModel;

public class MainView implements Initializable {

    // regular variables
    final String xml_config_path = "flightsimulator/src/main/config/config.xml";
    final String csv_config_path = "flightsimulator/src/main/config/flight.csv";
    List<String> xmlColumnsNames;
    List<String> xmlNodes;
    StringProperty selectedAttribute, selectedAlgorithm;
    MapProperty<String, float[]> hashMap;

    IntegerProperty currentTimeStepProperty;

    // FXML variables
    @FXML
    private ProgressBar myProgressBar;
    @FXML
    private Button uploadCSV, uploadXML;
    @FXML
    private ListView<String> attributeList;
    @FXML
    private ComboBox<String> algorithmsDropdown;

    @FXML
    private Circle joyStick;
    @FXML
    private Slider throttle;
    @FXML
    private Slider rudder;
    @FXML
    private Label latitude;
    @FXML
    private Label longitude;
    @FXML
    private Label altitude;
    @FXML
    private Label roll;
    @FXML
    private Label pitch;
    @FXML
    private Label yawn;

    @FXML
    private LineChart<Number, Number> selectedAttributeGraph, correlativeAttributeGraph, anomaliesGraph;

    @FXML
    private TextField speedInput;

    @FXML
    private Slider TimeSlider;

    private MainViewModel vm;

    public void setViewModel(MainViewModel vm) {
        this.vm = vm;
        this.currentTimeStepProperty = new SimpleIntegerProperty();
        this.currentTimeStepProperty.bind(this.vm.currentTimeStepProperty);
        this.currentTimeStepProperty.addListener((o, ov, nv) -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    updateAllGraphs(false);
                    update_stabelizers();
                }
            });
        });

        this.selectedAttribute = new SimpleStringProperty();
        this.selectedAlgorithm = new SimpleStringProperty();
        this.hashMap = new SimpleMapProperty<>();
        this.attributeList.itemsProperty().bind(this.vm.attributesListProperty);
        this.algorithmsDropdown.itemsProperty().bind(this.vm.algorithmsListProperty);
        this.algorithmsDropdown.getSelectionModel().selectFirst();
        this.vm.selectedAttribute.bind(this.selectedAttribute);
        this.vm.selectedAlgorithm.bind(this.selectedAlgorithm);
        this.set_startup_xml();
        this.set_startup_csv();
        this.init_graphs();
        this.hashMap.bind(this.vm.hashMap);
        this.TimeSlider.setMax(this.vm.getfilesize()); // change this
        this.TimeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue.intValue());
            vm.timeSliderHandler(newValue.intValue());
        });
        this.speedInput.textProperty().addListener((obs, oldValue, newValue) -> {

            try {
                Integer.parseInt(newValue);
                vm.setSpeedTime(Integer.parseInt(newValue));
            } catch (Exception e) {
                System.out.println("Only int values are supported");
            }
        });
    }

    private void init_graphs() {
        this.selectedAttributeGraph.setCreateSymbols(false);
        this.selectedAttributeGraph.setAnimated(false);

        this.correlativeAttributeGraph.setCreateSymbols(false);
        this.correlativeAttributeGraph.setAnimated(false);

        this.anomaliesGraph.setCreateSymbols(false);
        this.vm.updateHashMap();
    };

    private void updateAllGraphs(boolean attributeChanged) {
        String selectedAttr = this.selectedAttribute.getValue();
        String correlatedAttr = this.vm.getCorrelatedFeature(selectedAttr);
        updateSpecificGraph(this.selectedAttributeGraph, selectedAttr, attributeChanged);
        updateSpecificGraph(this.correlativeAttributeGraph, correlatedAttr, attributeChanged);
    }

    private void updateSpecificGraph(LineChart<Number, Number> graph, String attr, boolean attributeChanged) {
        XYChart.Series<Number, Number> series;

        // In case we updateGraphs because the selectedAttribute changed, build the
        // series from the ground up
        if (attributeChanged) {
            series = new XYChart.Series<>();
            float[] values = this.hashMap.valueAt(attr).getValue();
            if (values == null) {
                graph.getData().clear();
                graph.setTitle("None");
                return;
            }
            // TODO: set upper bound to the max current value
            for (int i = 1; i < this.currentTimeStepProperty.get(); i++) {
                // TODO: fix reading value at [2175] location
                series.getData().add(new XYChart.Data<>(i, values[i - 1]));
            }
            graph.getData().clear();
            graph.setTitle(attr);
            graph.getData().add(series);
        }
        // Otherwise, we update because of timeStep update - add only the neccessary
        // data to the series - MUCH FASTER!
        else {
            series = graph.getData().get(0);
            int index = this.currentTimeStepProperty.get();
            // TODO: fix reading value at [2175] location
            System.out.println("INDEX: " + index + "--VALUE: " + this.hashMap.valueAt(attr).getValue()[index - 1]);
            series.getData().add(new XYChart.Data<>(index, this.hashMap.valueAt(attr).getValue()[index - 1]));
        }
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
            // TODO: handle chaging CSV before replacing because it's used by the FSC
            Files.copy(file.toPath(), (new File(csv_config_path)).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            Alert alert = new Alert(AlertType.NONE, "CSV validated successfuly", ButtonType.OK);
            alert.setTitle("CSV Validated successfully");
            alert.show();
            System.out.println("CSV Validated successfully");
        } else {
            Alert alert = new Alert(AlertType.NONE, "CSV validation failed", ButtonType.OK);
            alert.setTitle("Validation Failed");
            alert.show();
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
                updateAllGraphs(true);

                break;
            default:
                break;
        }

    }

    @FXML
    private void update_stabelizers()
    // Moves throttle and ruddle and joystick and changes dashboard data
    {
        float[] pos = this.vm.joyStickPos();

        // Setting joyStick and bars location
        joyStick.setCenterX(pos[0] * 30);
        joyStick.setCenterY(pos[1] * 30);
        rudder.setValue(pos[2] * 100);
        throttle.setValue(pos[3] * 100);

        // Setting dashboard values
        System.out.println(Float.toString(pos[4]));
        latitude.setText(Float.toString(pos[4]));
        longitude.setText(Float.toString(pos[5]));
        altitude.setText(Float.toString(pos[6]));
        roll.setText(Float.toString(pos[7]));
        pitch.setText(Float.toString(pos[8]));
        yawn.setText(Float.toString(pos[9]));

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
            System.err.println(e.toString());
            this.algorithmsDropdown.getSelectionModel().selectFirst();
        }
    }
}