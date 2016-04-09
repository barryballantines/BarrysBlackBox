/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.panel;

import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.model.Command;
import ballantines.avionics.blackbox.model.TrackingData;
import ballantines.avionics.blackbox.util.Log;
import ballantines.avionics.kacars.KAcarsConfig;
import com.sun.javafx.stage.StageHelper;
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
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

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
        kacarsConfigPipe.connectTo(services.kacarsConfigPipe);
        updateButtonStates();
        
    }

    @Override
    public void pipeUpdated(Pipe pipe) {
        L.pipeUpdated(pipe);
        if (isConnectedPipe == pipe || trackingDataPipe == pipe || kacarsConfigPipe == pipe) {
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
            KAcarsConfig kacarsConfig = kacarsConfigPipe.get();
            if (kacarsConfig!=null) {
                uploadPirepBtn.setDisable(!kacarsConfig.enabled);
                downloadFlightBtn.setDisable(!kacarsConfig.enabled);
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
        services.fireCommand(Command.START_RECORDING);
    }
    
    @FXML 
    public void stopRecordingBtnPressed() {
        services.fireCommand(Command.FINISH_RECORDING);
    }
    
    @FXML 
    public void uploadPirepBtnPressed() {
        services.fireCommand(Command.UPLOAD_PIREP);
    }
    
    @FXML
    public void downloadBtnPressed() {
        services.fireCommand(Command.DOWNLOAD_BID);
    }
    
    public void setServices(Services services) {
        this.services = services;
    }
    
    @FXML private ToggleButton connectBtn;
    @FXML private Button startRecordingBtn;
    @FXML private Button stopRecordingBtn;
    @FXML private Button uploadPirepBtn;
    @FXML private Button downloadFlightBtn;
    
    private Pipe<Boolean> isConnectedPipe = Pipe.newInstance("isConnected", this);
    private Pipe<Boolean> recordingControlPipe = Pipe.newInstance("recordingControl", this);
    private Pipe<TrackingData> trackingDataPipe = Pipe.newInstance("trackingData", this);
    private Pipe<KAcarsConfig> kacarsConfigPipe = Pipe.newInstance("kacarsConfig", this);
    
    private Services services;
}
