/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.service;

import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.TestServices;
import ballantines.avionics.blackbox.udp.FlightData;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mbuse
 */
public class FuelCheckerTest {
    
    private Services services;
    
    public FuelCheckerTest() {
    }
    
    

    @Test
    public void testFuelIncrease() {
        services = new TestServices();
        services.init();
        
        FuelChecker checker = new FuelChecker(services);
        checker.connect();
        
        changeFuel(1000); // + 1000
        changeFuel(900);  // -  100
        changeFuel(800);  // -  100
        
        checker.disconnect();
        
        assertEquals("Fuel", 1000, services.trackingDataPipe.get().departureFuel);
    }
    
    @Test
    public void testFuelDecrease() {
        services = new TestServices();
        services.init();
        
        FuelChecker checker = new FuelChecker(services);
        checker.connect();
        
        changeFuel(1000); // + 1000
        changeFuel(900);  // -  100
        changeFuel(800);  // -  100
        changeFuel(900);  // +  100
        changeFuel(850);  // -   50
        changeFuel(900);  // +   50
        
        checker.disconnect();
        
        assertEquals("Fuel", 1150, services.trackingDataPipe.get().departureFuel);
    }
    
    @Test
    public void testDisconnect() {
        services = new TestServices();
        services.init();
        
        FuelChecker checker = new FuelChecker(services);
        
        changeFuel(100);
        changeFuel(200);
        checker.connect();
        changeFuel(300);
        changeFuel(400);
        changeFuel(500);
        checker.disconnect();
        changeFuel(600);
        changeFuel(700);
        
        assertEquals("Fuel", 500, services.trackingDataPipe.get().departureFuel);
        
    }
    
    
    
    
    private void changeFuel(int fuel) {
        JSONObject json = new JSONObject();
        json.put("fuel-lbs", (double) fuel);
        services.flightDataPipe.set(new FlightData(json));
    }
}
