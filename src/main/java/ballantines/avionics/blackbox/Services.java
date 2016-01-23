/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox;

import ballantines.avionics.blackbox.log.FlightPhase;
import ballantines.avionics.blackbox.log.LogEvent;
import ballantines.avionics.blackbox.model.Command;
import ballantines.avionics.blackbox.service.FGFlightDataRetrievalImpl;
import ballantines.avionics.blackbox.service.FlightDataRetrieval;
import ballantines.avionics.blackbox.model.FlightTrackingResult;
import ballantines.avionics.blackbox.model.TrackingData;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

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
        ServerConfig serverConfig = readServerConfigFromUserPreferences();
        propertyService = new HttpPropertyServiceImpl(serverConfig); 
        flightDataRetrieval = new FGFlightDataRetrievalImpl(propertyService);
        serverConfigPipe.set(serverConfig);
        // KACARS
        KAcarsConfig kacarsConfig = readKACARSConfigFromUserPreferences();
        kacarsClient = new KAcarsClient(kacarsConfig);
        kacarsConfigPipe.set(kacarsConfig);
        // UDP SERVER
        udpServerPortPipe.set(5555);
        udpServerRunningPipe.set(false);
        // TrackingData
        TrackingData data = readTrackingDataFromUserPreferences();
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
            writeServerConfigToUserPreferences(config);
        }
        else if (pipe == this.kacarsConfigPipe) {
            KAcarsConfig config = kacarsConfigPipe.get();
            kacarsClient.setConfig(config);
            writeKACARSConfigToUserPreferences(config);
        }
        else if (pipe == this.trackingDataPipe) {
            TrackingData data = trackingDataPipe.get();
            writeTrackingDataToUserPreferences(data);
        }
    }
    
    public TrackingData readTrackingDataFromUserPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(Services.class);
        String serialized = prefs.get("trackingData", null);
        TrackingData data = (serialized==null) 
                ? new TrackingData()
                : TrackingData.fromString(serialized);
        return data;
    }
    
    public void writeTrackingDataToUserPreferences(TrackingData data) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(Services.class);
            if (data==null) {
                L.info("[SERVICES] removing tracking data from user preferences.");
                prefs.remove("trackingData");
            }
            else {
                L.info("[SERVICES] writing tracking data to user preferences: %s ", data);
                prefs.put("trackingData", data.toString());
            }
            prefs.flush();
        }
        catch (BackingStoreException ex) {
            L.error(ex, "[SERVICES] Failed to write TrackingData to User Preferences");
        }
    }
    
    public List<LogEvent> readEventLogFromUserPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(Services.class);
        try {
            
            String serialized = prefs.get("eventLog", null);
            if (serialized==null) {
                return new ArrayList<>();
            }
            else {
                return LogEvent.deserializeEvents(serialized);
            }
        } catch (Exception ex) {
            L.error(ex, "[SERVICES] Error reading event log from user preferences... data corrupt???");
            try {
                prefs.remove("eventLog");
                prefs.flush();
            }
            catch (BackingStoreException fex) {
                L.error(ex, "[SERVICES] Failed to remove corrupted event log from user preferences.");
            }
            return new ArrayList<>();
        }
    }
    
    public void writeEventLogToUserPreferences(List<LogEvent> events) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(Services.class);
            if (events==null) {
                L.info("[SERVICES] removing event log from User Preferences.");
                prefs.remove("eventLog");
            }
            else {
                L.info("[SERVICES] writing event log to user preferences");
                String serializedEvents = LogEvent.serializeEvents(events);
                prefs.put("eventLog", serializedEvents);
            }
            prefs.flush();
        }
        catch (BackingStoreException ex) {
            L.error(ex, "[SERVICES] Failed to write Event log to User Preferences");
        }
    }
    
    public KAcarsConfig readKACARSConfigFromUserPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(KAcarsConfig.class);
        KAcarsConfig config = new KAcarsConfig();
        config.url = prefs.get("config.url", null);
        config.pilotID = prefs.get("config.user", null);
        config.password = prefs.get("config.password", null);
        config.enabled = prefs.getBoolean("config.enabled", false);
        config.liveUpdateIntervalMS = prefs.getInt("config.liveupdate.interval", 30000);
        config.liveUpdateEnabled = prefs.getBoolean("config.liveupdate.enabled", false);
        L.info("[SERVICES] reading kACARS config from user preferences: %s " , config);
        return config;
    }
    
    public void writeKACARSConfigToUserPreferences(KAcarsConfig config) {
        try {
            L.info("[SERVICES] writing kACARS config to user preferences: %s ", config);
            Preferences prefs = Preferences.userNodeForPackage(KAcarsConfig.class);
            prefs.put("config.url", config.url);
            prefs.put("config.user", config.pilotID);
            prefs.put("config.password", config.password);
            prefs.putBoolean("config.enabled", config.enabled);
            prefs.putInt("config.liveupdate.interval", config.liveUpdateIntervalMS);
            prefs.putBoolean("config.liveupdate.enabled", config.liveUpdateEnabled);
            prefs.flush();
        } catch (BackingStoreException ex) {
            L.error(ex, "[SERVICES] Failed to write kACARS config to User Preferences");
        }
    }
    
    public ServerConfig readServerConfigFromUserPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(ServerConfig.class);
        
        String host = prefs.get("flightgear.httpd.host", "localhost");
        int port = prefs.getInt("flightgear.httpd.port", 5500);
        final ServerConfig serverConfig = new ServerConfig(host, port);
        
        L.info("[SERVICES] reading server config from user preferences: %s", serverConfig);
        return serverConfig;
    }
    
    public void writeServerConfigToUserPreferences(ServerConfig serverConfig) {
        try {
            if (serverConfig==null) {
                return;
            }
            L.info("[SERVICES] writing server config to user preferences: %s", serverConfig);
            Preferences prefs = Preferences.userNodeForPackage(ServerConfig.class);
            prefs.put("flightgear.httpd.host", serverConfig.getHost());
            prefs.putInt("flightgear.httpd.port", serverConfig.getPort());
            prefs.flush();
        } catch (BackingStoreException ex) {
            L.error(ex, "[SERVICES] Failed to write ServerConfig to User Preferences");
        }
    } 
    
    public void fireCommand(Command cmd) {
        this.commandPipe.set(null);
        this.commandPipe.set(cmd);
    }
    
    
    // === ACCESSORS ===

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
