package ballantines.avionics.blackbox.service;

import ballantines.avionics.blackbox.log.LogEvent;
import ballantines.avionics.blackbox.model.Position;
import ballantines.avionics.blackbox.model.TrackingData;
import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.flightgear.connect.ServerConfig;
import ballantines.avionics.kacars.KAcarsConfig;
import java.io.File;
import java.util.List;
import org.json.JSONArray;

/**
 *
 * @author mbuse
 */
public interface PersistenceService {

    List<LogEvent> readEventLog();

    KAcarsConfig readKACARSConfig();

    ServerConfig readServerConfig();
    
    List<KAcarsConfig> readStoredKACARSConfigs();

    TrackingData readTrackingData();

    FlightData readLatestFlightData();
    
    Position readKnownParkingPosition(String airport);
    
    File readRoutesDirectory();

    void writeEventLog(List<LogEvent> events);

    void writeKACARSConfig(KAcarsConfig config);
    
    void writeStoredKACARSConfigs(List<KAcarsConfig> configs);

    void writeServerConfig(ServerConfig serverConfig);

    void writeTrackingData(TrackingData data);

    void writeLatestFlightData(FlightData data);
    
    void writeKnownParkingPosition(String airport, Position position);
    
    void writeRoutesDirectory(File directory);
    
    File readPreferencesBackupDirectory();
    
    void writePreferencesBackupDirectory(File directory);
    
    JSONArray exportPreferences();
    
    void importPreferences(JSONArray backup);
    
    void deletePreferences();
    
}
