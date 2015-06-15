/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.model;

import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mbuse
 */
public class TrackingDataTest {
    
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Test
    public void testJSONSerialization() {
        TrackingData expected = new TrackingData();
        TrackingData parsed;
        
        // empty 
        String json = expected.toString();
        System.out.println("JSON: " + json);
        parsed = TrackingData.fromString(json);
        assertEquals("Empty data", expected, parsed);
        
        // tracking started
        expected.departureAirport="EDDH";
        expected.departureTime = Calendar.getInstance(UTC);
        expected.departureFuel = 10000;
        expected.trackingStarted = true;
        
        json = expected.toString();
        System.out.println("JSON: " + json);
        parsed = TrackingData.fromString(json);
        assertEquals("Tracking started", expected, parsed);
        
        
        // tracking started
        expected.arrivalAirport="EDDH";
        expected.arrivalTime = Calendar.getInstance(UTC);
        expected.arrivalFuel = 8000;
        expected.landingRateFPM = -150;
        expected.trackingFinished = true;
        
        json = expected.toString();
        System.out.println("JSON: " + json);
        parsed = TrackingData.fromString(json);
        assertEquals("Tracking finished", expected, parsed);
        
    }
    
    @Test
    public void testCalculations() {
        Calendar startTime = Calendar.getInstance(UTC);
        Calendar finishTime = (Calendar) startTime.clone();
        finishTime.add(Calendar.HOUR_OF_DAY, 1);
        finishTime.add(Calendar.MINUTE, 45);
        
        TrackingData data = new TrackingData();
        data.departureAirport="EDDH";
        data.departureTime = startTime;
        data.departureFuel = 10000;
        data.trackingStarted = true;
        data.arrivalAirport="EDDH";
        data.arrivalTime = finishTime;
        data.arrivalFuel = 8000;
        data.landingRateFPM = -150;
        data.trackingFinished = true;
        
        assertEquals("fuelConsumption", 2000, data.getFuelConsumption());
        assertEquals("flightTimeInMinutes", 105, data.getFlightTimeInMinutes());
        assertEquals("flightTime hours", 1, data.getFlightTimeHHMM()[0]);
        assertEquals("flightTime minutes", 45, data.getFlightTimeHHMM()[1]);
        assertEquals("flightTime formatted", "1:45", data.getFlightTimeFormatted());
    }
    
    
}
