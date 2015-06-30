/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.panel;

import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.model.TrackingData;
import ballantines.avionics.blackbox.util.Log;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import de.mbuse.pipes.Pipes;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;

/**
 *
 * @author mbuse
 */
public class Toolbar implements Initializable, PipeUpdateListener {
    
    private static final Log L = Log.forClass(Toolbar.class);
    
    public static Parent create(Services services) throws IOException {
        Toolbar controller = new Toolbar();
        controller.setServices(services);
        FXMLLoader loader = new FXMLLoader(PIREPForm.class.getResource("/fxml/toolbar.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Pipes.connect(isConnectedPipe, services.udpServerRunningPipe);
        Pipes.connect(recordingControlPipe, services.isRecordingPipe);
        trackingDataPipe.connectTo(services.trackingDataPipe);
        updateButtonStates();
        
    }

    @Override
    public void pipeUpdated(Pipe pipe) {
        L.pipeUpdated(pipe);
        if (isConnectedPipe == pipe || trackingDataPipe == pipe) {
            updateButtonStates();
        }
    }
    
    private void updateButtonStates() {
        Platform.runLater(new Runnable() { public void run() {
            Boolean isConnected = isConnectedPipe.get();
            if (isConnected == Boolean.TRUE) {
                connectBtn.setSelected(true);
            }
            else {
                connectBtn.setSelected(false);
            }

            TrackingData data = trackingDataPipe.get();
            if (data!=null) {
                startRecordingBtn.setDisable(data.isTracking());
                stopRecordingBtn.setDisable(data.trackingFinished);
                uploadPirepBtn.setDisable(!data.trackingFinished);
                
            }
            else {
                startRecordingBtn.setDisable(false);
                stopRecordingBtn.setDisable(true);
                uploadPirepBtn.setDisable(true);
            }
        }});
    }

    @FXML 
    public void connectBtnPressed() {
        boolean isConnected = connectBtn.selectedProperty().get();
        isConnectedPipe.set(isConnected);
    }
    
    @FXML 
    public void startRecordingBtnPressed() {
        recordingControlPipe.set(Boolean.TRUE);
    }
    
    @FXML 
    public void stopRecordingBtnPressed() {
        recordingControlPipe.set(Boolean.FALSE);
    }
    
    @FXML 
    public void uploadPirepBtnPressed() {
        
    }
    
    
    
    public void setServices(Services services) {
        this.services = services;
    }
    
    @FXML private ToggleButton connectBtn;
    @FXML private Button startRecordingBtn;
    @FXML private Button stopRecordingBtn;
    @FXML private Button uploadPirepBtn;
    
    private Pipe<Boolean> isConnectedPipe = Pipe.newInstance("isConnected", this);
    private Pipe<Boolean> recordingControlPipe = Pipe.newInstance("recordingControl", this);
    private Pipe<TrackingData> trackingDataPipe = Pipe.newInstance("trackingData", this);
    
    private Services services;
}
