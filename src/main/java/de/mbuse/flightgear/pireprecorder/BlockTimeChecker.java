/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.mbuse.flightgear.pireprecorder;

import java.util.Calendar;
import java.util.TimerTask;
import javafx.application.Platform;

/**
 *
 * @author mbuse
 */
public class BlockTimeChecker extends TimerTask {
    
    private FlightDataRetrieval retrieval;
    private PIREPForm pirepForm;
    
    public BlockTimeChecker(PIREPForm form, FlightDataRetrieval retrieval) {
        this.pirepForm = form;
        this.retrieval = retrieval;
    }
    
    @Override
    public void run() {
        double groundspeed = retrieval.getGroundspeed();
        final Calendar blockTime = retrieval.getTimeUTC();
        if (Math.abs(groundspeed) > 3.0) {
            System.out.println("GroundMovementChecker - start of the journey!");
            this.cancel();
        }
        else {
            System.out.println("GroundMovementChecker - still in parking position...");
        }
        Platform.runLater(new Runnable() {@Override public void run() {
            pirepForm.setDepartureTimeGauge(blockTime);
        }});
    }
    
}
