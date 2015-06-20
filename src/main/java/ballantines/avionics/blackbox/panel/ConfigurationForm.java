/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ballantines.avionics.blackbox.panel;

import ballantines.avionics.blackbox.service.FlightDataRetrieval;
import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.util.Log;
import ballantines.avionics.flightgear.connect.ServerConfig;
import ballantines.avionics.kacars.KAcarsClient;
import ballantines.avionics.kacars.KAcarsConfig;
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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 *
 * @author mbuse
 */
public class ConfigurationForm implements Initializable, PipeUpdateListener {
    
    private static Log L = Log.forClass(ConfigurationForm.class);
    
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
    
    @FXML private TextField kacarsUrlTxt;
    @FXML private TextField kacarsPilotIDTxt;
    @FXML private PasswordField kacarsPasswordTxt;
    @FXML private CheckBox kacarsEnabledCheck;
    @FXML private TextField kacarsLUIntervalText;
    @FXML private CheckBox kacarsLUEnabledCheck;
    @FXML private Label kacarsMessageLbl;
    
    
    private Services services;
    private final Pipe<ServerConfig> serverConfigPipe = Pipe.newInstance("configurationForm.serverConfig", this);
    private final Pipe<Boolean> udpServerRunningPipe = Pipe.newInstance("configurationForm.udpServerRunningPipe", this);
    private final Pipe<Integer> udpServerPortPipe = Pipe.newInstance("configurationForm.udpServerPort", this);
    private final Pipe<KAcarsConfig> kacarsConfigPipe = Pipe.newInstance("configurationForm.kacarsConfig", this);
    
    // === ===

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fgHostnameTxt.setText("");
        fgPortTxt.setText("");
        
        // connecting pipes...
        Pipes.connect(services.serverConfigPipe, this.serverConfigPipe);
        Pipes.connect(services.udpServerPortPipe, this.udpServerPortPipe);
        Pipes.connect(services.udpServerRunningPipe, this.udpServerRunningPipe);
        Pipes.connect(services.kacarsConfigPipe, this.kacarsConfigPipe);
        
        
    }

    @Override
    public void pipeUpdated(Pipe pipe) {
        L.pipeUpdated(pipe);
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
        else if (pipe == this.kacarsConfigPipe) {
            KAcarsConfig config = kacarsConfigPipe.get();
            kacarsUrlTxt.setText(config.url);
            kacarsPilotIDTxt.setText(config.pilotID);
            kacarsPasswordTxt.setText(config.password);
            kacarsEnabledCheck.setText(config.enabled ? "Enabled" : "Not enabled");
            kacarsEnabledCheck.selectedProperty().set(config.enabled);
            
            kacarsMessageLbl.setTextFill(Color.BLACK);
            kacarsMessageLbl.setText("Press button to test connection.");
            
            kacarsLUIntervalText.setText(String.format("%d", config.liveUpdateIntervalMS / 1000));
            kacarsLUEnabledCheck.selectedProperty().set(config.liveUpdateEnabled);
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
        } catch (Exception e) {
            L.error(e, "testFlightgearConnection failed");
        }
        setFlightGearTestFeedback(false, "Connection failed!");
    }
    
    @FXML 
    public void udpRunningStateChanged(ActionEvent event) {
        boolean isSelected = udpServerRunningCheck.selectedProperty().getValue();
        L.info("UDP Server state changed to %b (true = running)", isSelected);
        udpServerRunningPipe.set(isSelected);
    }
    
    @FXML
    public void udpServerPortChanged(ActionEvent event) {
        String txt = udpPortText.getText();
        try {
            int port = Integer.parseInt(txt);
            L.info("UDP Server port changed to %d", port);
            udpServerPortPipe.set(port);
        } catch (NumberFormatException nfe) {
            L.error(nfe, "udpServerPortChanged failed");
        }
    }

    @FXML void kacarsConfigChanged(ActionEvent event) {
        KAcarsConfig config = new KAcarsConfig();
        config.url = kacarsUrlTxt.getText();
        config.pilotID = kacarsPilotIDTxt.getText();
        config.password = kacarsPasswordTxt.getText();
        config.enabled = kacarsEnabledCheck.selectedProperty().getValue();
        config.liveUpdateIntervalMS = Integer.parseInt(kacarsLUIntervalText.getText()) * 1000;
        config.liveUpdateEnabled = kacarsLUEnabledCheck.selectedProperty().getValue();
        L.info("kACARS configuration changed: %s", config.toString());
        kacarsConfigPipe.set(config);
    }
    
    @FXML void testKacarsConnection(ActionEvent event) {
        KAcarsClient client = services.getKacarsClient();
        try {
            boolean success = client.verify();
            kacarsMessageLbl.setTextFill(success ? Color.GREEN : Color.ORANGERED);
            kacarsMessageLbl.setText(success ? "Connection verified" : "Wrong pilotID or password.");
        } catch(Exception ex) {
            kacarsMessageLbl.setTextFill(Color.RED);
            kacarsMessageLbl.setText("Error: " + ex.getMessage());
            L.error(ex, "Error while testing KACARS connection");
            ex.printStackTrace();
        }
    }
    
    public void setServices(Services services) {
        this.services = services;
    }
    
}
