package ballantines.avionics.blackbox.service;

import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.log.LogEvent;
import ballantines.avionics.blackbox.model.Position;
import ballantines.avionics.blackbox.model.TrackingData;
import ballantines.avionics.blackbox.panel.PositionPanel;
import ballantines.avionics.blackbox.panel.RouteFinderPanel;
import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.blackbox.util.Log;
import ballantines.avionics.flightgear.connect.ServerConfig;
import ballantines.avionics.kacars.KAcarsConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.json.JSONObject;

/**
 *
 * @author mbuse
 */
public class PreferencesPersistenceServiceImpl implements PersistenceService {
    
    private static Log L = Log.forClass(PreferencesPersistenceServiceImpl.class);
    
    private Preferences prefs = Preferences.userNodeForPackage(Services.class);
    
    
    public void writeKnownParkingPosition(String airport, Position position) {
        try {
            Preferences prefs = Preferences.userRoot();
            
            String root = "de.mbuse.flightgear.pireprecorder.parking." + airport;
            prefs.put(root + ".longitude", "" + position.lon);
            prefs.put(root + ".latitude", "" + position.lat);
            prefs.put(root + ".heading", "" + position.hdg);
            prefs.put(root + ".altitude", "" + position.alt);
            prefs.flush();
        } catch (BackingStoreException ex) {}
    }
    
    public Position readKnownParkingPosition(String airport) {
        Preferences prefs = Preferences.userRoot();
        String root = "de.mbuse.flightgear.pireprecorder.parking." + airport;
        Position pos = new Position();
        
        pos.lon = prefs.getDouble(root + ".longitude", Double.NaN);
        pos.lat = prefs.getDouble(root + ".latitude", Double.NaN);
        pos.hdg = prefs.getDouble(root + ".heading", Double.NaN);
        pos.alt = prefs.getDouble(root + ".altitude", Double.NaN);
        
        return pos;
    }
    
    @Override
    public void writeLatestFlightData(FlightData data) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(PositionPanel.class);
            prefs.put("lastKnownFlightData", data.toString());
            prefs.flush();
        }
        catch(BackingStoreException ex) {
            L.error(ex, "Failed to store lastKnownFlightData to preferences: %s", data);
        }
    }
    
    @Override
    public FlightData readLatestFlightData() {
        Preferences prefs = Preferences.userNodeForPackage(PositionPanel.class);
        String json = prefs.get("lastKnownFlightData", null);
        if (json==null) {
            return null;
        }
        FlightData data = new FlightData(new JSONObject(json));
        return data;
    }
    
    @Override
    public TrackingData readTrackingData() {
        String serialized = prefs.get("trackingData", null);
        TrackingData data = (serialized==null) 
                ? new TrackingData()
                : TrackingData.fromString(serialized);
        return data;
    }
    
    @Override
    public void writeTrackingData(TrackingData data) {
        try {
            if (data==null) {
                L.info("[PERSISTENCE] removing tracking data from user preferences.");
                prefs.remove("trackingData");
            }
            else {
                L.info("[PERSISTENCE] writing tracking data to user preferences: %s ", data);
                prefs.put("trackingData", data.toString());
            }
            prefs.flush();
        }
        catch (BackingStoreException ex) {
            L.error(ex, "[PERSISTENCE] Failed to write TrackingData to User Preferences");
        }
    }
    
    @Override
    public KAcarsConfig readKACARSConfig() {
        Preferences prefs = Preferences.userNodeForPackage(KAcarsConfig.class);
        KAcarsConfig config = new KAcarsConfig();
        config.url = prefs.get("config.url", null);
        config.pilotID = prefs.get("config.user", null);
        config.password = prefs.get("config.password", null);
        config.enabled = prefs.getBoolean("config.enabled", false);
        config.liveUpdateIntervalMS = prefs.getInt("config.liveupdate.interval", 30000);
        config.liveUpdateEnabled = prefs.getBoolean("config.liveupdate.enabled", false);
        L.info("[PERSISTENCE] reading kACARS config from user preferences: %s " , config);
        return config;
    }
    
    @Override
    public void writeKACARSConfig(KAcarsConfig config) {
        try {
            L.info("[PERSISTENCE] writing kACARS config to user preferences: %s ", config);
            Preferences prefs = Preferences.userNodeForPackage(KAcarsConfig.class);
            prefs.put("config.url", config.url);
            prefs.put("config.user", config.pilotID);
            prefs.put("config.password", config.password);
            prefs.putBoolean("config.enabled", config.enabled);
            prefs.putInt("config.liveupdate.interval", config.liveUpdateIntervalMS);
            prefs.putBoolean("config.liveupdate.enabled", config.liveUpdateEnabled);
            prefs.flush();
        } catch (BackingStoreException ex) {
            L.error(ex, "[PERSISTENCE] Failed to write kACARS config to User Preferences");
        }
    }
    
    @Override
    public ServerConfig readServerConfig() {
        Preferences prefs = Preferences.userNodeForPackage(ServerConfig.class);
        
        String host = prefs.get("flightgear.httpd.host", "localhost");
        int port = prefs.getInt("flightgear.httpd.port", 5500);
        final ServerConfig serverConfig = new ServerConfig(host, port);
        
        L.info("[PERSISTENCE] reading server config from user preferences: %s", serverConfig);
        return serverConfig;
    }
    
    @Override
    public void writeServerConfig(ServerConfig serverConfig) {
        try {
            if (serverConfig==null) {
                return;
            }
            L.info("[PERSISTENCE] writing server config to user preferences: %s", serverConfig);
            Preferences prefs = Preferences.userNodeForPackage(ServerConfig.class);
            prefs.put("flightgear.httpd.host", serverConfig.getHost());
            prefs.putInt("flightgear.httpd.port", serverConfig.getPort());
            prefs.flush();
        } catch (BackingStoreException ex) {
            L.error(ex, "[PERSISTENCE] Failed to write ServerConfig to User Preferences");
        }
    }
    
    @Override
    public List<LogEvent> readEventLog() {
        try {
            
            String serialized = prefs.get("eventLog", null);
            if (serialized==null) {
                return new ArrayList<>();
            }
            else {
                return LogEvent.deserializeEvents(serialized);
            }
        } catch (Exception ex) {
            L.error(ex, "[PERSISTENCE] Error reading event log from user preferences... data corrupt???");
            try {
                prefs.remove("eventLog");
                prefs.flush();
            }
            catch (BackingStoreException fex) {
                L.error(ex, "[PERSISTENCE] Failed to remove corrupted event log from user preferences.");
            }
            return new ArrayList<>();
        }
    }
    
    @Override
    public void writeEventLog(List<LogEvent> events) {
        try {
            if (events==null) {
                L.info("[PERSISTENCE] removing event log from User Preferences.");
                prefs.remove("eventLog");
            }
            else {
                L.info("[PERSISTENCE] writing event log to user preferences");
                String serializedEvents = LogEvent.serializeEvents(events);
                prefs.put("eventLog", serializedEvents);
            }
            prefs.flush();
        }
        catch (BackingStoreException ex) {
            L.error(ex, "[PERSISTENCE] Failed to write Event log to User Preferences");
        }
    }

    @Override
    public File readRoutesDirectory() {
        Preferences prefs = Preferences.userNodeForPackage(RouteFinderPanel.class);
        String path = prefs.get("routeFinder.storage.directory", null);
        return (path==null) ? null : new File(path);
    }

    @Override
    public void writeRoutesDirectory(File directory) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(RouteFinderPanel.class);
            prefs.put("routeFinder.storage.directory", directory.getAbsolutePath());
            prefs.flush();
        }
        catch(BackingStoreException ex) {
            L.error(ex, "[PERSISTENCE] Failed to store routes directory in preferences.");
        }
    }
}
