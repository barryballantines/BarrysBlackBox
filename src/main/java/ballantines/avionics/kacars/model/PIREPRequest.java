/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.kacars.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 *
 * @author mbuse
 */
@Root(name="kacars")
public class PIREPRequest {
    
    @Element @Path("switch")
    private String data = "pirep";
    
    @Element @Path("verify")
    public String pilotID;
    
    @Element @Path("pirep")
    public String flightNumber;
    
    @Element @Path("pirep")
    public String depICAO;
    
    @Element @Path("pirep")
    public String arrICAO;
    
    @Element @Path("pirep")
    public String registration;
    
    @Element @Path("pirep")
    public int pax;
    
    @Element @Path("pirep")
    public int cargo;
    
    @Element @Path("pirep")
    public int fuelUsed;
    
    /** Flight time, format: hh.mm, e.g. 03.30 **/
    @Element @Path("pirep")
    public String flightTime;
    
    @Element @Path("pirep")
    public int landing;
    
    @Element @Path("pirep")
    public String comments;
    
    @Element @Path("pirep")
    public String log;
    
    public PIREPRequest(Flight flight) {
        if (flight!=null) {
            this.flightNumber = flight.flightNumber;
            this.registration = flight.aircraftReg;
            this.depICAO = flight.depICAO;
            this.arrICAO = flight.arrICAO;
            this.pax = flight.aircraftMaxPax;
            this.cargo = flight.aircraftCargo;
        }
    }
    
    public PIREPRequest() {
        
    }

    @Override
    public String toString() {
        return "{ 'class':'" + getClass().getName() 
                + "', pilotID:'" + pilotID 
                + "', flightNumber:'" + flightNumber 
                + "', depICAO:'" + depICAO 
                + "', arrICAO:'" + arrICAO 
                + "', registration:'" + registration 
                + "', pax:" + pax 
                + ", cargo:" + cargo 
                + ", fuelUsed:" + fuelUsed 
                + ", flightTime:'" + flightTime 
                + "', landing:'" + landing 
                + "', comments:'" + comments 
                + "', log:'" + log + "'}";
    }
    
    
    
}
