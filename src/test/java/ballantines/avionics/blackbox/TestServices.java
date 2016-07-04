/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox;

import ballantines.avionics.blackbox.log.LogEvent;
import ballantines.avionics.blackbox.model.Position;
import ballantines.avionics.blackbox.model.TrackingData;
import ballantines.avionics.blackbox.service.PersistenceService;
import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.flightgear.connect.ServerConfig;
import ballantines.avionics.kacars.KAcarsConfig;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mbuse
 */
public class TestServices extends Services {
    
    public static Services get() {
        return INSTANCE;
    }
    
    private static Services INSTANCE = new TestServices(); 

    private PersistenceService noopPersistenceService = new NoopPersistenceService();

    @Override
    public PersistenceService getPersistenceService() {
        return noopPersistenceService;
    }
    
    
    
    private class NoopPersistenceService implements PersistenceService {

        @Override
        public void writeServerConfig(ServerConfig serverConfig) {
            // NOTHING 
        }

        @Override
        public ServerConfig readServerConfig() {
            return new ServerConfig();
        }

        @Override
        public void writeKACARSConfig(KAcarsConfig config) {
            // NOTHING 
        }

        @Override
        public KAcarsConfig readKACARSConfig() {
            return new KAcarsConfig();
        }

        @Override
        public void writeTrackingData(TrackingData data) {
            // NOTHING 
        }

        @Override
        public TrackingData readTrackingData() {
            return new TrackingData();
        }

        @Override
        public void writeEventLog(List<LogEvent> events) {
            // NOTHING
        }

        @Override
        public List<LogEvent> readEventLog() {
            return new ArrayList<>();
        }

        @Override
        public FlightData readLatestFlightData() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Position readKnownParkingPosition(String airport) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void writeLatestFlightData(FlightData data) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void writeKnownParkingPosition(String airport, Position position) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
        
    }
}
