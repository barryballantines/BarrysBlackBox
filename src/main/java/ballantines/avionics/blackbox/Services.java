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
    
    // === STATICS ===
    
    public static Services get() {
        return INSTANCE;
    }
    
    private static Services INSTANCE = new Services(); 
    
    // === BUSINESS LOGIC ===
    
    public void init() {
        ServerConfig serverConfig = getServerConfigFromUserPreferences();
        KAcarsConfig kacarsConfig = readKACARSConfigFromUserPreferences();
        propertyService = new HttpPropertyServiceImpl(serverConfig); 
        flightDataRetrieval = new FGFlightDataRetrievalImpl(propertyService);
        timer = new Timer("Barry's BlackBox Timer");
        serverConfigPipe.set(serverConfig);
        udpServerPortPipe.set(5555);
        udpServerRunningPipe.set(false);
        
    }
    
    public void shutdown() {
        timer.cancel();
    }

    @Override
    public void pipeUpdated(Pipe pipe) {
        System.out.println("[SERVICES] Model updated : " + pipe.id() + " -> " + pipe.get());
        
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
        System.out.println("[SERVICES] reading kACARS config from user preferences: " + config);
        return config;
    }
    
    public void writeKACARSConfigToUserPreferences(KAcarsConfig config) {
        try {
            System.out.println("[SERVICES] writing kACARS config to user preferences: " + config);
            Preferences prefs = Preferences.userNodeForPackage(KAcarsConfig.class);
            prefs.put("config.url", config.url);
            prefs.put("config.user", config.pilotID);
            prefs.put("config.password", config.password);
            prefs.putBoolean("config.enabled", config.enabled);
            prefs.flush();
        } catch (BackingStoreException ex) {
            System.err.println("[SERVICES] Failed to write kACARS config to User Preferences: " + ex);
        }
    }
    
    public ServerConfig getServerConfigFromUserPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(ServerConfig.class);
        
        String host = prefs.get("flightgear.httpd.host", "localhost");
        int port = prefs.getInt("flightgear.httpd.port", 5500);
        final ServerConfig serverConfig = new ServerConfig(host, port);
        
        System.out.println("[SERVICES] reading server config from user preferences: " + serverConfig);
        return serverConfig;
    }
    
    public void writeServerConfigToUserPreferences(ServerConfig serverConfig) {
        try {
            if (serverConfig==null) {
                return;
            }
            System.out.println("[SERVICES] writing server config to user preferences: " + serverConfig);
            Preferences prefs = Preferences.userNodeForPackage(ServerConfig.class);
            prefs.put("flightgear.httpd.host", serverConfig.getHost());
            prefs.putInt("flightgear.httpd.port", serverConfig.getPort());
            prefs.flush();
        } catch (BackingStoreException ex) {
            System.err.println("[SERVICES] Failed to write ServerConfig to User Preferences: " + ex);
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
    
    
}
