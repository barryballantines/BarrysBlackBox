/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.kacars;

import ballantines.avionics.kacars.model.Flight;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
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
    
    public Flight getFlight(String flightNumber) throws Exception {
        String template ="<kacars>"
                + "<switch><data>getflight</data></switch>"
                + "<pirep><flightNumber>%s</flightNumber></pirep>"
                + "</kacars>";
        String body = String.format(template, flightNumber);
        
        Post response = Http.post(config.url, body);
        String responseBody = response.text();
        System.out.println(responseBody);
        
        Flight flight = serializer.read(Flight.class, responseBody);
        return flight;
    }
    
    
    
}
