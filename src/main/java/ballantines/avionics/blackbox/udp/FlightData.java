/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.udp;

import ballantines.avionics.blackbox.util.Calculus;
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
    
    public double getHeading() {
        return json.getDouble("heading");
    }
    
    public double getLatitude() {
        return json.getDouble("latitude");
    }
    
    public double getLongitude() {
        return json.getDouble("longitude");
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
    
    public double getGroundSpeedForward() {
        double[] speedFpsVector = new double[] {
            json.getDouble("speed-east-fps"),
            json.getDouble("speed-north-fps")
        };
        double headingDeg = json.getDouble("heading-deg");
        double[] headingVector = Calculus.headingDegAsVector(headingDeg);
        
        double fwdSpeedFps = Calculus.scalar(speedFpsVector, headingVector);
        return 0.592535 * fwdSpeedFps;
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
    
    public double getVerticalSpeedFPS() {
        return json.getDouble("vertical-speed");
    }
    
    public int getVerticalSpeedFPM() {
        return (int) (getVerticalSpeedFPS() * 60);
    }

    @Override
    public String toString() {
        return json.toString();
    }
    
}
