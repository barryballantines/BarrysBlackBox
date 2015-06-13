/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox;

import ballantines.avionics.blackbox.udp.FlightData;
import de.mbuse.pipes.Pipe;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author mbuse
 */
public class FlightSimulator {
    
    private Pipe<FlightData> pipe;
    private double headingDeg = 0.0;
    private double cruiseSpeedKts = 250.;
    private double altitude = 0.0;
    
    public FlightSimulator(Pipe<FlightData> p) {
        this.pipe = p;
    }

    public void setHeadingDeg(double headingDeg) {
        this.headingDeg = headingDeg;
    }
    
    
    public void simulateParking(int n) {
        for (int i=0; i<n; i++) {
            JSONObject json = new JSONObject();
            setVerticalSpeed(json, 0.0);
            setWoW(json, 1);
            setGroundSpeed(json, 0., headingDeg);
            setAltitude(json, altitude);
            pipe.set(new FlightData(json));
        }
    }
    
    public void simulatePushback(int n) {
        for (int i=0; i<n; i++) {
            JSONObject json = new JSONObject();
            setVerticalSpeed(json, 0.0);
            setWoW(json, 1);
            setAltitude(json, altitude);
            setGroundSpeed(json, -5. , headingDeg);
            pipe.set(new FlightData(json));      
        }
    }
    
    public void simulateTaxi(int n) {
        for (int i=0; i<n; i++) {
            JSONObject json = new JSONObject();
            setVerticalSpeed(json, 0.0);
            setWoW(json, 1);
            setAltitude(json, altitude);
            setGroundSpeed(json, +20. , headingDeg);      
        }
    }
    
    
    public void simulateTakeOff(int n, double speedKts, double rateFps) {
        // speedup
        double step = speedKts/n;
        double gs = 0.;
        for (int i=0; i<n; i++) {
            JSONObject json = new JSONObject();
            setGroundSpeed(json, gs, headingDeg);
            setVerticalSpeed(json, 0.0);
            setWoW(json, 1);
            setAltitude(json, altitude);
            pipe.set(new FlightData(json));
            gs += step;
        }
        
        // takeoff and initial climb...
        for (int i=0; i<n; i++) {
            altitude += rateFps;
            JSONObject json = new JSONObject();
            setVerticalSpeed(json, rateFps);
            setWoW(json, 0);
            setGroundSpeed(json, speedKts, headingDeg);
            setAltitude(json, altitude);
            pipe.set(new FlightData(json));
        }
    }
    
    public void simulateCruise(int n, double speedKts, double rateFps) {
        this.cruiseSpeedKts = speedKts;
        for (int i=0; i<n; i++) {
            altitude += rateFps;
            JSONObject json = new JSONObject();
            setVerticalSpeed(json, rateFps);
            setGroundSpeed(json, speedKts, headingDeg);
            setWoW(json, 0);
            setAltitude(json, altitude);
            pipe.set(new FlightData(json));
        }
    }
    
    public void simulateLanding(int n, double landingSpeedKts, double rateFps) {
        double gs = cruiseSpeedKts;
        double step = gs/n;
        for (int i=0; i<n; i++) {
            altitude += rateFps;
            JSONObject json = new JSONObject();
            setVerticalSpeed(json, rateFps);
            setGroundSpeed(json, gs, headingDeg);
            setWoW(json, 0);
            setAltitude(json, altitude);
            pipe.set(new FlightData(json));
            gs -= step;
        }
        
        step = landingSpeedKts/n;
        for (int i=0; i<n; i++) {
            JSONObject json = new JSONObject();
            setVerticalSpeed(json, 0.0);
            setGroundSpeed(json, gs, headingDeg);
            setWoW(json, 1);
            setAltitude(json, altitude);
            pipe.set(new FlightData(json));
            gs -= step; 
        }
    }
    
    private void setVerticalSpeed(JSONObject json, double vs) {
        json.put("vertical-speed", vs);
    }
    
    private void setAltitude(JSONObject json, double alt) {
        json.put("altitude", alt);
    }
    
    private void setGroundSpeed(JSONObject json, double speedKts, double heading) {
        double rad = 2. * Math.PI * heading / 360.;
        double speedFps = speedKts / 0.592535;
        json.put("speed-east-fps", speedFps * Math.sin(rad));
        json.put("speed-north-fps", speedFps * Math.cos(rad));
        json.put("heading-deg", heading);
        json.put("groundspeed", Math.abs(speedKts));
    }
    
    private void setWoW(JSONObject json, int wow) {
        json.put("wow", new JSONArray(new int[] { wow ,wow, wow }));
    }
    
}
