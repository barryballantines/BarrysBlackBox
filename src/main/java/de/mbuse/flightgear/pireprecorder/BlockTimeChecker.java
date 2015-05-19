/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.mbuse.flightgear.pireprecorder;

import de.mbuse.flightgear.pireprecorder.udp.FlightData;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.TimerTask;
import javafx.application.Platform;

/**
 *
 * @author mbuse
 */
public class BlockTimeChecker implements PipeUpdateListener<FlightData> {
    
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    
    private PIREPForm pirepForm;
    
    public BlockTimeChecker(PIREPForm form) {
        this.pirepForm = form;
    }
    
    public Calendar getUTCTime() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(UTC);
        return cal;
    }

    @Override
    public void pipeUpdated(Pipe<FlightData> pipe) {
        FlightData data = pipe.get();
        if (data==null) {
            return;
        }
        double groundspeed = data.getGroundSpeed();
        final Calendar blockTime = getUTCTime();
        if (Math.abs(groundspeed) > 3.0) {
            System.out.println("GroundMovementChecker - start of the journey!");
            pipe.removeChangeListener(this);
        }
        else {
            System.out.println("GroundMovementChecker - still in parking position...");
        }
        Platform.runLater(new Runnable() {@Override public void run() {
            pirepForm.setDepartureTimeGauge(blockTime);
        }});
    }
    
}
