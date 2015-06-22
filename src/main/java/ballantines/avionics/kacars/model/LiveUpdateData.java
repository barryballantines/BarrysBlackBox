/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.kacars.model;

import ballantines.avionics.blackbox.log.FlightPhase;
import ballantines.avionics.blackbox.model.TrackingData;
import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.blackbox.util.JSONUtil;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 *
 */
@Root(name="kacars")
public class LiveUpdateData {
    private static final String STRING = "'%s'";
    private static final String TOSTRINGFORMAT = JSONUtil.createFormatString(LiveUpdateData.class, 
            "pilotId", STRING,
            "flightNumber", STRING,
            "registration", STRING,
            "depTime", STRING,
            "depICAO", STRING,
            "arrICAO", STRING,
            "route", STRING,
            "latitude", STRING,
            "longitude", STRING,
            "heading", STRING,
            "altitude", STRING,
            "groundSpeed", STRING,
            "status", STRING );
    
    @Element @Path("switch")
    private String data = "liveupdate";
    
    @Element @Path("verify")
    public String pilotID;
    
    @Element @Path("liveupdate")
    public String flightNumber;
    
    @Element @Path("liveupdate")
    public String registration;
    
    @Element @Path("liveupdate")
    public String depTime;
    
    @Element @Path("liveupdate")
    public String depICAO;
    
    @Element @Path("liveupdate")
    public String arrICAO;
    
    @Element @Path("liveupdate")
    public String route;
    
    @Element @Path("liveupdate")
    public String latitude;
    
    @Element @Path("liveupdate")
    public String longitude;
    
    @Element @Path("liveupdate")
    public String heading;
    
    @Element @Path("liveupdate")
    public String altitude;
    
    @Element @Path("liveupdate")
    public String groundSpeed;
    
    @Element @Path("liveupdate")
    public String status;
    
    
    public LiveUpdateData() {}
    
    public LiveUpdateData(Flight flight, FlightData data, TrackingData trackingData, FlightPhase phase) {
        if (flight==null) {
            flight = new Flight();
        }
        if (trackingData==null) {
            trackingData = new TrackingData();
        }
        
        this.depICAO = firstNotNull(trackingData.departureAirport, data.getDeparture(), flight.depICAO, "");
        this.arrICAO = firstNotNull(trackingData.arrivalAirport, data.getDestination(), flight.arrICAO, "");
        this.depTime = firstNotNull(
                trackingData==null ? null : String.format("%tR", trackingData.departureTime),
                flight.depTime,
                "");
        this.flightNumber = firstNotNull(flight.flightNumber, "");
        this.registration = firstNotNull(flight.aircraftReg, "");
        this.route = firstNotNull(flight.route, "");
        this.latitude = String.format("%.6f", data.getLatitude());
        this.longitude = String.format("%.6f", data.getLongitude());
        this.groundSpeed = String.format("%.0f", data.getGroundSpeed());
        this.heading = String.format("%.0f", data.getHeading());
        this.altitude = String.format("%.0f", data.getAltitude());
        this.status = phase==null ? "" : phase.name();
    }
    
    private String firstNotNull(String... values) {
        for (String v : values) {
            if (v!=null) {
                return v;
            }
        }
        return null;
    }
    
    
    @Override
    public String toString() {
        return String.format(TOSTRINGFORMAT,
                pilotID, flightNumber, registration, depTime, depICAO, arrICAO,
                route, latitude, longitude, heading, altitude, groundSpeed, status);
    }
    
}
