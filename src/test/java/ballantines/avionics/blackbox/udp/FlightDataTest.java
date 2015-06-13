/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.udp;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mbuse
 */
public class FlightDataTest {
    
    public FlightDataTest() {
    }

    @Test
    public void testGroundspeedForward() {
        FlightData data = createFlightData(0.0, 10.0, 0);
        assertEquals("Heading North", 5.9, data.getGroundSpeedForward(), 0.1);
        
        data = createFlightData(0.0, 10., 90.);
        assertEquals("Heading East", 0., data.getGroundSpeedForward(), 0.1);
        
        data = createFlightData(0.0, 10., 180.);
        assertEquals("Heading North", -5.9, data.getGroundSpeedForward(), 0.1);
        
        data = createFlightData(0.0, 10., 270.);
        assertEquals("Heading North", 0., data.getGroundSpeedForward(), 0.1);
        
    }
    
    
    private FlightData createFlightData(double speedEastFps, double speedNorthFps, double heading) {
        JSONObject json = new JSONObject();
        json.put("speed-east-fps", speedEastFps);
        json.put("speed-north-fps", speedNorthFps);
        json.put("heading-deg", heading);
        
        return new FlightData(json);
    }
}
