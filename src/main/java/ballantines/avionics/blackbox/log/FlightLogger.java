/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.log;

import ballantines.avionics.blackbox.model.AircraftInformation;
import ballantines.avionics.blackbox.service.FlightDataRetrieval;
import ballantines.avionics.blackbox.model.FlightTrackingResult;
import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.blackbox.util.Buffer;
import ballantines.avionics.blackbox.util.Calculus;
import ballantines.avionics.kacars.KAcarsClient;
import ballantines.avionics.kacars.model.Flight;
import ballantines.avionics.blackbox.log.LogEvent.Type;
import ballantines.avionics.blackbox.service.LiveUpdate;
import ballantines.avionics.blackbox.util.Log;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mbuse
 */
public class FlightLogger implements PipeUpdateListener {
    private static Log L = Log.forClass(FlightLogger.class);
    private static final double CLIMB_THRESHOLD = 200.0;
    private static final double TAKEOFF_THRESHOLD = 50.0;
    private static final double TAXI_THRESHOLD = 0.3;
    
    // OUTPUT PIPES
    public final Pipe<LogEvent> eventPipe = Pipe.newInstance("flightLogger.event", this);
    public final Pipe<Flight> flightBidPipe = Pipe.newInstance("flightLogger.flight", this);
    public final Pipe<FlightPhase> phasePipe = Pipe.newInstance("flightLogger.phase", this);
    
    private final Pipe<Boolean> isRecordingPipe = Pipe.newInstance("pirepForm.isRecording", this);
    private final Pipe<FlightData> dataPipe = Pipe.newInstance("flightLogger.data");
    
    private final Pipe<Double> avgVerticalSpeedPipe = Pipe.newInstance("flightLogger.avgVerticalSpeed", 0.0, this);
    
    private double maxClimbRate = 0.0;
    private double maxDescentRate = 0.0;
    
    private Buffer averageVerticalSpeedBuffer = new Buffer(120);

    private Services services;
    private LiveUpdate liveUpdate;
    
    private List<LogEvent> events = new ArrayList<>();
    
    public FlightLogger(Services services) {
       this.services = services;
       this.liveUpdate = new LiveUpdate(services);
       this.isRecordingPipe.connectTo(services.isRecordingPipe);
       this.dataPipe.connectTo(services.flightDataPipe); 
       
       services.flightPhasePipe.connectTo(phasePipe);
       services.flightBidPipe.connectTo(flightBidPipe);
       
    }

    public List<LogEvent> getEvents() {
        return events;
    }
    
    @Override
    public void pipeUpdated(Pipe pipe) {
        L.pipeUpdated(pipe);
        
        if (pipe == isRecordingPipe) {
            Boolean isRecording = isRecordingPipe.get();
            if (isRecording!=null && isRecording ) {
                events.clear();
                beforeStartRecording();
                dataPipe.addListener(this);
                liveUpdate.start();
            }
            else if (isRecording!=null){
                dataPipe.removeChangeListener(this);
                afterStopRecording();
                liveUpdate.stop();
            }
        }
        
        if (pipe == dataPipe) {
            flightDataUpdated();
        }
        
        if (pipe == avgVerticalSpeedPipe) {
            verticalSpeedUpdated();
        }
        
        if (pipe == phasePipe) {
            L.debug("New flight phase %s", phasePipe.get());
        }
    }
    
    protected void beforeStartRecording() {
        KAcarsClient client = services.getKacarsClient();
        
        if (client.isEnabled()) {
            try {
                Flight f = client.getBid();
                if (f!=null) {
                    flightBidPipe.set(f);
                    postEvent(Type.FIRST_MESSAGE, "This is flight %s from %s to %s.", f.flightNumber, f.depICAO, f.arrICAO);
                    postEvent("Block time %s", (f.depTime != null) ? f.depTime : "N/A");
                    postEvent("Assigned aircraft model: %s", f.aircraftFullName);
                    postEvent("Assigned aircraft registration: %s", f.aircraftReg);
                    if (f.route != null) {
                        postEvent("Assigned route: %s", f.route);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Cannot get flight information:" + ex);
            }
        }
        
        FlightDataRetrieval service = services.getFlightDataRetrieval();
        try {
            AircraftInformation ac = service.getAircraftInformation();
            
            if (ac!=null) {
                postEvent("Actual aircraft: %s", ac.name);
                postEvent("Fuel loaded: %d lbs", ac.fuelLbs);
                service.getRouteInformation();
            }
        } catch (Exception e) {
            
        }
        
    }
    
    protected void afterStopRecording() {
        postEvent("Engines shutdown.");
        postEvent("Disembarking.");
        FlightTrackingResult result = services.flightTrackingResultPipe.get();
        if (result!=null) {
            int[] hhmm = result.getFlightTimeHoursAndMinutes();
            postEvent("Flight time: %d:%02d", hhmm[0], hhmm[1]);
            postEvent("Landing rate: %d fpm", result.landingRateFPM);
            postEvent("Fuel consumption: %d lbs", result.fuelConsumption);
        }
        postEvent(Type.LAST_MESSAGE, "Flight recording stopped.");
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
        
            switch (phase) {
                case CLIMB:
                case CRUISE:
                case DESCENT:
                    storeVerticalSpeed(data);
            }
        }
    }
    
    private void storeVerticalSpeed(FlightData data) {
        averageVerticalSpeedBuffer.put(data.getVerticalSpeedFPM());
        if (averageVerticalSpeedBuffer.getCapacity() == averageVerticalSpeedBuffer.getSize()) {
            double avg = averageVerticalSpeedBuffer.getAverage();
            avg = Math.round(avg/100.0) * 100.0;
            avgVerticalSpeedPipe.set(avg);
        }
    }
    
    private FlightPhase phaseTransition(FlightPhase phase, FlightData data) {
        double wow;
        int gs = (int) (data.getGroundSpeedForward());
        L.debug("Evaluating phase transition: groundspeed %d kts, phase %s", gs, phase);
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
                wow = Calculus.max(data.getWoW());
                if (wow < 0.5) {
                    postEvent("Taking off at %d kts.", gs);
                    return FlightPhase.CLIMB;
                }
                break;
            case DESCENT :
            case CRUISE :
                wow = Calculus.max(data.getWoW());
                if (wow > 0.5) {
                    postEvent("Touchdown.");
                    return FlightPhase.LANDING;
                }
                break;
        }
        return phase;
    }
    
    private FlightPhase phaseTransition(FlightPhase phase, double vsfpm) {
        FlightData data = dataPipe.get();
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
                    postEvent("Top of climb reached at altitude %d ft.", (int) data.getAltitude());
                    postEvent("Starting descend.");
                    return FlightPhase.DESCENT;
                }
                if (vsfpm > -CLIMB_THRESHOLD && vsfpm < CLIMB_THRESHOLD) {
                    postEvent("Top of climb reached at altitude %d ft.", (int) dataPipe.get().getAltitude());
                    postEvent("Cruising at ground speed %d kts.", (int) data.getGroundSpeed());
                    return FlightPhase.CRUISE;
                }
                break;
            case CRUISE :
                if (vsfpm >= CLIMB_THRESHOLD) {
                    postEvent("Starting climb at %d fpm.", data.getVerticalSpeedFPM());
                    return FlightPhase.CLIMB;
                }
                if (vsfpm <= -CLIMB_THRESHOLD) {
                    postEvent("Top of descent reached at altitude %d ft.", (int) data.getAltitude());
                    postEvent("Starting descent at %d fpm.", data.getVerticalSpeedFPM());
                    return FlightPhase.DESCENT;
                }
                break;
            case DESCENT :
                if (vsfpm > -CLIMB_THRESHOLD && vsfpm < CLIMB_THRESHOLD) {
                    postEvent("End of descent. Cruising at %d ft, ground speed %d kts.", 
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
                phasePipe.set(FlightPhase.DESCENT);
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
        postEvent(Type.INFO, msg, data);
    }
    
    protected void postEvent(Type type, String msg, Object... data) {
        LogEvent event = new LogEvent(String.format(msg, data));
        events.add(event);
        eventPipe.set(event);
    }
    
    
    
}
