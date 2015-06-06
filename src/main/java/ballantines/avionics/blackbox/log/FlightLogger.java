/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.log;

import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.blackbox.util.Buffer;
import ballantines.avionics.blackbox.util.Calculus;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;

/**
 *
 * @author mbuse
 */
public class FlightLogger implements PipeUpdateListener {
    
    private static final double CLIMB_THRESHOLD = 200.0;
    private static final double TAKEOFF_THRESHOLD = 50.0;
    private static final double TAXI_THRESHOLD = 0.3;
    
    public final Pipe<Boolean> isRecordingPipe = Pipe.newInstance("pirepForm.isRecording", false, this);
    public final Pipe<FlightData> dataPipe = Pipe.newInstance("flightLogger.data");
    public final Pipe<FlightPhase> phasePipe = Pipe.newInstance("flightLogger.phase", null);
    public final Pipe<LogEvent> eventPipe = Pipe.newInstance("flightLogger.event", this);
    
    private final Pipe<Double> avgVerticalSpeedPipe = Pipe.newInstance("flightLogger.avgVerticalSpeed", 0.0, this);
    
    private double maxClimbRate = 0.0;
    private double maxDescentRate = 0.0;
    
    private Buffer averageVerticalSpeedBuffer = new Buffer(30);

    @Override
    public void pipeUpdated(Pipe pipe) {
        System.out.println("[FLIGHTLOGGER] Pipe updated : " + pipe.id() + " -> " + pipe.get());   
        
        if (pipe == isRecordingPipe) {
            Boolean isRecording = isRecordingPipe.get();
            if (isRecording!=null && isRecording ) {
                dataPipe.addListener(this);
            }
            else {
                dataPipe.removeChangeListener(this);
            }
        }
        
        if (pipe == dataPipe) {
            flightDataUpdated();
        }
        
        if (pipe == avgVerticalSpeedPipe) {
            verticalSpeedUpdated();
        }
    }
    
    
    private void verticalSpeedUpdated() {
        FlightPhase phase = phasePipe.get();
        double vs = avgVerticalSpeedPipe.get();
        FlightPhase nextPhase = phaseTransition(phase, vs);
        phasePipe.set(nextPhase);
    }
    
    private void flightDataUpdated() {
        FlightPhase phase = phasePipe.get();
        FlightData data = dataPipe.get();
        
        // PHASE
        if (phase==null) {
            initializeFlightPhase(data);
        }
        else {
            FlightPhase nextPhase = phaseTransition(phase, data);
            phasePipe.set(nextPhase);
        }
        storeVerticalSpeed(data);
    }
    
    private void storeVerticalSpeed(FlightData data) {
        averageVerticalSpeedBuffer.put(data.getVerticalSpeedFPM());
        double avg = averageVerticalSpeedBuffer.getAverage();
        avg = Math.round(avg/100.0) * 100.0;
        avgVerticalSpeedPipe.set(avg);
    }
    
    private FlightPhase phaseTransition(FlightPhase phase, FlightData data) {
        int gs = (int) (data.getGroundSpeed());
        switch (phase) {
            case BOARDING :
                if (gs < -TAXI_THRESHOLD) {
                    postEvent("Pushing back.");
                    return FlightPhase.PUSHBACK;
                }
                else if (gs > TAXI_THRESHOLD) {
                    postEvent("Taxiing to runway.");
                    return FlightPhase.TAXI;
                } 
                break;
            case PUSHBACK :
                if (gs > TAXI_THRESHOLD) {
                    postEvent("Taxiing to runway.");
                    return FlightPhase.TAXI;
                }
                break;
            case TAXI :
                if (gs >= TAKEOFF_THRESHOLD) {
                    postEvent("Takeoff.");
                    return FlightPhase.TAKEOFF;
                }
                break;
            case LANDING : 
                if (gs < TAKEOFF_THRESHOLD) {
                    postEvent("Landed. Taxiing to gate.");
                    return FlightPhase.TAXI;
                }
                break;
            case TAKEOFF :
                double wow = Calculus.max(data.getWoW());
                if (wow < 0.5) {
                    postEvent("Taking off at %d kts.", gs);
                    return FlightPhase.CLIMB;
                }
                break;
            case DESCEND :
            case CRUISE :
                double wow = Calculus.max(data.getWoW());
                if (wow > 0.5) {
                    postEvent("Touchdown.", gs);
                    return FlightPhase.LANDING;
                }
                break;
        }
        return phase;
    }
    
    private FlightPhase phaseTransition(FlightPhase phase, double vsfpm) {
        switch (phase) {
            case TAKEOFF :
                if (vsfpm >= CLIMB_THRESHOLD) {
                    maxClimbRate = vsfpm;
                    return FlightPhase.CLIMB;
                }
                break;
            case CLIMB :
                maxClimbRate = Math.max(vsfpm, maxClimbRate);
                if (vsfpm <= -CLIMB_THRESHOLD) {
                    postEvent("TOC reached at %d ft.", (int) dataPipe.get().getAltitude());
                    postEvent("Starting descend.");
                    return FlightPhase.DESCEND;
                }
                if (vsfpm > -CLIMB_THRESHOLD && vsfpm < CLIMB_THRESHOLD) {
                    postEvent("TOC reached at %d ft.", (int) dataPipe.get().getAltitude());
                    postEvent("Cruising at ground speed %d kts.", (int) dataPipe.get().getGroundSpeed());
                    return FlightPhase.CRUISE;
                }
                break;
            case CRUISE :
                if (vsfpm >= CLIMB_THRESHOLD) {
                    postEvent("Climbing at %d ft.", (int) dataPipe.get().getAltitude());
                    return FlightPhase.CLIMB;
                }
                if (vsfpm <= -CLIMB_THRESHOLD) {
                    postEvent("Top of descent reached at %d ft.", (int) dataPipe.get().getAltitude());
                    return FlightPhase.DESCEND;
                }
                break;
            case DESCEND :
                if (vsfpm > -CLIMB_THRESHOLD && vsfpm < CLIMB_THRESHOLD) {
                    postEvent("Cruising at %d ft, ground speed %d kts.", 
                            (int) dataPipe.get().getAltitude(),
                            (int) dataPipe.get().getGroundSpeed());
                    return FlightPhase.CRUISE;
                }
                break;
            case LANDING :
                if (vsfpm >= CLIMB_THRESHOLD) {
                    postEvent("Go around!");
                    return FlightPhase.TAKEOFF;
                }
        }
        return phase;
    }
    
    private void initializeFlightPhase(FlightData data) {
        // is on ground?
        if (Calculus.max(data.getWoW()) > 0.5) {
            double gs = data.getGroundSpeed();
            // is parking?
            if (gs < TAXI_THRESHOLD && gs > -TAXI_THRESHOLD) {
                postEvent("At the gate. Boarding.");
                phasePipe.set(FlightPhase.BOARDING);
            }
            else if (gs > TAKEOFF_THRESHOLD) {
                postEvent("Takeoff.");
                phasePipe.set(FlightPhase.TAKEOFF);
            }
            else if (gs > 0.0) {
                postEvent("Taxiing.");
                phasePipe.set(FlightPhase.TAXI);
            }
            else {
                postEvent("Pushing back.");
                phasePipe.set(FlightPhase.PUSHBACK);
            }
            
        } 
        else {
            double vs = data.getVerticalSpeedFPM();
            if (vs > CLIMB_THRESHOLD) {
                postEvent("Climbing at %d ft.", (int) data.getAltitude());
                phasePipe.set(FlightPhase.CLIMB);
            }
            else if (vs < -CLIMB_THRESHOLD) {
                postEvent("Descending at %d ft.", (int) data.getAltitude());
                phasePipe.set(FlightPhase.DESCEND);
            }
            else {
               postEvent("Cruising at %d ft, ground speed %d kts.", 
                            (int) data.getAltitude(),
                            (int) data.getGroundSpeed());
                phasePipe.set(FlightPhase.CRUISE);
            }
        }
    }
    
    protected void postEvent(String msg, Object... data) {
        LogEvent event = new LogEvent(String.format(msg, data));
        eventPipe.set(event);
    }
    
    
    
}
