/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mbuse.flightgear.pireprecorder;

import de.mbuse.flightgear.pireprecorder.udp.FlightData;
import de.mbuse.pipes.Pipe;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mbuse
 */
public class LandingRateServiceTest {
    
    public LandingRateServiceTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of pipeUpdated method, of class LandingRateService.
     */
    @Test
    public void testSimulatedFlight() {
        
        LandingRateService lrs = new LandingRateService();
        
        lrs.statusPipe.set(LandingRateService.Status.GROUND);
        
        // taxi 
        System.out.println("-- taxi --");
        simulate(lrs.flightDataPipe, 10.0, 0.5, 20);
        simulate(lrs.flightDataPipe, 10.0, -0.5, 10);
        
        // takeoff
        System.out.println("-- t/o --");
        simulate(lrs.flightDataPipe, 0.001, 1200.0, 30);
        
        // cruise
        System.out.println("-- cruise --");
        simulate(lrs.flightDataPipe, 0.00001, 0., 50);
        
        // descent
        System.out.println("-- descent --");
        simulate(lrs.flightDataPipe, 0.001, -1400., 20);
        
        // touchdown
        System.out.println("-- touchdown --");
        simulate(lrs.flightDataPipe, 0.001, -400., 10);
        simulate(lrs.flightDataPipe, 10.0, -200., 1);
        
        // taxi
        System.out.println("-- taxi --");
        simulate(lrs.flightDataPipe, 10.0, 0.5, 20);
        simulate(lrs.flightDataPipe, 10.0, -0.5, 10);
        
        assertEquals("Landing Rate is wrong.", -400., lrs.landingRate.get(), 0.01);
        
    }
    
    
    private void simulate(Pipe<FlightData> pipe, double wow, double descentRate, int n) {
        JSONObject json = new JSONObject();
        json.put("vertical-speed", descentRate);
        json.put("wow", new JSONArray(new double[] { wow , wow, wow }));
        
        for (int i=0; i<n; i++) {
            pipe.set(new FlightData(json));
        }
    }
}
