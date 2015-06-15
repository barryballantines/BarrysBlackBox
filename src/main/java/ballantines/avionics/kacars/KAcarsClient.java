/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.kacars;

import ballantines.avionics.blackbox.util.Log;
import ballantines.avionics.kacars.model.AircraftData;
import ballantines.avionics.kacars.model.AircraftDataList;
import ballantines.avionics.kacars.model.Flight;
import ballantines.avionics.kacars.model.LiveUpdateData;
import ballantines.avionics.kacars.model.LoginStatus;
import ballantines.avionics.kacars.model.PIREPRequest;
import ballantines.avionics.kacars.model.PIREPStatus;
import java.io.StringWriter;
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
    
    private static Log L = Log.forClass(KAcarsClient.class);
    
    private KAcarsConfig config = new KAcarsConfig();
    private Serializer serializer = new Persister();

    public KAcarsClient() {
        super();
    }
    public KAcarsClient(KAcarsConfig config) {
        this();
        this.config = config;
    }

    public void setConfig(KAcarsConfig config) {
        this.config = config;
    }
    
    public boolean isEnabled() {
        return config.enabled;
    }
    
    public boolean verify() throws Exception {
        LoginStatus status = send(LoginStatus.class, VERIFY_TEMPLATE, "verify", config.pilotID, config.password);
        return status.isLoggedIn();
    }
    
    public List<AircraftData> getAllAircrafts() throws Exception {
        AircraftDataList list = send(AircraftDataList.class, SIMPLE_ACTION_TEMPLATE, "aircraft");
        
        return (list!=null) 
                ? list.getAircrafts()
                : AircraftDataList.EMPTY_LIST;
    }
    
    public Flight getBid() throws Exception {
        return getBid(config.pilotID);
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
    
    public boolean filePIREP(PIREPRequest pirep) throws Exception {
        if (pirep.pilotID==null) {
            pirep.pilotID = config.pilotID;
        }
        String body = toXML(pirep);
        PIREPStatus status = send(PIREPStatus.class, body);
        return status.isSuccess();
    }
    
    public void liveUpdate(LiveUpdateData data) throws Exception {
        if (data.pilotID==null) {
            data.pilotID = config.pilotID;
        }
        String body = toXML(data);
        sendBody(body);
    }
    
    protected String toXML(Object request) throws Exception{
        StringWriter stringWriter = new StringWriter();
        serializer.write(request, stringWriter);
        return stringWriter.toString();
    }
    
    protected <T> T send(Class<T> responseType, String template, Object... args) throws Exception{
        String responseBody = send(template, args);
        
        return serializer.read(responseType, fixResponse(responseBody));
    }
    
    protected String fixResponse(String response) {
        if (response.startsWith("<?xml")) {
            return response;
        }
        else {
            int start = response.indexOf("<?xml");
            return response.substring(start);
        }
    }

    protected String send(String template, Object... args) {
        String requestBody = String.format(template, args);
        return sendBody(requestBody);
    }

    protected String sendBody(String requestBody) {
        if (isEnabled()) {
            L.info("Sending Request: %s", requestBody);
            Post response = Http.post(config.url, requestBody.getBytes(), config.timeout, config.timeout);
            String responseBody = response.text();
            L.info("Receiving Response: %s", responseBody);
            return responseBody;
        }
        else {
            L.info("KAcarsClient is not enabled.");
            L.info("Would send: %s ", requestBody);
            throw new IllegalStateException("KAcarsClient is not enabled!");
        }
    }
    
    private void checkEnabled() {
        if (!isEnabled()) {
            throw new IllegalStateException("KAcarsClient is not enabled!");
        }
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
