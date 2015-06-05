/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.kacars.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.simpleframework.xml.core.Persister;

/**
 *
 * @author mbuse
 */
public class FlightTest {
    
    private static final String FULL_FLIGHT_INFO_XML 
            = "<?xml version=\"1.0\"?>\n" 
            + "<sitedata>"
                + "<info>" 
                    + "<flightStatus>1</flightStatus>"
                    + "<flightNumber>EVX9757</flightNumber>"
                    + "<aircraftReg>GB-EVX757</aircraftReg>"
                    + "<aircraftICAO>B752</aircraftICAO>"
                    + "<aircraftFullName>Boeing 757-200</aircraftFullName>"
                    + "<flightLevel/>"
                    + "<aircraftMaxPax>239</aircraftMaxPax>"
                    + "<aircraftCargo>0</aircraftCargo>"
                    + "<depICAO>UMKK</depICAO>"
                    + "<arrICAO>EVRA</arrICAO>"
                    + "<route/>"
                    + "<depTime>N/A</depTime>"
                    + "<arrTime>N/A</arrTime>"
                    + "<flightTime>0</flightTime>"
                    + "<flightType>C</flightType>"
                    + "<aircraftName>B757-200</aircraftName>"
                    + "<aircraftRange>3900</aircraftRange>"
                    + "<aircraftWeight>127520/255000</aircraftWeight>"
                    + "<aircraftCruise>458</aircraftCruise>"
                + "</info>"
            + "</sitedata>" ;
    
    private static final String PARTIAL_FLIGHT_INFO_XML 
            = "<?xml version=\"1.0\"?>\n" 
            + "<sitedata>"
                + "<info>" 
                    + "<flightStatus>1</flightStatus>"
                    + "<flightNumber>EVX9757</flightNumber>"
                    + "<aircraftReg>GB-EVX757</aircraftReg>"
                    + "<aircraftICAO>B752</aircraftICAO>"
                    + "<aircraftMaxPax>239</aircraftMaxPax>"
                    + "<aircraftCargo>0</aircraftCargo>"
                    + "<depICAO>UMKK</depICAO>"
                    + "<arrICAO>EVRA</arrICAO>"
                    + "<flightTime>0</flightTime>"
                    + "<flightType>C</flightType>"
                + "</info>"
            + "</sitedata>" ;
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testDeserializationFull() throws Exception {
        Persister persister = new Persister();
        
        Flight f = persister.read(Flight.class, FULL_FLIGHT_INFO_XML);
        
        assertEquals("flightStatus", 1, f.flightStatus);
        assertEquals("flightNumber", "EVX9757", f.flightNumber);
        assertEquals("aircraftReg", "GB-EVX757", f.aircraftReg);
        assertEquals("aircraftICAO", "B752", f.aircraftICAO);
        assertEquals("aircraftFullName", "Boeing 757-200", f.aircraftFullName);
        assertEquals("flightLevel", null, f.flightLevel);
        assertEquals("aircraftMaxPax", 239, f.aircraftMaxPax);
        assertEquals("aircraftCargo", 0, f.aircraftCargo);
        assertEquals("depICAO", "UMKK", f.depICAO);
        assertEquals("arrICAO", "EVRA", f.arrICAO);
        assertEquals("route", null, f.route);
        assertEquals("depTime", "N/A", f.depTime);
        assertEquals("arrTime", "N/A", f.arrTime);
        assertEquals("flightTime", "0", f.flightTime);
        assertEquals("flightType", "C", f.flightType);
        assertEquals("aircraftName", "B757-200", f.aircraftName);
        assertEquals("aircraftRange", 3900, f.aircraftRange);
        assertEquals("aircraftWeight", "127520/255000", f.aircraftWeight);
        assertEquals("aircraftCruise", 458, f.aircraftCruise);
    }
    
    @Test
    public void testDeserializationPartial() throws Exception {
        Persister persister = new Persister();
        
        Flight f = persister.read(Flight.class, PARTIAL_FLIGHT_INFO_XML);
        
        // KNOWN:
        assertEquals("flightStatus", 1, f.flightStatus);
        assertEquals("flightNumber", "EVX9757", f.flightNumber);
        assertEquals("aircraftReg", "GB-EVX757", f.aircraftReg);
        assertEquals("aircraftICAO", "B752", f.aircraftICAO);
        assertEquals("aircraftMaxPax", 239, f.aircraftMaxPax);
        assertEquals("aircraftCargo", 0, f.aircraftCargo);
        assertEquals("flightTime", "0", f.flightTime);
        assertEquals("flightType", "C", f.flightType);
        assertEquals("depICAO", "UMKK", f.depICAO);
        assertEquals("arrICAO", "EVRA", f.arrICAO);
        // UNKNOWN:
        assertEquals("aircraftFullName", null, f.aircraftFullName);
        assertEquals("flightLevel", null, f.flightLevel);
        assertEquals("route", null, f.route);
        assertEquals("depTime", null, f.depTime);
        assertEquals("arrTime", null, f.arrTime);
        assertEquals("aircraftName", null, f.aircraftName);
        assertEquals("aircraftWeight", null, f.aircraftWeight);
        assertEquals("aircraftCruise", -1, f.aircraftCruise);
        assertEquals("aircraftRange", -1, f.aircraftRange);
    }
}
