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
public class AircraftDataTest {
    
    private static final String AIRCRAFT_DATA_XML 
            = "<?xml version=\"1.0\"?>\n" 
            + "<aircraftdata>"
                + "<info>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>GB-EVX321</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>D-EV201</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>D-EV202</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>D-EV203</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>GB-EV201</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>GB-EV202</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>GB-EV203</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>GB-EV204</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>GB-EV205</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>GB-EV206</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>GB-EV207</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>GB-EV208</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>GB-EV209</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>GB-EV210</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>D-EV204</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>D-EV205</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>D-EV206</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>D-EV207</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>GB-EV211</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>GB-EV212</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>D-EV208</aircraftReg>"
                    + "<aircraftICAO>A321</aircraftICAO>"
                    + "<aircraftReg>GB-EV213</aircraftReg>"
                + "</info>"
            + "</aircraftdata>";
    
   
    @Test
    public void testDeserialize() throws Exception {
         Persister persister = new Persister();
        
        AircraftDataList aircrafts = persister.read(AircraftDataList.class, AIRCRAFT_DATA_XML);
        
        assertEquals("icaos length", 22, aircrafts.getAircrafts().size());
    }
    
}
