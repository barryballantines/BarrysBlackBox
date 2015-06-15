/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox;

import ballantines.avionics.blackbox.model.TrackingData;
import ballantines.avionics.flightgear.connect.ServerConfig;
import ballantines.avionics.kacars.KAcarsConfig;

/**
 *
 * @author mbuse
 */
public class TestServices extends Services {
    
    public static Services get() {
        return INSTANCE;
    }
    
    private static Services INSTANCE = new TestServices(); 

    @Override
    public void writeServerConfigToUserPreferences(ServerConfig serverConfig) {
        // NOTHING 
    }

    @Override
    public ServerConfig readServerConfigFromUserPreferences() {
        return new ServerConfig();
    }

    @Override
    public void writeKACARSConfigToUserPreferences(KAcarsConfig config) {
        // NOTHING 
    }

    @Override
    public KAcarsConfig readKACARSConfigFromUserPreferences() {
        return new KAcarsConfig();
    }

    @Override
    public void writeTrackingDataToUserPreferences(TrackingData data) {
        // NOTHING 
    }

    @Override
    public TrackingData readTrackingDataFromUserPreferences() {
        return new TrackingData();
    }
    
    
    
   
    
}
