/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox;

import ballantines.avionics.blackbox.log.FlightLogger;
import ballantines.avionics.blackbox.log.LogEvent;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;

/**
 *
 * @author mbuse
 */
public class ACARSLogPanel implements Initializable, PipeUpdateListener<LogEvent> {

    
    public static Parent create(Services services) throws IOException {
        ACARSLogPanel controller = new ACARSLogPanel();
        controller.setServices(services);
        
        FXMLLoader loader = new FXMLLoader(ACARSLogPanel.class.getResource("/fxml/acarsLog.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        FlightLogger logger = new FlightLogger();
        
        logger.isRecordingPipe.connectTo(services.isRecordingPipe);
        logger.dataPipe.connectTo(services.flightDataPipe);
        logger.eventPipe.addListener(this);
        
    }

    
    @Override
    public void pipeUpdated(Pipe<LogEvent> pipe) {
        logField.appendText(pipe.get().getFormattedMessage() + "\n");
    }

    
    
    public void setServices(Services services) {
        this.services = services;
    }

    public Services getServices() {
        return services;
    }
    
    
    @FXML
    private TextArea logField;
    private Services services;
}
