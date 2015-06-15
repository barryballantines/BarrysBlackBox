/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ballantines.avionics.blackbox.service;

import ballantines.avionics.blackbox.panel.PIREPForm;
import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.blackbox.util.Log;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.util.Calendar;
import java.util.TimeZone;
import javafx.application.Platform;

/**
 *
 * @author mbuse
 */
public class BlockTimeChecker implements PipeUpdateListener<FlightData> {
    
    private static Log L = Log.forClass(BlockTimeChecker.class);
    
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
        L.pipeUpdated(pipe);
        FlightData data = pipe.get();
        if (data==null) {
            return;
        }
        double groundspeed = data.getGroundSpeed();
        final Calendar blockTime = getUTCTime();
        if (Math.abs(groundspeed) > 3.0) {
            L.info("GroundMovementChecker - start of the journey!");
            pipe.removeChangeListener(this);
        }
        else {
            L.debug("GroundMovementChecker - still in parking position...");
        }
        Platform.runLater(new Runnable() {@Override public void run() {
            pirepForm.setDepartureTimeGauge(blockTime);
        }});
    }
    
}
