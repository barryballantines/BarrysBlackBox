package ballantines.avionics.blackbox.service;

import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.log.LogEvent;
import ballantines.avionics.blackbox.model.Position;
import ballantines.avionics.blackbox.model.TrackingData;
import ballantines.avionics.blackbox.panel.ConfigurationForm;
import ballantines.avionics.blackbox.panel.PositionPanel;
import ballantines.avionics.blackbox.panel.RouteFinderPanel;
import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.blackbox.util.Log;
import ballantines.avionics.flightgear.connect.ServerConfig;
import ballantines.avionics.kacars.KAcarsConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.json.JSONArray;
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
        File dir = (path==null) ? null : new File(path);
        if (dir!=null && !dir.exists()) {
            L.warn("[PERSISTENCE] Stored routes directory does not exist. Will be ignored: %s", path);
            return null;
        }
        return dir;
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
    
    @Override
    public File readPreferencesBackupDirectory() {
        Preferences prefs = Preferences.userNodeForPackage(ConfigurationForm.class);
        String path = prefs.get("configuration.backup.directory", null);
        File dir = (path==null) ? null : new File(path);
        if (dir!=null && !dir.exists()) {
            L.warn("[PERSISTENCE] Stored preferences backup directory does not exist. Will be ignored: %s", path);
            return null;
        }
        return dir;
    }

    @Override
    public void writePreferencesBackupDirectory(File directory) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(ConfigurationForm.class);
            prefs.put("configuration.backup.directory", directory.getAbsolutePath());
            prefs.flush();
        }
        catch(BackingStoreException ex) {
            L.error(ex, "[PERSISTENCE] Failed to store routes directory in preferences.");
        }
    }
    
    public JSONArray exportPreferences() {
        JSONArray array = new JSONArray();
        array.put(exportNodeByPrefix(null, "de.mbuse.flightgear.pireprecorder.parking."));
        array.put(exportNode(PositionPanel.class, "lastKnownFlightData"));
        array.put(exportNode(Services.class, "trackingData", "eventLog"));
        array.put(exportNodeByPrefix(KAcarsConfig.class, "config"));
        array.put(exportNodeByPrefix(ServerConfig.class, "flightgear.httpd."));
        array.put(exportNode(RouteFinderPanel.class, "routeFinder.storage.directory"));
        array.put(exportNode(ConfigurationForm.class, "configuration.backup.directory"));
        return array;
    }
    
    public void importPreferences(JSONArray backup) {
        for (int i=0; i<backup.length(); i++) {
            importNode(backup.getJSONObject(i));
        }
    }
    
    public void deletePreferences() {
        deletePreferencesByPrefix(null, "de.mbuse.flightgear.pireprecorder.parking.");
        deletePreferences(PositionPanel.class, "lastKnownFlightData");
        deletePreferences(Services.class, "trackingData", "eventLog");
        deletePreferencesByPrefix(KAcarsConfig.class, "config");
        deletePreferencesByPrefix(ServerConfig.class, "flightgear.httpd.");
        deletePreferencesByPrefix(RouteFinderPanel.class, "routeFinder.storage.directory");
        deletePreferencesByPrefix(ConfigurationForm.class, "configuration.backup.directory");
    }
    
    private JSONObject exportNode(Class cls, String... properties) {
        Preferences prefs = (cls==null) ? Preferences.userRoot() : Preferences.userNodeForPackage(cls);
        
        JSONObject node = new JSONObject();
        JSONObject props = new JSONObject();
        
        node.put("class", (cls==null) ? "USERROOT" : cls.getName());
        for (String p : properties) {
            String value = prefs.get(p, null);
            if (value!=null) {
                props.put(p, value);
            }
        }
        
        node.put("properties", props);
        
        return node;
    }
    
    private void deletePreferences(Class cls, String... keys) {
        Preferences prefs = (cls==null) 
                ? Preferences.userRoot()
                : Preferences.userNodeForPackage(cls);
        try {
            for(String key : keys) {
                prefs.remove(key);
            }
        } catch (Exception ex) {
            L.error(ex, "Failed to delete Node for %s", cls.getName());
        }
    }
    
    private void deletePreferencesByPrefix(Class cls, String prefix) {
        Preferences prefs = (cls==null) 
                ? Preferences.userRoot()
                : Preferences.userNodeForPackage(cls);
        try {
            String[] keys = prefs.keys();
            for(String key : keys) {
                if (key.startsWith(prefix)) {
                    prefs.remove(key);
                }
            }
        } catch (Exception ex) {
            L.error(ex, "Failed to delete Node for %s", cls.getName());
        }
    }
    
    private void deleteNode(Class cls) {
        Preferences prefs = Preferences.userNodeForPackage(cls);
        try {
            prefs.removeNode();
        } catch (BackingStoreException ex) {
            L.error(ex, "Failed to delete Node for %s", cls.getName());
        }
    }
    
    private JSONObject exportNodeByPrefix(Class cls, String prefix) {
        Preferences prefs = (cls==null) ? Preferences.userRoot() : Preferences.userNodeForPackage(cls);
        
        JSONObject node = new JSONObject();
        JSONObject props = new JSONObject();
        
        node.put("class", (cls==null) ? "USERROOT" : cls.getName());
        try {
            String[] keys = prefs.keys();
            Arrays.sort(keys);
            for(String p : keys) {
                if (p.startsWith(prefix)) {
                    String value = prefs.get(p, null);
                    if (value!=null) {
                        props.put(p, value);
                    }
                }
            }
        } catch (Exception ex) {
            L.error(ex, "Failed to import node %s", node);
        }
        
        node.put("properties", props);
        
        return node;
    }
    
    private void importNode(JSONObject node) {
        String cls = node.getString("class");
        JSONObject props = node.getJSONObject("properties");
        try {
            Preferences prefs = ("USERROOT".equals(cls)) 
                ? Preferences.userRoot()
                : Preferences.userNodeForPackage(Class.forName(cls));
            
            for (String key : (Set<String>) props.keySet()) {
                prefs.put(key, props.getString(key));
            }
            prefs.flush();
            
        } catch (Exception ex) {
            L.error(ex, "Failed to import node %s", node);
        }
    }
}
