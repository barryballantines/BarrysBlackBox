package ballantines.avionics.blackbox.panel;

import ballantines.avionics.blackbox.Services;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author mbuse
 */
public class RadioPanel implements Initializable {
    
    final ToggleGroup VOR1_BUTTON_GROUP = new ToggleGroup();
    final ToggleGroup VOR2_BUTTON_GROUP = new ToggleGroup();
    final ToggleGroup ADF1_BUTTON_GROUP = new ToggleGroup();
    final ToggleGroup ADF2_BUTTON_GROUP = new ToggleGroup();
    final ToggleGroup COMM1_BUTTON_GROUP = new ToggleGroup();
    final ToggleGroup COMM2_BUTTON_GROUP = new ToggleGroup();
    
    private Services services;
    
    @FXML private ListView<RadioPreset> radioPresetList;
    @FXML private Button addVORBtn;
    @FXML private Button addADFBtn;
    @FXML private Button addCOMMBtn;
    @FXML private BorderPane mainPane;
    
    public static Parent create(Services services) throws IOException {
        RadioPanel controller = new RadioPanel();
        controller.setServices(services);
        FXMLLoader loader = new FXMLLoader(RadioPanel.class.getResource("/fxml/radio-panel.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }

    public void setServices(Services services) {
        this.services = services;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        radioPresetList.setCellFactory(preset -> new RadioPresetCell());
        radioPresetList.prefWidthProperty().bind(mainPane.widthProperty().subtract(4));
        radioPresetList.getSelectionModel().selectedItemProperty().addListener(item -> {
            Platform.runLater(() -> {
                radioPresetList.getSelectionModel().clearSelection();
            });
           
        });
    }
    
    @FXML
    public void handleAddVORAction(ActionEvent event) {
        radioPresetList.getItems().add(new VORPreset("New VOR"));
    }
    
    @FXML
    public void handleAddADFAction(ActionEvent event) {
        radioPresetList.getItems().add(new ADFPreset("New ADF"));
    }
    
    @FXML
    public void handleAddCOMMAction(ActionEvent event) {
        radioPresetList.getItems().add(new COMMPreset("New COMM"));
    }
    
    
    // ===
    
    public class RadioPresetCell extends ListCell<RadioPreset> {

        @Override
        protected void updateItem(RadioPreset item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty||item==null) {
                setGraphic(null);
            }
            else {
                setGraphic(item.createNode());
            }
            prefWidthProperty().bind(radioPresetList.widthProperty().subtract(4));
        }
        
       
    }
    
    public abstract class RadioPreset implements Initializable {
        
        protected String stationName;
        protected String frequency;
        protected String frequencyProperty1;
        protected String frequencyProperty2;
        protected String fxmlUrl;
        
        private ToggleGroup toggleGroup1;
        private ToggleGroup toggleGroup2;
        
        @FXML protected TextField frequencyTF;
        @FXML protected TextField nameTF; 
        @FXML protected ToggleButton station1Btn;
        @FXML protected ToggleButton station2Btn;
        
        public RadioPreset(String fxmlUrl, 
                           String name, 
                           String property1, 
                           String property2,
                           ToggleGroup toggleGroup1,
                           ToggleGroup toggleGroup2) {
            this.fxmlUrl = fxmlUrl;
            this.stationName = name;
            this.frequencyProperty1 = property1;
            this.frequencyProperty2 = property2;
            this.toggleGroup1 = toggleGroup1;
            this.toggleGroup2 = toggleGroup2;
        }
        
        public Node createNode() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlUrl));
                loader.setController(this);
                return (Node) loader.load();
            } catch (Exception ex) {
                // OMG!!!
                throw new RuntimeException("PANIC! Cannot create RadioPresetPanel", ex);
            }
        }
        
        @Override
        public void initialize(URL location, ResourceBundle resources) {
            nameTF.setText(stationName);
            frequencyTF.setText(frequency);
            station1Btn.setToggleGroup(toggleGroup1);
            station2Btn.setToggleGroup(toggleGroup2);
            
        }
        
        @FXML void handleValueChanged() {
            stationName = nameTF.getText();
            frequency = frequencyTF.getText();
        }
        
        
        @FXML void handleRemoveAction() {
            radioPresetList.getItems().remove(this);
        }
        
        @FXML
        public void handleStation1Action(ActionEvent event) {
            writeFrequencyProperty(frequencyProperty1);
        }
        
        @FXML 
        public void handleStation2Action(ActionEvent event) {
            writeFrequencyProperty(frequencyProperty2);
        }
        
        private void writeFrequencyProperty(String prop) {
            try {
                double freq = Double.parseDouble(frequency);
                services.getPropertyService().writeProperty(prop, freq);
            }
            catch (NumberFormatException nfe) {
                
            }
        }
    }
    
    public class VORPreset extends RadioPreset {
        
        protected String course;
        protected String courseProperty1 = "/instrumentation/nav/radials/selected-deg";
        protected String courseProperty2 = "/instrumentation/nav[1]/radials/selected-deg";
        @FXML protected TextField courseTF;
        
        public VORPreset(String name) {
            super("/fxml/radios-vor.fxml", 
                  name, 
                  "/instrumentation/nav/frequencies/selected-mhz", 
                  "/instrumentation/nav[1]/frequencies/selected-mhz",
                  VOR1_BUTTON_GROUP,
                  VOR2_BUTTON_GROUP);
        }
        @Override
        public void initialize(URL location, ResourceBundle resources) {
            super.initialize(location, resources);
            courseTF.setText(course);
        }
        
        @Override @FXML
        void handleValueChanged() {
            super.handleValueChanged();
            course = courseTF.getText();
        }
        
        @FXML @Override
        public void handleStation1Action(ActionEvent event) {
            writeFrequencyAndCourse(frequencyProperty1, courseProperty1);
        }
        
        @FXML @Override
        public void handleStation2Action(ActionEvent event) {
            writeFrequencyAndCourse(frequencyProperty2, courseProperty2);
        }
        
        private void writeFrequencyAndCourse(String freqProp, String crsProp) {
            try {
                double freq = Double.parseDouble(frequency);
                double  crs = Double.parseDouble(course);
                Map<String, Object> p = new HashMap<>();
                p.put(freqProp, freq);
                p.put(crsProp, crs);
                services.getPropertyService().writeProperties(p);
            } catch (NumberFormatException nfe) {}
        }
        
    }
    
    public class COMMPreset extends RadioPreset {
        public COMMPreset(String name) {
            super("/fxml/radios-comm.fxml", 
                  name, 
                  "/instrumentation/comm/frequencies/selected-mhz", 
                  "/instrumentation/comm[1]/frequencies/selected-mhz",
                  COMM1_BUTTON_GROUP,
                  COMM2_BUTTON_GROUP);
        }
    }
    
    public class ADFPreset extends RadioPreset {
        public ADFPreset(String name) {
            super("/fxml/radios-adf.fxml", 
                  name, 
                  "/instrumentation/adf/frequencies/selected-khz", 
                  "/instrumentation/adf[1]/frequencies/selected-khz",
                  ADF1_BUTTON_GROUP,
                  ADF2_BUTTON_GROUP);
        }
    }
}
