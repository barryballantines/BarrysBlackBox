/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.mbuse.flightgear.pireprecorder;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 *
 * @author mbuse
 */
public class ConfigurationForm implements Initializable {
    
    public static Parent create(FlightDataRetrieval retrieval) throws IOException {
        ConfigurationForm controller = new ConfigurationForm();
        controller.setFlightDataRetrieval(retrieval);
        FXMLLoader loader = new FXMLLoader(controller.getClass().getResource("/fxml/configuration.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }
    
    // === ===
    
    @FXML private TextField fgHostnameTxt;
    @FXML private TextField fgPortTxt;
    @FXML private Label fgTestFeedbackLbl;
    
    private FlightDataRetrieval retrieval;

    // === ===
    public void setFlightDataRetrieval(FlightDataRetrieval retrieval) {
        this.retrieval = retrieval;
    }

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Preferences prefs = Preferences.userRoot();
        
        String host = prefs.get("de.mbuse.flightgear.pireprecorder.fgHost", "localhost");
        long port = prefs.getLong("de.mbuse.flightgear.piperecorder.fgPort", 5500);
        
        fgHostnameTxt.setText(host);
        fgPortTxt.setText(""+port);
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
            
            Configuration config = (Configuration) retrieval;
            config.setHost(host);
            config.setPort(port);
            
            try {
                Preferences prefs = Preferences.userRoot();
                prefs.put("de.mbuse.flightgear.pireprecorder.fgHost", host);
                prefs.putLong("de.mbuse.flightgear.pireprecorder.fgPort", port);
                prefs.flush();
            } catch (BackingStoreException ex) {}
            
            testFlightgearConnection(event);
    }

    @FXML
    public void testFlightgearConnection(ActionEvent event) {
        try {
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
    
}
