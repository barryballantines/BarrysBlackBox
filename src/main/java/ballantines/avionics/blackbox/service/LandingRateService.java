/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.service;

import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.blackbox.util.Buffer;
import ballantines.avionics.blackbox.util.Calculus;
import ballantines.avionics.blackbox.util.Log;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.util.Arrays;

/**
 *
 * @author mbuse
 */
public class LandingRateService implements PipeUpdateListener{
    
    private static Log L = Log.forClass(LandingRateService.class);
    
    public final Pipe<FlightData> flightDataPipe = Pipe.newInstance("landingRateService.flightData", this);
    public final Pipe<Status> statusPipe = Pipe.newInstance("landingRateService.status", Status.GROUND, this);
    public final Pipe<Double> landingRate = Pipe.newInstance("landingRateService.landingRate", this);
    
    private Buffer descentRateBuffer = new Buffer(4);

    
    public void reset() {
        L.info("Resetting LandingRateService");
        statusPipe.set(Status.GROUND);
        landingRate.set(null);
    }
    
    @Override
    public void pipeUpdated(Pipe pipe) {
        L.pipeUpdated(pipe);
        if (pipe == flightDataPipe) {
            flightDataUpdated(flightDataPipe.get());
        }
        else if (pipe == statusPipe) {
            statusUpdated(statusPipe.get());
        }
        else if (pipe == landingRate) {
            
        }
    }
    
    private void flightDataUpdated(FlightData data) {
        double[] wows = data.getWoW();
        double maxWoW = Calculus.max(wows);
        if (Status.AIRBORNE == statusPipe.get()) {
            
            if (maxWoW > 0.1) {
                statusPipe.set(Status.GROUND);
            }
            else {
                double descentRate = data.getVerticalSpeedFPS();
                statusPipe.set(Status.AIRBORNE);
                descentRateBuffer.put(descentRate);
            }
        }
        else {
            if (maxWoW <= 0.1) {
                statusPipe.set(Status.AIRBORNE);
            }
            else {
                statusPipe.set(Status.GROUND);
            }
        }
    }
    
    private void statusUpdated(Status status) {
        if (status==Status.GROUND) {
            // landed...
            double rate = calculateLandingRate();
            L.info("Landing rate: %.3f fps", rate);
            landingRate.set(rate);
        }
    }
    
    private double calculateLandingRate() {
        double[] rates = descentRateBuffer.getValues();
        double avg = Calculus.average(rates);
        if (L.isInfo()) {
            L.info("Calculating Landing rate from: avg %s = %.3f", Arrays.toString(rates), avg);
        }
        return avg;
    }
    
    public static enum Status {
        GROUND,
        AIRBORNE;
    }
}
