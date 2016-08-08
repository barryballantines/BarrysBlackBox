/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.panel;

import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.model.Position;
import ballantines.avionics.flightgear.connect.PropertyService;
import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.blackbox.util.Log;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
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
import javafx.scene.control.TextField;
import org.json.JSONObject;

/**
 *
 * @author mbuse
 */
public class PositionPanel implements Initializable, PipeUpdateListener<FlightData> {

    private static Log L = Log.forClass(PositionPanel.class);
    
    public static Parent create(Services services) throws IOException {
        PositionPanel controller = new PositionPanel();
        controller.setServices(services);
        
        FXMLLoader loader = new FXMLLoader(PIREPForm.class.getResource("/fxml/position.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }
    
    private Services services;
    
    private Pipe<FlightData> flightDataPipe = Pipe.newInstance("parkingPosition.flightData", this);
    
    @FXML private Button relocateBtn;
    @FXML private Button storePositionBtn;
    @FXML private Label airportLbl;
    @FXML private Label positionLbl;
    @FXML private Label headingLbl;
    
    @FXML private TextField lastPositionUI;
    @FXML private TextField lastHeadingUI;
    @FXML private TextField lastAltitudeUI;
    @FXML private TextField lastSpeedUI;
    @FXML private TextField lastFuelUI;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        flightDataPipe.connectTo(services.flightDataPipe);
        try {
            FlightData data = services.getPersistenceService().readLatestFlightData();
            showLastFlightData(data);
        } catch (Exception ex) {
            showLastFlightData(null);
        }
    }

    @Override
    public void pipeUpdated(Pipe<FlightData> pipe) {
        L.pipeUpdated(pipe);
        FlightData data = pipe.get();
        if (data==null) {
            data = services.getPersistenceService().readLatestFlightData();
        }
        else {
            services.getPersistenceService().writeLatestFlightData(data);
        }
        
        showLastFlightData(data);
        
        if (data==null) {
            return;
        }
        
        final String airport = data.getClosestAirport();

        if(airport != null && !airport.equals(airportLbl.getText())) {
            final Position pos = services.getPersistenceService().readKnownParkingPosition(airport);

            Platform.runLater(new Runnable() { @Override public void run() {
                boolean hasPosition = Double.isFinite(pos.lat) && Double.isFinite(pos.lon);
                
                airportLbl.setText(airport);
                positionLbl.setText(hasPosition 
                        ? String.format(Locale.US, "Lon: %.8f  Lat: %.8f", pos.lon, pos.lat) 
                        : "N/A");
                headingLbl.setText(Double.isFinite(pos.hdg)
                        ? String.format("%03.0f", pos.hdg)
                        : "N/A");
                relocateBtn.setDisable(!hasPosition);
            }});
        }

    }
    
    private void showLastFlightData(final FlightData data) {
        Platform.runLater(new Runnable() { public void run() {
            if (data!=null) {
                lastPositionUI.setText(
                        String.format(Locale.US, "Lon: %.8f  Lat: %.8f", 
                            data.getLongitude(),data.getLatitude()));
                lastHeadingUI.setText(String.format("%03.0f", data.getHeading()));
                lastSpeedUI.setText(String.format("%3.0f", data.getGroundSpeed()));
                lastFuelUI.setText(String.format("%.0f", data.getFuel()));
                lastAltitudeUI.setText(String.format("%.0f", data.getAltitude()));
            } else {
                lastPositionUI.setText("N/A");
                lastHeadingUI.setText("N/A");
                lastSpeedUI.setText("N/A");
                lastFuelUI.setText("N/A");
                lastAltitudeUI.setText("N/A");
            }
        }});
    }
    
    @FXML
    public void handleStorePositionBtnPressed() {
        PropertyService propertyService = services.getPropertyService();
        String airport = propertyService.readProperty("/sim/airport/closest-airport-id");
        Position pos = new Position();
        
        Map<String, Object> position = propertyService.readProperties(
                "/position/latitude-deg", 
                "/position/latitude-string", 
                "/position/longitude-deg", 
                "/position/longitude-string",
                "/position/altitude-ft");
        
        pos.hdg = propertyService.readProperty("/orientation/heading-deg");     
        pos.lon = (double) position.get("/position/longitude-deg");
        pos.lat = (double) position.get("/position/latitude-deg");
        pos.alt = (double) position.get("/position/altitude-ft");
        
        services.getPersistenceService().writeKnownParkingPosition(airport, pos);
    
        airportLbl.setText(airport);
        positionLbl.setText(String.format(Locale.US, "Lon: %.8f  Lat: %.8f", pos.lon, pos.lat));
        headingLbl.setText(Double.isFinite(pos.hdg)
                        ? String.format("%03.0f", pos.hdg)
                        : "N/A");
    }
    
    @FXML
    public void handleRelocatePositionBtnPressed() {
        
        PropertyService propertyService = services.getPropertyService();
        
        String airport = propertyService.readProperty("/sim/airport/closest-airport-id");
        
        Position pos = services.getPersistenceService().readKnownParkingPosition(airport);
        
        if (pos!=null) {
            Map<String, Object> properties = new HashMap<>();
            
            setOptionalDoubleValues(properties, "/position/latitude-deg", pos.lat);
            setOptionalDoubleValues(properties, "/position/longitude-deg", pos.lon);
            setOptionalDoubleValues(properties, "/position/altitude-ft", pos.alt);
            setOptionalDoubleValues(properties, "/orientation/heading-deg", pos.hdg);
            
            propertyService.writeProperties(properties);
        }
    }
    
    private void setOptionalDoubleValues(Map<String, Object> map, String key, Double value) {
        if (Double.isFinite(value)) {
            map.put(key, value);
        }
    }
    
    @FXML
    public void handleRecoverLastPositionBtnPressed() {
        FlightData data = services.getPersistenceService().readLatestFlightData();
        if (data != null) {
            Map<String, Object> properties = new HashMap<>();
            
            properties.put("/position/latitude-deg", data.getLatitude());
            properties.put("/position/longitude-deg", data.getLongitude());
            properties.put("/position/altitude-ft", data.getAltitude());
            properties.put("/orientation/heading-deg", data.getHeading());
            properties.put("/velocities/groundspeed-kt", data.getGroundSpeed());
            
            PropertyService propertyService = services.getPropertyService();
            propertyService.writeProperties(properties);
        }
    }

    public void setServices(Services services) {
        this.services = services;
    }
    
}
