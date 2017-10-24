package ballantines.avionics.blackbox.panel;

import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.model.Waypoint;
import ballantines.avionics.blackbox.util.Log;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author mbuse
 */
public class RadioPanel implements Initializable, PipeUpdateListener {
    private static final Log L = Log.forClass(RadioPanel.class);
    
    final ToggleGroup VOR1_BUTTON_GROUP = new ToggleGroup();
    final ToggleGroup VOR2_BUTTON_GROUP = new ToggleGroup();
    final ToggleGroup ADF1_BUTTON_GROUP = new ToggleGroup();
    final ToggleGroup ADF2_BUTTON_GROUP = new ToggleGroup();
    final ToggleGroup COMM1_BUTTON_GROUP = new ToggleGroup();
    final ToggleGroup COMM2_BUTTON_GROUP = new ToggleGroup();
    
    private Services services;
    
    @FXML private ListView<RadioPreset> radioPresetList;
    @FXML private BorderPane mainPane;
    
    private Pipe<RadioPreset> selectedVOR1PresetPipe = Pipe.newInstance("radioPanel.selectedVOR1Preset", this);
    private Pipe<RadioPreset> selectedVOR2PresetPipe = Pipe.newInstance("radioPanel.selectedVOR2Preset", this);
    private Pipe<RadioPreset> selectedADF1PresetPipe = Pipe.newInstance("radioPanel.selectedADF1Preset", this);
    private Pipe<RadioPreset> selectedADF2PresetPipe = Pipe.newInstance("radioPanel.selectedADF2Preset", this);
    private Pipe<RadioPreset> selectedCOMM1PresetPipe = Pipe.newInstance("radioPanel.selectedCOMM1Preset", this);
    private Pipe<RadioPreset> selectedCOMM2PresetPipe = Pipe.newInstance("radioPanel.selectedCOMM2Preset", this);
    private Pipe<List<Waypoint>> detailedRouteInfoPipe = Pipe.newInstance("radioPanel.detailedRouteInfoPipe", this);
    
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
        
        detailedRouteInfoPipe.connectTo(services.detailedRouteInfoPipe);
    }

    @Override
    public void pipeUpdated(Pipe pipe) {
        L.pipeUpdated(pipe);
        // TODO...
        
        if (detailedRouteInfoPipe == pipe) {
            addRadioPanelsForDetailedRouteInfo(detailedRouteInfoPipe.get());
        }
    }
    
    
    
    @FXML
    public void handleAddVORAction(ActionEvent event) {
        radioPresetList.getItems().add(new VORPreset("NAV #" + (radioPresetList.getItems().size()+1)));
    }
    
    @FXML
    public void handleAddADFAction(ActionEvent event) {
        radioPresetList.getItems().add(new ADFPreset("ADF #" + (radioPresetList.getItems().size()+1)));
    }
    
    @FXML
    public void handleAddCOMMAction(ActionEvent event) {
        radioPresetList.getItems().add(new COMMPreset("COM #" + (radioPresetList.getItems().size()+1)));
    }
    
    private void addRadioPanelsForDetailedRouteInfo(List<Waypoint> detailedRouteInfo) {
        if (detailedRouteInfo==null) {
            return;
        }
        for (Waypoint wp : detailedRouteInfo) {
            addRadioPanelForWaypoint(wp);
        }
    }
    
    private void addRadioPanelForWaypoint(Waypoint wp) {
        if (wp.isRadioFix()) {
            RadioPreset radio = null;
            if (wp.isVOR()) {
                // VOR
                radio = new VORPreset(wp);
            }
            else if (wp.isNDB()) {
                // NDB
                radio = new ADFPreset(wp);
            }
            else {
                // Unknown...
                return;
            }
            
            if (wp!=null) {
                radioPresetList.getItems().add(radio);
            }
        }
    }
    
    
    // ===
    
    public class RadioPresetCell extends ListCell<RadioPreset> {
        
        private static final String DND_PREFIX = "radiopreset:";
        
        public RadioPresetCell() {
            super();
            
            setOnDragDetected(event -> {
                RadioPreset item = getItem();
                if (item == null) {
                    return;
                }
                
                ClipboardContent content = new ClipboardContent();
                int index = getIndex();
                content.putString(DND_PREFIX + index);
                
                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                db.setContent(content);
        
                Image snapshot = getGraphic().snapshot(new SnapshotParameters(), null);
                db.setDragView(snapshot);
                
                event.consume();
            });
            
            setOnDragOver(event -> {
                if (event.getGestureSource()!=this && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });
            
            setOnDragEntered(event -> {
                if (event.getGestureSource()!=this && event.getDragboard().hasString()) {
                    setOpacity(0.3);
                }
            });
            
            setOnDragExited(event -> {
                if (event.getGestureSource()!=this && event.getDragboard().hasString()) {
                    setOpacity(1.0);
                }
            });
            
            setOnDragDropped(event -> { 
                Dragboard db = event.getDragboard();
                boolean success = false;
                
                if (db.hasString() && db.getString().startsWith(DND_PREFIX)) {
                    int currentIndex = getIndex();
                    int draggedIndex = Integer.parseInt(db.getString().substring(DND_PREFIX.length()));
                    
                    List<RadioPreset> presets = radioPresetList.getItems();
                    int presetsLength = presets.size();
                    
                    RadioPreset tmp = presets.remove(draggedIndex);
                    if (currentIndex >= presetsLength) {
                        presets.add(tmp);
                    }
                    else {
                        presets.add(currentIndex, tmp);
                    }
                    success = true;
                }
                
                event.setDropCompleted(success);
                event.consume();
            });
            
            setOnDragDone(DragEvent::consume);
        }

        
        
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
        
        protected Pipe<RadioPreset> radioPresetPipe1;
        protected Pipe<RadioPreset> radioPresetPipe2;
        
        @FXML protected TextField frequencyTF;
        @FXML protected TextField nameTF; 
        @FXML protected ToggleButton station1Btn;
        @FXML protected ToggleButton station2Btn;
        
        public RadioPreset(String fxmlUrl, 
                           String name, 
                           String property1, 
                           String property2,
                           ToggleGroup toggleGroup1,
                           ToggleGroup toggleGroup2,
                           Pipe<RadioPreset> pipe1,
                           Pipe<RadioPreset> pipe2) {
            this.fxmlUrl = fxmlUrl;
            this.stationName = name;
            this.frequencyProperty1 = property1;
            this.frequencyProperty2 = property2;
            this.toggleGroup1 = toggleGroup1;
            this.toggleGroup2 = toggleGroup2;
            this.radioPresetPipe1 = pipe1;
            this.radioPresetPipe2 = pipe2;
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
            
            station1Btn.setSelected(radioPresetPipe1.get()==this);
            station2Btn.setSelected(radioPresetPipe2.get()==this);
            
            nameTF.textProperty().addListener((observable, oldValue, newValue) -> { 
                stationName = newValue;
            });
            
            frequencyTF.textProperty().addListener((obs, oldValue, newValue) -> {
                frequency = newValue;
                validateFrequency();
            });
            validateFrequency();
        }
        
        protected void validateFrequency() {
            if (isFrequencyValid()) {
                frequencyTF.getStyleClass().remove("input-field--invalid");
            }
            else {
                frequencyTF.getStyleClass().add("input-field--invalid");
            }
        }
        
        protected boolean isFrequencyValid() {
            return frequency!=null && frequency.matches("\\d+(\\.\\d+)?");
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
            if (station1Btn.isSelected()) {
                radioPresetPipe1.set(this);
                writeFrequencyProperty(frequencyProperty1);
            } 
            else {
                radioPresetPipe1.set(null);
            }
        }
        
        @FXML 
        public void handleStation2Action(ActionEvent event) {
            if (station2Btn.isSelected()) {
                radioPresetPipe2.set(this);
                writeFrequencyProperty(frequencyProperty2);
            }
            else {
                radioPresetPipe2.set(null);
            }
        }
        
        private void writeFrequencyProperty(String prop) {
            if (isFrequencyValid()) {
                try {
                    double freq = Double.parseDouble(frequency);
                    services.getPropertyService().writeProperty(prop, freq);
                }
                catch (NumberFormatException nfe) {

                }
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
                  VOR2_BUTTON_GROUP,
                  selectedVOR1PresetPipe,
                  selectedVOR2PresetPipe);
        }
        
        public VORPreset(Waypoint wp) {
            this(wp.ident);
            frequency = String.format(Locale.US, "%3.2f", wp.freq);
            course = String.format(Locale.US, "%03d", wp.track);
        }
        
        @Override
        public void initialize(URL location, ResourceBundle resources) {
            super.initialize(location, resources);
            courseTF.setText(course);
            
            courseTF.textProperty().addListener((obs, oldValue, newValue) -> {
                course = newValue;
                validateCourse();
            });
            validateCourse();
        }
        
        protected void validateCourse() {
            if (isCourseValid()) {
                courseTF.getStyleClass().remove("input-field--invalid");
            }
            else {
                courseTF.getStyleClass().add("input-field--invalid");
            }
        }
        
        protected boolean isCourseValid() {
            return course!=null && course.matches("\\d\\d?\\d?");
        }
        
        @Override @FXML
        void handleValueChanged() {
            super.handleValueChanged();
            course = courseTF.getText();
        }
        
        @FXML @Override
        public void handleStation1Action(ActionEvent event) {
            if (station1Btn.isSelected()) {
                radioPresetPipe1.set(this);
                writeFrequencyAndCourse(frequencyProperty1, courseProperty1);
            }
            else {
                radioPresetPipe1.set(null);
            }
        }
        
        @FXML @Override
        public void handleStation2Action(ActionEvent event) {
            if (station2Btn.isSelected()) {
                radioPresetPipe2.set(this);
                writeFrequencyAndCourse(frequencyProperty2, courseProperty2);
            }
            else {
                radioPresetPipe2.set(null);
            }
        }
        
        private void writeFrequencyAndCourse(String freqProp, String crsProp) {
            try {
                Map<String, Object> p = new HashMap<>();
                if (isFrequencyValid()) {
                    double freq = Double.parseDouble(frequency);
                    p.put(freqProp, freq);
                }
                if (isCourseValid()) {
                    double  crs = Double.parseDouble(course);
                    p.put(crsProp, crs);
                }
                if (!p.isEmpty()) {
                    services.getPropertyService().writeProperties(p);
                }
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
                  COMM2_BUTTON_GROUP,
                  selectedCOMM1PresetPipe,
                  selectedCOMM2PresetPipe);
        }
    }
    
    public class ADFPreset extends RadioPreset {
        public ADFPreset(String name) {
            super("/fxml/radios-adf.fxml", 
                  name, 
                  "/instrumentation/adf/frequencies/selected-khz", 
                  "/instrumentation/adf[1]/frequencies/selected-khz",
                  ADF1_BUTTON_GROUP,
                  ADF2_BUTTON_GROUP,
                  selectedADF1PresetPipe,
                  selectedADF2PresetPipe);
        }
        
         public ADFPreset(Waypoint wp) {
            this(wp.ident);
            frequency = String.format(Locale.US, "%3.2f", wp.freq);
         }
    }
}
