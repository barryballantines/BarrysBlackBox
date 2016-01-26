/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.log;

import ballantines.avionics.blackbox.FlightSimulator;
import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.TestServices;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import org.junit.Test;

/**
 *
 * @author mbuse
 */
public class FlightLoggerTest implements PipeUpdateListener<LogEvent>{
    
    
    @Test
    public void testNormalFlight() {
        Services services = TestServices.get();
        services.init();
        FlightLogger logger = new FlightLogger(services);
        FlightSimulator sim = new FlightSimulator(services.flightDataPipe);
        
        services.isRecordingPipe.set(Boolean.TRUE);
        
        sim.simulateParking(10);
        sim.simulatePushback(10);
        sim.simulateTaxi(30);
        sim.simulateTakeOff(20, 150., 100.);
        sim.simulateCruise(100, 250., 100.);
        sim.simulateCruise(100, 300., 0.00001);
        sim.simulateCruise(100, 250., -100.);
        sim.simulateLanding(50, 140., -30.);
        sim.simulateTaxi(30);
        sim.simulateParking(10);
        
        
        services.isRecordingPipe.set(Boolean.FALSE);
        
        for (LogEvent event : logger.getEvents()) {
            System.out.println("[FlightLoggerTest] : " + event.getFormattedMessage());
        }
        
               
    }

    @Override
    public void pipeUpdated(Pipe<LogEvent> model) {
        System.out.println("[FlightLoggerTest] LOG : " + model.get().getFormattedMessage());
    }
    
    
    
}
