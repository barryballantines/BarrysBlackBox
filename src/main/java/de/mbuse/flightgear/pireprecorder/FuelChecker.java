/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.mbuse.flightgear.pireprecorder;

import java.util.TimerTask;
import javafx.application.Platform;

/**
 *
 * @author mbuse
 */
public class FuelChecker extends TimerTask {
    
    private FlightDataRetrieval retrieval;
    private PIREPForm pirepForm;
    private long lastReportedFuel;
    
    public FuelChecker(PIREPForm form, FlightDataRetrieval retrieval) {
        super();
        this.pirepForm = form;
        this.retrieval = retrieval;
        this.lastReportedFuel = form.getDepartureFuelGauge();
    }

    @Override
    public void run() {
        try {
            long currentFuel = retrieval.getFuel();
            Services.get().currentFuelPipe.set(currentFuel);
            
            final long difference = currentFuel - lastReportedFuel;
            lastReportedFuel = currentFuel;

            if (difference > 0) {
                System.out.println("FuelChecker detected a fuel gain of " + difference + " lbs.");
                final long loadedFuel = pirepForm.getDepartureFuelGauge();
                
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
