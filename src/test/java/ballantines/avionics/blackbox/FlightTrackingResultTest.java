/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mbuse
 */
public class FlightTrackingResultTest {
    
    
    @Test
    public void testSomeMethod() {
        FlightTrackingResult result = new FlightTrackingResult();
        result.flightTimeMinutes = 100;
        
        int[] hhmm = result.getFlightTimeHoursAndMinutes();
        String formatted = String.format("%d.%02d", hhmm[0], hhmm[1]);
        
        assertEquals("Wrong format", "1.40", formatted);
        
    }
    
}
