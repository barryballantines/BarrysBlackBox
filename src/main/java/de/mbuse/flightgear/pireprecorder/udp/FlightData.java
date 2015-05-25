/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mbuse.flightgear.pireprecorder.udp;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author mbuse
 */
public class FlightData {
    
    private JSONObject json;
    
    public FlightData(JSONObject json) {
        this.json = json;
    }
    
    public double getAltitude() {
        return json.getDouble("altitude");
    }
    
    public String getClosestAirport() {
        return json.getString("closest-airport");
    }
    
    public double getFuel() {
        return json.getDouble("fuel-lbs");
    }
    
    public double getGroundSpeed() {
        return json.getDouble("groundspeed");
    }
    
    public String getDeparture() {
        return json.getString("route-departure-airport");
    }
    
    public String getDestination() {
        return json.getString("route-destination-airport");
    }
    
    public double getTotalDistance() {
        return json.getDouble("route-total-distance");
    }
    
    public double getRemainingDistance() {
        return json.getDouble("route-distance-remaining");
    }
    
    public double getFlightTime() {
        return json.getDouble("route-flight-time");
    }
    
    public double getETE() {
        return json.getDouble("route-ete");
    }
    
    public double[] getWoW() {
        JSONArray array = json.getJSONArray("wow");
        double[] result = new double[array.length()];
        for (int i=0; i<result.length; i++) {
            result[i] = array.getDouble(i);
        }
        return result;
    }
    
    public double getVerticalSpeed() {
        return json.getDouble("vertical-speed");
    }

    @Override
    public String toString() {
        return json.toString();
    }
    
}