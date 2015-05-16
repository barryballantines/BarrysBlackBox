/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mbuse.flightgear.pireprecorder;

import de.mbuse.flightgear.connect.HttpPropertyServiceImpl;
import de.mbuse.flightgear.connect.PropertyService;
import de.mbuse.flightgear.connect.ServerConfig;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author mbuse
 */
public class Services implements PipeUpdateListener<Object>{
    
    // === STATICS ===
    
    public static Services get() {
        return INSTANCE;
    }
    
    private static Services INSTANCE = new Services(); 
    
    // === BUSINESS LOGIC ===
    
    public void init() {
        ServerConfig serverConfig = getServerConfigFromUserPreferences();
        propertyService = new HttpPropertyServiceImpl(serverConfig); 
        flightDataRetrieval = new FGFlightDataRetrievalImpl(propertyService);
        
        serverConfigPipe.set(serverConfig);
    }

    @Override
    public void pipeUpdated(Pipe<Object> pipe) {
        System.out.println("[SERVICES] Model updated : " + pipe.id() + " -> " + pipe.get());
        
        if ("Services.serverConfig".equals(pipe.id())) {
            ServerConfig config = (ServerConfig) pipe.get();
            propertyService.setServerConfig(config);
            writeServerConfigToUserPreferences(config);
            
        }
    }
    
    public ServerConfig getServerConfigFromUserPreferences() {
        Preferences prefs = Preferences.userRoot();
        
        String host = prefs.get("de.mbuse.flightgear.pireprecorder.fgHost", "localhost");
        int port = prefs.getInt("de.mbuse.flightgear.piperecorder.fgPort", 5500);
        final ServerConfig serverConfig = new ServerConfig(host, port);
        
        System.out.println("[SERVICES] reading server config from user preferences: " + serverConfig);
        return serverConfig;
    }
    
    public void writeServerConfigToUserPreferences(ServerConfig serverConfig) {
        try {
            System.out.println("[SERVICES] writing server config to user preferences: " + serverConfig);
            Preferences prefs = Preferences.userRoot();
            prefs.put("de.mbuse.flightgear.pireprecorder.fgHost", serverConfig.getHost());
            prefs.putInt("de.mbuse.flightgear.pireprecorder.fgPort", serverConfig.getPort());
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
   
    
    // === VARIABLES ===
    
    private HttpPropertyServiceImpl propertyService;
    private FlightDataRetrieval flightDataRetrieval;
    
    public final Pipe<ServerConfig> serverConfigPipe = Pipe.newInstance("Services.serverConfig", this);
    public final Pipe<Boolean> isRecordingPipe = Pipe.newInstance("Services.isRecording", this);
    public final Pipe<Long> currentFuelPipe = Pipe.newInstance("Services.currentFuel", this);
    
}
