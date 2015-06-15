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
        return Calculus.average(rates);
    }
    
    public static enum Status {
        GROUND,
        AIRBORNE;
    }
}
