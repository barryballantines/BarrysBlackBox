/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.log;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 */
public class LogEvent {
    public static enum Type {
        FIRST_MESSAGE,
        INFO,
        LAST_MESSAGE;
    }
    
    private final static TimeZone UTC = TimeZone.getTimeZone("UTC");
    
    private Calendar timestamp;
    private String message;
    private Type type;

    public LogEvent(String message) {
        this(Calendar.getInstance(UTC), message);
    }
    public LogEvent(String message, Type type) {
        this(Calendar.getInstance(UTC), message, type);
    }
    public LogEvent(Calendar timestamp, String message) {
        this(timestamp, message, Type.INFO);
    }
    public LogEvent(Calendar timestamp, String message, Type type) {
        this.timestamp = timestamp;
        this.message = message;
        this.type = type;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public Type getType() {
        return type;
    }
    
    
    
    public JSONObject asJSONObject() {
        JSONObject json = new JSONObject();
        json.put("timestamp", timestamp.getTime().getTime());
        json.put("message", message);
        json.put("type", type.name());
        return json;
    }
    
    public void writeOn(PrintWriter writer) {
        writer.println(getFormattedMessage());
    }

    public String getFormattedMessage() {
        return String.format("[%tR] %s", timestamp, message);
    }

    @Override
    public String toString() {
        return "{ class:'" + getClass().getName() 
                + "', timestamp:'" + timestamp.getTime()
                + "', message: '" + message 
                + "', type: '" + type + "'}";
    }
    
    public static LogEvent fromJSONObject(JSONObject json) {
        Calendar ts = Calendar.getInstance(UTC);
        ts.setTimeInMillis(json.optLong("timestamp", ts.getTimeInMillis()));
        String message = json.optString("message");
        String typeString = json.optString("type");
        LogEvent.Type type = typeString == null ? LogEvent.Type.INFO : LogEvent.Type.valueOf(typeString);
        return new LogEvent(ts, message, type);
    } 
    
    public static String serializeEvents(List<LogEvent> events) {
        List<JSONObject> jsonEvents = new ArrayList<>();
        for (LogEvent e : events) {
            jsonEvents.add(e.asJSONObject());
        }
        return new JSONArray(jsonEvents).toString();
    }
    
    public static List<LogEvent> deserializeEvents(String jsonString) {
        JSONArray jsonArray = new JSONArray(jsonString);
        List<LogEvent> logEvents = new ArrayList<>();
        for (int i=0;i<jsonArray.length();i++) {
            JSONObject json = jsonArray.getJSONObject(i);
            LogEvent event = LogEvent.fromJSONObject(json);
            logEvents.add(event);
        }
        return logEvents;
    }
    
}
