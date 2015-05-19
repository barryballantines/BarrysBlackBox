/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.mbuse.flightgear.pireprecorder;

import de.mbuse.flightgear.pireprecorder.udp.FlightData;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.util.TimerTask;
import javafx.application.Platform;

/**
 *
 * @author mbuse
 */
public class FuelChecker implements PipeUpdateListener<FlightData> {
    
    private PIREPForm pirepForm;
    private double lastReportedFuel;
    
    public FuelChecker(PIREPForm form) {
        super();
        this.pirepForm = form;
        this.lastReportedFuel = form.getDepartureFuelGauge();
    }

    @Override
    public void pipeUpdated(Pipe<FlightData> model) {
        FlightData data = model.get();
        if (data==null) {
            return;
        }
        try {
            double currentFuel = data.getFuel();
            //Services.get().currentFuelPipe.set(currentFuel);
            
            final double difference = currentFuel - lastReportedFuel;
            lastReportedFuel = currentFuel;

            if (difference > 0) {
                System.out.println("FuelChecker detected a fuel gain of " + difference + " lbs.");
                final double loadedFuel = pirepForm.getDepartureFuelGauge();
                
                Platform.runLater(new Runnable() { public void run() {
                    pirepForm.setDepartureFuelGauge(loadedFuel + difference);
                }});
            }
            else {
                System.out.println("Fuel Checker - everything is okay...");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
