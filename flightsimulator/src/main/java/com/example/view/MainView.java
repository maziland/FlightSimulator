package com.example.view;

import com.example.util.*;

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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
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
    List<Integer> currentAnomaliesTimeSteps;
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
    private NumberAxis FGxAxis;
    @FXML
    private NumberAxis FGyAxis;
    @FXML
    private NumberAxis SGxAxis;
    @FXML
    private NumberAxis SGyAxis;

    @FXML
    private LineChart<Number, Number> selectedAttributeGraph, correlativeAttributeGraph, anomaliesGraph;
    private XYChart.Series<Number, Number> pointSeries;
    private XYChart.Series<Number, Number> linRegSeries;

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
        this.algorithmsDropdown.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            this.selectedAlgorithm.set(nv);
        });
        this.algorithmsDropdown.itemsProperty().bind(this.vm.algorithmsListProperty);
        this.algorithmsDropdown.getSelectionModel().selectFirst();
        this.vm.selectedAttribute.bind(this.selectedAttribute);
        this.vm.selectedAlgorithm.bind(this.selectedAlgorithm);

        this.set_startup_xml();
        this.set_startup_csv();
        this.init_graphs();
        this.hashMap.bind(this.vm.hashMap);
        this.TimeSlider.setMax(this.vm.getfilesize()); // change this

        this.TimeSlider.valueProperty().bind(this.currentTimeStepProperty);

        this.TimeSlider.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> this.TimeSlider.valueProperty().unbind());
        this.TimeSlider.addEventFilter(MouseEvent.MOUSE_RELEASED,
                e -> this.TimeSlider.valueProperty().bind(this.currentTimeStepProperty));

        this.TimeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            vm.timeSliderHandler(newValue.intValue());
            this.TimeSlider.valueProperty().bind(this.currentTimeStepProperty);
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

        this.anomaliesGraph.setCreateSymbols(true);
        this.anomaliesGraph.setAnimated(false);
        linRegSeries = new XYChart.Series<>();
        pointSeries = new XYChart.Series<>();
        // linRegSeries.setCreateSymbols(false);
        this.anomaliesGraph.getData().add(linRegSeries);
        this.anomaliesGraph.getData().add(pointSeries);
        this.vm.updateHashMap();
    };

    private Line getCorrelatedLinearRegression(String selected, String correlated) {
        return this.vm.getCorrelatedLinearRegression(selected, correlated);
    }

    private void clearAllGraphs() {
        this.selectedAttributeGraph.getData().clear();
        this.anomaliesGraph.getData().clear();
        this.correlativeAttributeGraph.getData().clear();
    }

    private void updateAllGraphs(boolean attributeChanged) {
        String selectedAttr = this.selectedAttribute.getValue();
        String correlatedAttr = this.vm.getCorrelatedFeature(selectedAttr);

        // Update selected Attribute graph
        updateSpecificGraph(this.selectedAttributeGraph, selectedAttr, this.hashMap.valueAt(selectedAttr).getValue(),
                attributeChanged);

        // Update correlated Attribute graph
        updateSpecificGraph(this.correlativeAttributeGraph, correlatedAttr,
                this.hashMap.valueAt(selectedAttr).getValue(), attributeChanged);

        // Update anomalies graph
        updateAnomaliesGraph(selectedAttr, correlatedAttr, attributeChanged);
    }

    private void updateAnomaliesGraph(String selectedAttr, String correlatedAttr, boolean attributeChanged) {

        if (attributeChanged)
            this.currentAnomaliesTimeSteps = this.vm.detectAnomalies(selectedAttr + ":" + correlatedAttr);
        setAnomalyGraphSeriesList(selectedAttr, correlatedAttr, attributeChanged);
    }

    private void setAnomalyGraphSeriesList(String selectedAttr, String correlatedAttr,
            boolean attributeChanged) {

        Circle redCircle = new Circle(3);
        redCircle.setFill(Color.RED);

        Integer index = this.currentTimeStepProperty.getValue();

        if (this.selectedAlgorithm.getValue().equals("SimpleAnomalyDetector")) {

            // Get linear regression
            Point[] pointsArr = StatLib.createPointsArray(this.hashMap.get(selectedAttr),
                    this.hashMap.get(correlatedAttr));

            Line linReg = getCorrelatedLinearRegression(selectedAttr, correlatedAttr);

            // Points
            if (attributeChanged == false) {
                if (index == this.vm.getfilesize())
                    return;
                XYChart.Data<Number, Number> point = new XYChart.Data<Number, Number>(pointsArr[index].x,
                        pointsArr[index].y);
                if (this.currentAnomaliesTimeSteps.contains(index))
                    point.setNode(redCircle);
                this.pointSeries.getData().add(point);

            }

            else {
                pointSeries.getData().clear();
                for (int i = 0; i < index; i++) {
                    XYChart.Data<Number, Number> point = new XYChart.Data<Number, Number>(pointsArr[i].x,
                            pointsArr[i].y);
                    if (this.currentAnomaliesTimeSteps.contains(index)) {
                        point.setNode(redCircle);
                    }
                    this.pointSeries.getData().add(point);
                }
            }

            // Linear reg
            if (attributeChanged == false) {
                float a = this.hashMap.get(this.selectedAttribute.get())[this.currentTimeStepProperty.get()];
                linRegSeries.getData().add(new XYChart.Data<Number, Number>(a, a * linReg.a + linReg.b));
            } else {
                linRegSeries.getData().clear();
                for (int i = 1; i < index; i++) {
                    linRegSeries.getData().add(new XYChart.Data<Number, Number>(i, (linReg.a * i) + linReg.b));
                }
            }

        } else if (this.selectedAlgorithm.getValue().equals("ZScoreAnomalyDetector")) {
            List<Float> zscoreList = this.vm.getZscoresForFeature(selectedAttr);
            if (zscoreList == null) {
                System.out.println("Got null");
            }

            // Points
            if (attributeChanged == false) {
                if (index == this.vm.getfilesize())
                    return;
                Float y = zscoreList.get(index);
                XYChart.Data<Number, Number> point = new XYChart.Data<Number, Number>(index,
                        y);
                // TODO: add anomalies for zScore alg
                if (this.currentAnomaliesTimeSteps.contains(index))
                    point.setNode(redCircle);
                System.out.println("NOT CHANGED!!! Adding point: (" + index + "," + y + ")");
                if (!(Float.isInfinite(y) && Float.isNaN(y)))
                    this.pointSeries.getData().add(point);

            }

            else {
                this.clearAllGraphs();
                pointSeries.getData().clear();
                for (int i = 0; i < index; i++) {
                    XYChart.Data<Number, Number> point = new XYChart.Data<Number, Number>(index,
                            zscoreList.get(index));
                    if (this.currentAnomaliesTimeSteps.contains(index)) {
                        point.setNode(redCircle);
                    }
                    System.out.println("CHANGED!!! Adding point: (" + index + "," + zscoreList.get(index) + ")");
                    this.pointSeries.getData().add(point);
                }
            }
        }

    }

    private void updateSpecificGraph(LineChart<Number, Number> graph, String attr, float[] values,
            boolean attributeChanged) {
        XYChart.Series<Number, Number> series;

        // In case we updateGraphs because the selectedAttribute changed, build the
        // series from the ground up
        if (attributeChanged) {
            series = new XYChart.Series<>();
            if (values == null) {
                graph.getData().clear();
                graph.setTitle("None");
                return;
            }

            for (int i = 1; i < this.currentTimeStepProperty.get(); i++) {
                series.getData().add(new XYChart.Data<>(i, values[i - 1]));
            }
            graph.getData().clear();
            graph.setTitle(attr);
            graph.getData().add(series);
        }

        // Otherwise, we update because of timeStep update - add only the neccessary
        // data to the series - MUCH FASTER!
        else if (graph.getData().size() != 0) {
            series = graph.getData().get(0);
            int index = this.currentTimeStepProperty.get();
            try {
                series.getData().add(new XYChart.Data<>(index, this.hashMap.valueAt(attr).getValue()[index - 1]));
            } catch (Exception e) {
            }
        }

        float max = values[1];
        float min = values[1];
        for (int i = 1; i < this.currentTimeStepProperty.get(); i++) {
            if (max < values[i]) {
                max = values[i];
            }
            if (min > values[i]) {
                min = values[i];
            }
        }

        // Set attributes for first graph
        FGxAxis.setAutoRanging(false);
        FGxAxis.setUpperBound(this.currentTimeStepProperty.get());
        // FGxAxis.setTickUnit(this.currentTimeStepProperty.get()/10);
        FGxAxis.setAnimated(true);
        FGyAxis.setAutoRanging(false);
        FGyAxis.setLowerBound(min);
        FGyAxis.setUpperBound(max);
        FGyAxis.setTickUnit((max - min) / 10);
        FGyAxis.setAnimated(true);

        // Set attributes for second graph
        SGxAxis.setAutoRanging(false);
        SGxAxis.setUpperBound(this.currentTimeStepProperty.get());
        // SGxAxis.setTickUnit(this.currentTimeStepProperty.get()/10);
        SGxAxis.setAnimated(true);
        SGyAxis.setAutoRanging(false);
        SGyAxis.setLowerBound(min);
        SGyAxis.setUpperBound(max);
        SGyAxis.setTickUnit((max - min) / 10);
        SGyAxis.setAnimated(true);

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
        if (id.equals("ForwardButton")) {
            int new_value = Integer.parseInt(this.speedInput.getText()) + 1;
            this.speedInput.setText(Integer.toString(new_value));
            vm.setSpeedTime(new_value);
        } else if (id.equals("BackwardButton")) {
            int new_value = Integer.parseInt(this.speedInput.getText()) - 1;

            // Verify that the user is not try to set the value to 0
            if (new_value != 0) {
                this.speedInput.setText(Integer.toString(new_value));
                vm.setSpeedTime(new_value);
            }
        } else if (id.equals("stopButton")) {
            clearAllGraphs();
        }
        this.vm.mediaCommand(id);
    }

    @FXML
    public void listMouseClick(MouseEvent mevent) {
        String id = ((Control) mevent.getSource()).getId();
        switch (id) {
            case ("algorithmsDropdown"):
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