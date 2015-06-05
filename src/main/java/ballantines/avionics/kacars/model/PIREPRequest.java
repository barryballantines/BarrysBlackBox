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
    
    
    
}
