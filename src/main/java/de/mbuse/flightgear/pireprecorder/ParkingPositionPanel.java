/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mbuse.flightgear.pireprecorder;

import de.mbuse.flightgear.connect.PropertyService;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 *
 * @author mbuse
 */
public class ParkingPositionPanel implements Initializable {

    
    public static Parent create(Services services) throws IOException {
        ParkingPositionPanel controller = new ParkingPositionPanel();
        controller.propertyService = services.getPropertyService();
        
        FXMLLoader loader = new FXMLLoader(PIREPForm.class.getResource("/fxml/parking.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }
    
    private PropertyService propertyService;
    
    @FXML private Button relocateBtn;
    @FXML private Button storePositionBtn;
    @FXML private Label airportLbl;
    @FXML private Label positionLbl;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }
    
    @FXML
    public void handleStorePositionBtnPressed() {
        String airport = propertyService.readProperty("/sim/airport/closest-airport-id");
        
        Map<String, Object> position = propertyService.readProperties(
                "/position/latitude-deg", 
                "/position/latitude-string", 
                "/position/longitude-deg", 
                "/position/longitude-string",
                "/position/altitude-ft");
        
        double hdg = propertyService.readProperty("/orientation/heading-deg");
        
        airportLbl.setText(airport);
        positionLbl.setText(position.get("/position/latitude-string") + " "
                + position.get("/position/longitude-string"));
        
        try {
            Preferences prefs = Preferences.userRoot();
            
            double lon = (double) position.get("/position/longitude-deg");
            double lat = (double) position.get("/position/latitude-deg");
            double alt = (double) position.get("/position/altitude-ft");
            
            String root = "de.mbuse.flightgear.pireprecorder.parking." + airport;
            prefs.put(root + ".longitude", "" + lon);
            prefs.put(root + ".latitude", "" + lat);
            prefs.put(root + ".heading", "" + hdg);
            prefs.put(root + ".altitude", "" + alt);
            prefs.flush();
        } catch (BackingStoreException ex) {}
        
    }
    
    @FXML
    public void handleRelocatePositionBtnPressed() {
        String lon = null;
        String lat = null;
        String hdg = null;
        String alt = null;
        
        String airport = propertyService.readProperty("/sim/airport/closest-airport-id");
        Preferences prefs = Preferences.userRoot();
        String root = "de.mbuse.flightgear.pireprecorder.parking." + airport;

        lon = prefs.get(root + ".longitude", null);
        lat = prefs.get(root + ".latitude", null);
        hdg = prefs.get(root + ".heading", null);
        alt = prefs.get(root + ".altitude", null);
        
        //System.out.print("Preferences: " + airport + ", " + lat + ", " + lon + ", " + hdg);
        
        if (lon!=null && lat!=null && hdg!=null && alt!=null) {
            Map<String, Object> properties = new HashMap<>();
            
            properties.put("/position/latitude-deg", Double.parseDouble(lat));
            properties.put("/position/longitude-deg", Double.parseDouble(lon));
            properties.put("/position/altitude-ft", Double.parseDouble(alt));
            properties.put("/orientation/heading-deg", Double.parseDouble(hdg));
            
            propertyService.writeProperties(properties);
        }
    }
    
 
}
