/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.kacars;

import ballantines.avionics.kacars.model.Flight;
import org.javalite.http.Http;
import org.javalite.http.Post;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/**
 *
 * @author mbuse
 */
public class KAcarsClient {
    
    
    private Config config;
    private Serializer serializer = new Persister();

    public KAcarsClient() {
        super();
    }
    public KAcarsClient(Config config) {
        this();
        this.config = config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }
    
    public Flight getBid() throws Exception {
        return getBid(config.user);
    }
    
    public Flight getBid(String pilotID) throws Exception {
        Flight flight = send(Flight.class, GETBID_TEMPLATE, "getbid", pilotID);
        if (flight==null || flight.flightStatus!=1) {
            return null;
        }
        return flight;
    }
    
    public Flight getFlight(String flightNumber) throws Exception {
        Flight flight = send(Flight.class, GETFLIGHT_TEMPLATE, "getflight", flightNumber);
        return flight;
    }
    
    protected <T> T send(Class<T> responseType, String template, Object... args) throws Exception{
        String requestBody = String.format(template, args);
        
        Post response = Http.post(config.url, requestBody);
        String responseBody = response.text();
        System.out.println(responseBody);
        
        return serializer.read(responseType, responseBody);
    }
    
    // === XML DEFINITION FOR KACARS ===
    
    private static final String KACARS_BEGIN = "<kacars>";
    private static final String KACARS_END = "</kacars>";
    private static final String SWITCH_FRAGMENT = "<switch><data>%s</data></switch>";
    
    private static final String GETFLIGHT_TEMPLATE 
            = KACARS_BEGIN
                + SWITCH_FRAGMENT
                + "<pirep>"
                    + "<flightNumber>%s</flightNumber>"
                + "</pirep>"
            + KACARS_END; 
    
    private static final String GETBID_TEMPLATE 
            = KACARS_BEGIN
                + SWITCH_FRAGMENT
                + "<verify>"
                    + "<pilotID>%s</pilotID>"
                + "</verify>"
            + KACARS_END;
}
