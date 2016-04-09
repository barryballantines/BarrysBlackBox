/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.kacars.model;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;


/**
 *
 * @author mbuse
 */
@Root(name = "sitedata", strict=false)
public class Flight implements Cloneable {
    @Element(required=true) @Path("info")
    public int flightStatus = -1;
    @Element(required=false) @Path("info")
    public String flightNumber;
    @Element(required=false) @Path("info")
    public String aircraftReg;
    @Element(required=false) @Path("info")
    public String aircraftICAO;
    @Element(required=false) @Path("info")
    public String aircraftName;
    @Element(required=false) @Path("info")
    public String aircraftFullName;
    @Element(required=false) @Path("info")
    public int aircraftMaxPax = -1;
    @Element(required=false) @Path("info")
    public int aircraftCargo = -1;
    @Element(required=false) @Path("info")
    public int aircraftRange = -1;
    @Element(required=false) @Path("info")
    public String aircraftWeight;
    @Element(required=false) @Path("info")
    public int aircraftCruise = -1;
    @Element(required=false) @Path("info")
    public String depICAO;
    @Element(required=false) @Path("info")
    public String arrICAO;
    @Element(required=false) @Path("info")
    public String route;
    @Element(required=false) @Path("info")
    public String flightLevel;
    @Element(required=false) @Path("info")
    public String depTime;
    @Element(required=false) @Path("info")
    public String arrTime;
    @Element(required=false) @Path("info")
    public String flightTime;
    @Element(required=false) @Path("info")
    public String flightType;

    @Override
    public String toString() {
        return "{" 
                + "'class' : '" + getClass().getCanonicalName()
                + "', 'flightStatus':" + flightStatus 
                + ", 'flightNumber':'" + flightNumber 
                + "', 'aircraftReg':'" + aircraftReg 
                + "', 'aircraftICAO':'" + aircraftICAO 
                + "', 'aircraftName':'" + aircraftName 
                + "', 'aircraftFullName':'" + aircraftFullName 
                + "', 'aircraftMaxPax':" + aircraftMaxPax 
                + ", 'aircraftCargo':" + aircraftCargo 
                + ", 'aircraftRange':" + aircraftRange 
                + ", 'aircraftWeight':'" + aircraftWeight 
                + "', 'aircraftCruise':" + aircraftCruise 
                + "', 'depICAO':'" + depICAO 
                + "', 'arrICAO':'" + arrICAO 
                + "', 'route':'" + route 
                + "', 'flightLevel':'" + flightLevel 
                + "', 'depTime':'" + depTime 
                + "', 'arrTime':'" + arrTime 
                + "', 'flightTime':'" + flightTime 
                + "', 'flightType':'" + flightType + "'}'";
    }
    
    public Flight clone() {
        try {
            return (Flight) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Flight should never throw this exception!");
        }
    }
}
