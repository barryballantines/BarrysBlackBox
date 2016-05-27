package ballantines.avionics.blackbox.service;

import ballantines.avionics.blackbox.log.LogEvent;
import ballantines.avionics.blackbox.model.Position;
import ballantines.avionics.blackbox.model.TrackingData;
import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.flightgear.connect.ServerConfig;
import ballantines.avionics.kacars.KAcarsConfig;
import java.util.List;

/**
 *
 * @author mbuse
 */
public interface PersistenceService {

    List<LogEvent> readEventLog();

    KAcarsConfig readKACARSConfig();

    ServerConfig readServerConfig();

    TrackingData readTrackingData();

    FlightData readLatestFlightData();
    
    Position readKnownParkingPosition(String airport);

    void writeEventLog(List<LogEvent> events);

    void writeKACARSConfig(KAcarsConfig config);

    void writeServerConfig(ServerConfig serverConfig);

    void writeTrackingData(TrackingData data);

    void writeLatestFlightData(FlightData data);
    
    void writeKnownParkingPosition(String airport, Position position);
    
    
}
