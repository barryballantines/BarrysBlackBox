/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox;

import ballantines.avionics.blackbox.log.FlightPhase;
import ballantines.avionics.blackbox.model.Command;
import ballantines.avionics.blackbox.service.FGFlightDataRetrievalImpl;
import ballantines.avionics.blackbox.service.FlightDataRetrieval;
import ballantines.avionics.blackbox.model.FlightTrackingResult;
import ballantines.avionics.blackbox.model.TrackingData;
import ballantines.avionics.blackbox.service.PersistenceService;
import ballantines.avionics.blackbox.service.PreferencesPersistenceServiceImpl;
import ballantines.avionics.flightgear.connect.HttpPropertyServiceImpl;
import ballantines.avionics.flightgear.connect.PropertyService;
import ballantines.avionics.flightgear.connect.ServerConfig;
import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.blackbox.util.Log;
import ballantines.avionics.kacars.KAcarsConfig;
import ballantines.avionics.kacars.KAcarsClient;
import ballantines.avionics.kacars.model.Flight;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.util.Timer;

/**
 *
 * @author mbuse
 */
public class Services implements PipeUpdateListener {
    
    private static Log L = Log.forClass(Services.class);
    
    // === STATICS ===
    
    public static Services get() {
        return INSTANCE;
    }
    
    private static Services INSTANCE = new Services(); 
    
    // === BUSINESS LOGIC ===
    
    public void init() {
        // FlightGear Properties 
        ServerConfig serverConfig = getPersistenceService().readServerConfig();
        propertyService = new HttpPropertyServiceImpl(serverConfig); 
        flightDataRetrieval = new FGFlightDataRetrievalImpl(propertyService);
        serverConfigPipe.set(serverConfig);
        // KACARS
        KAcarsConfig kacarsConfig = getPersistenceService().readKACARSConfig();
        kacarsClient = new KAcarsClient(kacarsConfig);
        kacarsConfigPipe.set(kacarsConfig);
        // UDP SERVER
        udpServerPortPipe.set(5555);
        udpServerRunningPipe.set(false);
        // TrackingData
        TrackingData data = getPersistenceService().readTrackingData();
        trackingDataPipe.set(data);
        // TIMER
        timer = new Timer("Barry's BlackBox Timer");
        
    }
    
    public void shutdown() {
        timer.cancel();
    }

    @Override
    public void pipeUpdated(Pipe pipe) {
        L.pipeUpdated(pipe);
        
        if (pipe == this.serverConfigPipe) {
            ServerConfig config = (ServerConfig) pipe.get();
            propertyService.setServerConfig(config);
            getPersistenceService().writeServerConfig(config);
        }
        else if (pipe == this.kacarsConfigPipe) {
            KAcarsConfig config = kacarsConfigPipe.get();
            kacarsClient.setConfig(config);
            getPersistenceService().writeKACARSConfig(config);
        }
        else if (pipe == this.trackingDataPipe) {
            TrackingData data = trackingDataPipe.get();
            getPersistenceService().writeTrackingData(data);
        }
    }
    
    public void fireCommand(Command cmd) {
        this.commandPipe.set(null);
        this.commandPipe.set(cmd);
    }
    
    
    // === ACCESSORS ===

    public PersistenceService getPersistenceService() {
        return persistenceService;
    }
    
    public PropertyService getPropertyService() {
        return propertyService;
    }
    
    public FlightDataRetrieval getFlightDataRetrieval() {
        return flightDataRetrieval;
    }

    public Timer getTimer() {
        return timer;
    }

    public KAcarsClient getKacarsClient() {
        return kacarsClient;
    }
    
    
   
    
    // === VARIABLES ===
    
    private PersistenceService persistenceService = new PreferencesPersistenceServiceImpl();
    private HttpPropertyServiceImpl propertyService;
    private FlightDataRetrieval flightDataRetrieval;
    private KAcarsClient kacarsClient;
    private Timer timer;
    
    public final Pipe<Command> commandPipe = Pipe.newInstance("Services.command", this);
    public final Pipe<KAcarsConfig> kacarsConfigPipe = Pipe.newInstance("Services.kacarsConfig", this);
    public final Pipe<ServerConfig> serverConfigPipe = Pipe.newInstance("Services.serverConfig", this);
    public final Pipe<Boolean> isRecordingPipe = Pipe.newInstance("Services.isRecording", this);
    public final Pipe<Long> currentFuelPipe = Pipe.newInstance("Services.currentFuel", this);
    public final Pipe<Boolean> udpServerRunningPipe = Pipe.newInstance("Services.udpServerRunning", this);
    public final Pipe<Integer> udpServerPortPipe = Pipe.newInstance("Services.udpServerPort", this);
    public final Pipe<FlightData> flightDataPipe = Pipe.newInstance("Services.flightData", this);
    public final Pipe<FlightTrackingResult> flightTrackingResultPipe = Pipe.newInstance("Service.flightTrackingResult", this);
    public final Pipe<TrackingData> trackingDataPipe = Pipe.newInstance("Services.trackingData", this);
    public final Pipe<Flight> flightBidPipe = Pipe.newInstance("Services.flightBidPipe", this);
    public final Pipe<FlightPhase> flightPhasePipe = Pipe.newInstance("Services.phase", this);
    
    
}
