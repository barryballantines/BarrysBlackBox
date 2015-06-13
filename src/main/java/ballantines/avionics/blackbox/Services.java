/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox;

import ballantines.avionics.flightgear.connect.HttpPropertyServiceImpl;
import ballantines.avionics.flightgear.connect.PropertyService;
import ballantines.avionics.flightgear.connect.ServerConfig;
import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.blackbox.util.Log;
import ballantines.avionics.kacars.KAcarsConfig;
import ballantines.avionics.kacars.KAcarsClient;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
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
    }
    
    public KAcarsConfig readKACARSConfigFromUserPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(KAcarsConfig.class);
        KAcarsConfig config = new KAcarsConfig();
        config.url = prefs.get("config.url", null);
        config.pilotID = prefs.get("config.user", null);
        config.password = prefs.get("config.password", null);
        config.enabled = prefs.getBoolean("config.enabled", false);
        L.info("[SERVICES] reading kACARS config from user preferences: %a " , config);
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
    
    public final Pipe<KAcarsConfig> kacarsConfigPipe = Pipe.newInstance("Services.kacarsConfig", this);
    public final Pipe<ServerConfig> serverConfigPipe = Pipe.newInstance("Services.serverConfig", this);
    public final Pipe<Boolean> isRecordingPipe = Pipe.newInstance("Services.isRecording", this);
    public final Pipe<Long> currentFuelPipe = Pipe.newInstance("Services.currentFuel", this);
    public final Pipe<Boolean> udpServerRunningPipe = Pipe.newInstance("Services.udpServerRunning", this);
    public final Pipe<Integer> udpServerPortPipe = Pipe.newInstance("Services.udpServerPort", this);
    public final Pipe<FlightData> flightDataPipe = Pipe.newInstance("Services.flightData", this);
    public final Pipe<FlightTrackingResult> flightTrackingResultPipe = Pipe.newInstance("Service.flightTrackingResult", this);
    
    
}
