/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.model;

import static ballantines.avionics.blackbox.util.JSONUtil.*;
import java.util.Calendar;
import java.util.Objects;
import org.json.JSONObject;

/**
 *
 * @author mbuse
 */
public class TrackingData {
    
    private static final String JSON_TEMPLATE = 
            "{'class':'" + TrackingData.class.getName() + "',"
            + "'departureAirport':%s,"
            + "'departureTime':%s,"
            + "'departureFuel':%d,"
            + "'arrivalAirport':%s,"
            + "'arrivalTime':%s,"
            + "'arrivalFuel':%d,"
            + "'landingRateFPM':%d,"
            + "'trackingStarted':%b,"
            + "'trackingFinished':%b}";
    
    // === DEPARTURE ===
    public String   departureAirport = null;
    public Calendar departureTime    = null;
    public int      departureFuel    = -1;
    
    // === ARRIVAL ===
    public String   arrivalAirport   = null;
    public Calendar arrivalTime      = null;
    public int      arrivalFuel      = -1;
    
    public int landingRateFPM        = 0;
    
    // === STATES ===
    
    public boolean  trackingStarted  = false;
    public boolean  trackingFinished = false;
    
    // === CONSTRUCTORS ===

    public TrackingData() {
    }
    
    public TrackingData(TrackingData data) {
        this.arrivalAirport = data.arrivalAirport;
        this.arrivalFuel = data.arrivalFuel;
        this.arrivalTime = data.arrivalTime;
        this.departureAirport = data.departureAirport;
        this.departureFuel = data.departureFuel;
        this.departureTime = data.departureTime;
        this.landingRateFPM = data.landingRateFPM;
        this.trackingFinished = data.trackingFinished;
        this.trackingStarted = data.trackingStarted;
    }
    
    
    // === CALCULATIONS ===
    
    public int getFuelConsumption() {
        if (trackingFinished) {
            return departureFuel - arrivalFuel;
        }
        else {
            return -1;
        }
    }
    
    public int getFlightTimeInMinutes() {
        if (trackingFinished) {
            long flightTimeMillis = arrivalTime.getTimeInMillis() - departureTime.getTimeInMillis();
            return (int) (flightTimeMillis /60000);
        }
        else {
            return -1;
        }
    }
    
    public int[] getFlightTimeHHMM() {
        int flightTimeMinutes = getFlightTimeInMinutes();
        int[] hhmm = new int[2];
        hhmm[0] = (int) (flightTimeMinutes/60);
        hhmm[1] = (int) (flightTimeMinutes - (60*hhmm[0]));
        return hhmm;
    }
    
    public String getFlightTimeFormatted() {
        int[] hhmm = getFlightTimeHHMM();
        return String.format("%d:%02d", hhmm[0], hhmm[1]);
    }

    // === EQUALS / HASHCODE / TOSTRING ===
    
    public String toString() {
        return String.format(JSON_TEMPLATE,
                literal(departureAirport),
                literal(departureTime),
                departureFuel,
                literal(arrivalAirport),
                literal(arrivalTime),
                arrivalFuel,
                landingRateFPM,
                trackingStarted,
                trackingFinished
                );
    }
    
    public static TrackingData fromString(String jsonLiteral) {
        JSONObject json = new JSONObject(jsonLiteral);
        if (TrackingData.class.getName().equals(json.getString("class"))) {
            TrackingData data = new TrackingData();
            data.departureAirport = json.optString("departureAirport", null);
            data.departureTime = parseCalendar(json.optLong("departureTime", -1L));
            data.departureFuel = json.optInt("departureFuel", -1);
            data.arrivalAirport = json.optString("arrivalAirport", null);
            data.arrivalTime = parseCalendar(json.optLong("arrivalTime", -1L));
            data.arrivalFuel = json.optInt("arrivalFuel", -1);
            data.landingRateFPM = json.optInt("landingRateFPM", -1);
            data.trackingStarted = json.optBoolean("trackingStarted", false);
            data.trackingFinished = json.optBoolean("trackingFinished", false);
            return data;
        }
        else {
            throw new IllegalArgumentException("Invalid json literal: " + jsonLiteral);
        }
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.departureAirport);
        hash = 41 * hash + Objects.hashCode(this.departureTime);
        hash = 41 * hash + this.departureFuel;
        hash = 41 * hash + Objects.hashCode(this.arrivalAirport);
        hash = 41 * hash + Objects.hashCode(this.arrivalTime);
        hash = 41 * hash + this.arrivalFuel;
        hash = 41 * hash + this.landingRateFPM;
        hash = 41 * hash + (this.trackingStarted ? 1 : 0);
        hash = 41 * hash + (this.trackingFinished ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TrackingData other = (TrackingData) obj;
        if (!Objects.equals(this.departureAirport, other.departureAirport)) {
            return false;
        }
        if (!Objects.equals(this.departureTime, other.departureTime)) {
            return false;
        }
        if (this.departureFuel != other.departureFuel) {
            return false;
        }
        if (!Objects.equals(this.arrivalAirport, other.arrivalAirport)) {
            return false;
        }
        if (!Objects.equals(this.arrivalTime, other.arrivalTime)) {
            return false;
        }
        if (this.arrivalFuel != other.arrivalFuel) {
            return false;
        }
        if (this.landingRateFPM != other.landingRateFPM) {
            return false;
        }
        if (this.trackingStarted != other.trackingStarted) {
            return false;
        }
        if (this.trackingFinished != other.trackingFinished) {
            return false;
        }
        return true;
    }
    
    
    
}
