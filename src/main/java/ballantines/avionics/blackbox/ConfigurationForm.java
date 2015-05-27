/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ballantines.avionics.blackbox;

import ballantines.avionics.flightgear.connect.ServerConfig;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import de.mbuse.pipes.Pipes;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 *
 * @author mbuse
 */
public class ConfigurationForm implements Initializable, PipeUpdateListener<Object> {
    
    public static Parent create(Services services) throws IOException {
        ConfigurationForm controller = new ConfigurationForm();
        controller.setServices(services);
        FXMLLoader loader = new FXMLLoader(controller.getClass().getResource("/fxml/configuration.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }
    
    // === ===
    
    @FXML private TextField fgHostnameTxt;
    @FXML private TextField fgPortTxt;
    @FXML private Label fgTestFeedbackLbl;
    @FXML private TextField udpPortText;
    @FXML private CheckBox udpServerRunningCheck;
    
    private Services services;
    private final Pipe<ServerConfig> serverConfigPipe = Pipe.newInstance("configurationForm.serverConfig", this);
    private final Pipe<Boolean> udpServerRunningPipe = Pipe.newInstance("configurationForm.udpServerRunningPipe", this);
    private final Pipe<Integer> udpServerPortPipe = Pipe.newInstance("configurationForm.udpServerPort", this);
    
    // === ===

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fgHostnameTxt.setText("");
        fgPortTxt.setText("");
        
        // connecting pipes...
        Pipes.connect(services.serverConfigPipe, this.serverConfigPipe);
        Pipes.connect(services.udpServerPortPipe, this.udpServerPortPipe);
        Pipes.connect(services.udpServerRunningPipe, this.udpServerRunningPipe);
        
        
    }

    @Override
    public void pipeUpdated(Pipe<Object> pipe) {
        System.out.println("[CONFIGURATION FORM] Model updated : " + pipe.id() + " -> " + pipe.get());
        if ("configurationForm.serverConfig".equals(pipe.id())) {
            ServerConfig config = (ServerConfig) pipe.get();
            fgHostnameTxt.setText(config.getHost());
            fgPortTxt.setText("" + config.getPort()); 
        }
        else if ("configurationForm.udpServerRunningPipe".equals(pipe.id())) {
            boolean running = (boolean) pipe.get();
            udpServerRunningCheck.selectedProperty().set(running);
            udpServerRunningCheck.setText(running ? "Server running" : "Server not running");
            udpPortText.editableProperty().set(!running);
        }
    }
    
    

    public void setFlightGearTestFeedback(boolean ok, String msg) {
        if (ok) {
            fgTestFeedbackLbl.setTextFill(Color.GREEN);
        }
        else {
            fgTestFeedbackLbl.setTextFill(Color.RED);
        }
        
        fgTestFeedbackLbl.setText(msg);
        
    }
    
    @FXML
    public void flightgearConnectionChanged(ActionEvent event) {
        
            String host = fgHostnameTxt.getText();
            String portText = fgPortTxt.getText();
            
            if (host.length()==0) {
                host = "localhost";
            }
            int port = 5500;
            if (portText.length()>0) {
                try {
                    port = Integer.parseInt(portText);
                } catch (NumberFormatException e) {
                    fgPortTxt.setText("");
                }
            }
            
            // TODO: refactor this... use a pipe and a services object...
            ServerConfig config = new ServerConfig("http", host, port);
            serverConfigPipe.set(config);
            
            testFlightgearConnection(event);
    }

    @FXML
    public void testFlightgearConnection(ActionEvent event) {
        try {
            FlightDataRetrieval retrieval = services.getFlightDataRetrieval();
            
            String airport = retrieval.getAirport();
            long fuel = retrieval.getFuel();
            Calendar time =retrieval.getTime();
            
            if (airport!=null) {
                setFlightGearTestFeedback(true, "Connection successfully tested!");
            }
            return;
        } catch (Exception e) {}
        setFlightGearTestFeedback(false, "Connection failed!");
    }
    
    @FXML 
    public void udpRunningStateChanged(ActionEvent event) {
        boolean isSelected = udpServerRunningCheck.selectedProperty().getValue();
        udpServerRunningPipe.set(isSelected);
    }
    
    @FXML
    public void udpServerPortChanged(ActionEvent event) {
        String txt = udpPortText.getText();
        try {
            int port = Integer.parseInt(txt);
            udpServerPortPipe.set(port);
        } catch (NumberFormatException nfe) {}
    }

    public void setServices(Services services) {
        this.services = services;
    }
    
}
