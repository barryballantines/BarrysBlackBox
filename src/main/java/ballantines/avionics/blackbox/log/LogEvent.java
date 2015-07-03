/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.log;

import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
    
    
    
}
