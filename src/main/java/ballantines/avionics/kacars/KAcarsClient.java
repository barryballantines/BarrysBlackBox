/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.kacars;

import ballantines.avionics.kacars.model.AircraftData;
import ballantines.avionics.kacars.model.AircraftDataList;
import ballantines.avionics.kacars.model.Flight;
import ballantines.avionics.kacars.model.LoginStatus;
import java.util.Collections;
import java.util.List;
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
    
    public boolean verify() throws Exception {
        LoginStatus status = send(LoginStatus.class, VERIFY_TEMPLATE, "verify", config.user, config.password);
        return status.isLoggedIn();
    }
    
    public List<AircraftData> getAllAircrafts() throws Exception {
        AircraftDataList list = send(AircraftDataList.class, SIMPLE_ACTION_TEMPLATE, "aircraft");
        
        return (list!=null) 
                ? list.getAircrafts()
                : AircraftDataList.EMPTY_LIST;
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
        String responseBody = send(template, args);
        return serializer.read(responseType, responseBody);
    }

    protected String send(String template, Object... args) {
        String requestBody = String.format(template, args);
        return send(requestBody);
    }

    protected String send(String requestBody) {
        Post response = Http.post(config.url, requestBody);
        String responseBody = response.text();
        System.out.println(responseBody);
        return responseBody;
    }
    
    // === XML DEFINITION FOR KACARS ===
    
    private static final String KACARS_BEGIN = "<kacars>";
    private static final String KACARS_END = "</kacars>";
    private static final String SWITCH_FRAGMENT = "<switch><data>%s</data></switch>";
    
    private static final String VERIFY_TEMPLATE 
            = KACARS_BEGIN
                + SWITCH_FRAGMENT
                + "<verify>"
                    + "<pilotID>%s</pilotID>"
                    + "<password>%s</password>"
                + "</verify>"
            + KACARS_END;
    
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
    
    private static final String SIMPLE_ACTION_TEMPLATE 
            = KACARS_BEGIN
                + SWITCH_FRAGMENT
            + KACARS_END;
}
