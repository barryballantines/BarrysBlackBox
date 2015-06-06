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
    private final static TimeZone UTC = TimeZone.getTimeZone("UTC");
    
    private Calendar timestamp;
    private String message;

    public LogEvent(String message) {
        this(Calendar.getInstance(UTC), message);
    }
    public LogEvent(Calendar timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
    
    public void writeOn(PrintWriter writer) {
        writer.println(getFormattedMessage());
    }

    public String getFormattedMessage() {
        return String.format("[%tR] %s", timestamp, message);
    }
    
}
