/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox;

import ballantines.avionics.flightgear.connect.PropertyService;
import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.blackbox.util.Log;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.application.Platform;
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
public class ParkingPositionPanel implements Initializable, PipeUpdateListener<FlightData> {

    private static Log L = Log.forClass(ParkingPositionPanel.class);
    
    public static Parent create(Services services) throws IOException {
        ParkingPositionPanel controller = new ParkingPositionPanel();
        controller.setServices(services);
        
        FXMLLoader loader = new FXMLLoader(PIREPForm.class.getResource("/fxml/parking.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }
    
    private Services services;
    
    private Pipe<FlightData> flightDataPipe = Pipe.newInstance("parkingPosition.flightData", this);
    
    @FXML private Button relocateBtn;
    @FXML private Button storePositionBtn;
    @FXML private Label airportLbl;
    @FXML private Label positionLbl;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        flightDataPipe.connectTo(services.flightDataPipe);
        
    }

    @Override
    public void pipeUpdated(Pipe<FlightData> pipe) {
        L.pipeUpdated(pipe);
        final String airport = pipe.get().getClosestAirport();
        
        if(airport != null && !airport.equals(airportLbl.getText())) {
            final Location l = readLocationFromPreferences(airport);
            
            Platform.runLater(new Runnable() { @Override public void run() {
                
                airportLbl.setText(airport);
                positionLbl.setText((l!=null) 
                        ? l.lat + " " + l.lon 
                        : "N/A");
                relocateBtn.setDisable(l==null);
            }});
        }
    }
    
    
    
    @FXML
    public void handleStorePositionBtnPressed() {
        PropertyService propertyService = services.getPropertyService();
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
        
        PropertyService propertyService = services.getPropertyService();
        
        String airport = propertyService.readProperty("/sim/airport/closest-airport-id");
        
        Location l = readLocationFromPreferences(airport);
        
        if (l!=null) {
            Map<String, Object> properties = new HashMap<>();
            
            properties.put("/position/latitude-deg", Double.parseDouble(l.lat));
            properties.put("/position/longitude-deg", Double.parseDouble(l.lon));
            properties.put("/position/altitude-ft", Double.parseDouble(l.alt));
            properties.put("/orientation/heading-deg", Double.parseDouble(l.hdg));
            
            propertyService.writeProperties(properties);
        }
    }
    
    private Location readLocationFromPreferences(String airport) {
        Location l = new Location();
        Preferences prefs = Preferences.userRoot();
        String root = "de.mbuse.flightgear.pireprecorder.parking." + airport;

        l.lon = prefs.get(root + ".longitude", null);
        l.lat = prefs.get(root + ".latitude", null);
        l.hdg = prefs.get(root + ".heading", null);
        l.alt = prefs.get(root + ".altitude", null);
        
        return (l.lon!=null && l.lat!=null && l.hdg!=null && l.alt!=null)
                ? l : null;
    }

    public void setServices(Services services) {
        this.services = services;
    }
    
    
    private static class Location {
        String lon = null;
        String lat = null;
        String hdg = null;
        String alt = null;
    }
    
}
