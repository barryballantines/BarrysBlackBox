/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ballantines.avionics.blackbox.service;

import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.model.TrackingData;
import ballantines.avionics.blackbox.panel.PIREPForm;
import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.blackbox.util.Log;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.util.TimerTask;
import javafx.application.Platform;

/**
 *
 * @author mbuse
 */
public class FuelChecker implements PipeUpdateListener<FlightData> {
    
    private static Log L = Log.forClass(FuelChecker.class);
    
    private Services services;
    private int lastReportedFuel;
    
    public FuelChecker(Services services) {
        super();
        this.services = services;
        this.lastReportedFuel = 0;
    }
    
    public void connect() {
        this.lastReportedFuel = 0;
        this.services.flightDataPipe.addListener(this);
    }
    
    public void disconnect() {
        this.services.flightDataPipe.removeChangeListener(this);
    }

    @Override
    public void pipeUpdated(Pipe<FlightData> model) {
        FlightData data = model.get();
        if (data==null) {
            return;
        }
        try {
            // get fuel from flight data
            int currentFuel = (int) data.getFuel();
            // does fuel increase or decrease???
            int difference = (int) (currentFuel - lastReportedFuel);
            lastReportedFuel = currentFuel;

            if (difference > 0) {
                L.info("FuelChecker detected a fuel gain of %d lbs.", difference);
                TrackingData trackData = new TrackingData(trackingDataPipe().get());
                int loadedFuel = (trackData==null) ? 0 : trackData.departureFuel;
                trackData.departureFuel = loadedFuel + difference;
                trackingDataPipe().set(trackData);
            }
            else {
                L.trace("Fuel Checker - everything is okay...");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    protected Pipe<TrackingData> trackingDataPipe() {
        return services.trackingDataPipe;
    }
}
