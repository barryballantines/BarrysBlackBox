/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.kacars.model;

import ballantines.avionics.blackbox.udp.FlightData;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 *
 */
@Root(name="kacars")
public class LiveUpdateData {
    
    @Element @Path("switch")
    private String data = "pirep";
    
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
    
    public LiveUpdateData(Flight flight, FlightData data) {
        if (flight==null) {
            flight = new Flight();
        }
        this.depICAO = firstNotNull(data.getDeparture(), flight.depICAO, "");
        this.arrICAO = firstNotNull(data.getDestination(), flight.arrICAO, "");
        this.flightNumber = firstNotNull(flight.flightNumber, "");
        this.registration = firstNotNull(flight.aircraftReg, "");
        this.route = firstNotNull(flight.route, "");
        this.latitude = String.format("%.6f", data.getLatitude());
        this.longitude = String.format("%.6f", data.getLongitude());
        this.groundSpeed = String.format("%.0f", data.getGroundSpeed());
        this.heading = String.format("%.0f", data.getHeading());
        this.altitude = String.format("%.0f", data.getAltitude());
    }
    
    private String firstNotNull(String... values) {
        for (String v : values) {
            if (v!=null) {
                return v;
            }
        }
        return null;
    }
    
}
