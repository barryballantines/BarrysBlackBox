/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.service;

import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.log.FlightPhase;
import ballantines.avionics.blackbox.model.TrackingData;
import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.blackbox.util.Log;
import ballantines.avionics.kacars.KAcarsConfig;
import ballantines.avionics.kacars.model.Flight;
import ballantines.avionics.kacars.model.LiveUpdateData;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.util.TimerTask;

/**
 *
 * @author mbuse
 */
public class LiveUpdate implements PipeUpdateListener {
    
    private static final Log L = Log.forClass(LiveUpdate.class);
    
    private static final long PERIOD = 10 * 1000;
    
    private Services services;
    private LiveUpdateTask task;
    
    private Pipe<KAcarsConfig> kacarsConfigPipe = Pipe.newInstance("LiveUpdate.kacarsConfig", this);
    private Pipe<Boolean> liveUpdateEnabledPipe = Pipe.newInstance("LiveUpdate.enabled", this);
    
    
    public LiveUpdate(Services services) {
        this.services = services;
        kacarsConfigPipe.connectTo(services.kacarsConfigPipe);
    }

    @Override
    public void pipeUpdated(Pipe pipe) {
        L.pipeUpdated(pipe);
        
        if (pipe == kacarsConfigPipe) {
            KAcarsConfig config = kacarsConfigPipe.get();
            liveUpdateEnabledPipe.set(config.liveUpdateEnabled);
        }
        
        if (pipe == liveUpdateEnabledPipe) {
            Boolean enabled = liveUpdateEnabledPipe.get();
            if (enabled!=null) {
                Boolean isRecording = services.isRecordingPipe.get();
                
                if (enabled && (isRecording !=null) && isRecording) {
                    start();
                } else {
                    stop();
                }
            }
        }
    }
    
    
    
    public void start() {
        KAcarsConfig config = kacarsConfigPipe.get();
        if (config.liveUpdateEnabled) {
            task = new LiveUpdateTask();
            services.getTimer().schedule(task, 1000, config.liveUpdateIntervalMS);
            L.info("LiveUpdate started");
        }
    }
    
    public void stop() {
        if (task!=null) {
            task.cancel();
            task = null;
            L.info("LiveUpdate stopped");
        }
    }
    
    private class LiveUpdateTask extends TimerTask {

        @Override
        public void run() {
            FlightData flightData = services.flightDataPipe.get();
            Flight flight = services.flightBidPipe.get();
            TrackingData trackingData = services.trackingDataPipe.get();
            FlightPhase phase = services.flightPhasePipe.get();
            if (flightData!=null) {
                LiveUpdateData liveUpdate = new LiveUpdateData(flight, flightData, trackingData, phase);

                L.info("LiveUpdate: %s", liveUpdate.toString());
                if (kacarsConfigPipe.get().enabled && flight!=null) {
                    try {
                        services.getKacarsClient().liveUpdate(liveUpdate);
                    } catch (Exception e) {
                        L.error(e, "Live Update failed.");
                    }
                }
            }
            else {
                L.info("LiveUpdate: No Data");
            }
        }
    }
    
}
