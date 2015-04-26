/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.mbuse.flightgear.pireprecorder;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.javalite.http.Http;
import org.javalite.http.Get;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author mbuse
 */
public class FGFlightDataRetrievalImpl implements FlightDataRetrieval, Configuration {
    
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    
    private String host = "localhost";
    private int port = 5500;

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
    

    private String getPropertyURL(String property) {
        return "http://" + host + ":" + port + "/json/" + property;
    }
    private String getProperty(String property) {
        Get response = Http.get(getPropertyURL(property));
        JSONObject json = new JSONObject(response.text());
        return json.get("value").toString();
    }
    
    private JSONObject getPropertyNode(String property, int depth) {
        String url = getPropertyURL(property) + "?d=" + depth;
        Get response = Http.get(url);
        JSONObject json = new JSONObject(response.text());
        return json;
    }
    
    private Map<String,Object> getProperties(String root, int depth) {
        JSONObject json = getPropertyNode(root, depth);
        Map<String, Object> map = new HashMap<String,Object>();
        visitNodes(map, json);
        return map;
    }
    
    private void visitNodes(Map<String,Object> properties, JSONObject node) {
        String path = node.getString("path");
        String type = node.getString("type");
        if (node.has("value")) {
            if ("string".equals(type)) {
                String value = node.getString("value");
                properties.put(path, value);
            }
            else if ("double".equals(type)) {
                String value = node.getString("value");
                properties.put(path, Double.parseDouble(value));
            }
            else if ("int".equals(type)) {
                String value = node.getString("value");
                properties.put(path, Integer.parseInt(value));
            }
            else if ("bool".equals(type)) {
                String value = node.getString("value");
                properties.put(path, "1".equals(value));
            }
        }
        if (node.has("children")) {
            JSONArray children = node.getJSONArray("children");
            for (int i=0; i<children.length(); i++) {
                JSONObject o = children.getJSONObject(i);
                visitNodes(properties, o);
            }
        }
    }
    
    @Override
    public String getAirport() {
        return getProperty("sim/airport/closest-airport-id");
    }

    @Override
    public long getFuel() {
        return Math.round(Double.parseDouble(getProperty("consumables/fuel/total-fuel-lbs")));
    }

    @Override
    public double getGroundspeed() {
        return Double.parseDouble(getProperty("velocities/groundspeed-kt"));
    }
    
    @Override
    public Calendar getTime() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(UTC);
        return cal;
    }
    
    
    public RouteInformation getRouteInformation() {
        String root = "autopilot/route-manager";
        Map<String, Object> p = getProperties(root, 2);
        RouteInformation info = new RouteInformation();
        root = "/" + root;
        info.departure = (String) p.get(root + "/departure/airport");
        info.destination = (String) p.get(root + "/destination/airport");
        info.totalDistance = (Double) p.get(root + "/total-distance");
        info.distanceRemaining = (Double) p.get(root + "/distance-remaining-nm");
        info.flightTime = (Double) p.get(root + "/flight-time");
        info.estimatedTimeToDestination = (Double) p.get(root + "/ete");
        
        return info;
    }
    
    
    public static class RouteInformation {
        public String departure;
        public String destination;
        public double totalDistance;
        public double distanceRemaining;
        public double flightTime;
        public double estimatedTimeToDestination;
    }
}
