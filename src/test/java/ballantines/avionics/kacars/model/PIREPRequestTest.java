/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.kacars.model;

import java.io.StringWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.simpleframework.xml.core.Persister;

/**
 *
 * @author mbuse
 */
public class PIREPRequestTest {
    
   
    @Test
    public void testFormatPIREPRequest() throws Exception {
        Persister p = new Persister();
        
        PIREPRequest req = new PIREPRequest();
        req.pilotID = "EUR123";
        req.flightNumber = "EVX12345";
        req.depICAO = "EDDH";
        req.arrICAO = "EGKK";
        req.registration = "GB-EV213";
        req.flightTime = "3.45";
        req.pax = 230;
        req.cargo = 500;
        req.fuelUsed = 24452;
        req.comments = "This is a comment...";
        req.log = "This is the Log. \n"
                + "I don't know what should be in the log, \n"
                + "but I guess, it is just some text...";
        
        StringWriter sw = new StringWriter();
        p.write(req, sw);
        
        System.out.println(sw.toString());
        
        
        
        
    }
    
}
