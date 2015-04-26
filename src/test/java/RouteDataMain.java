
import de.mbuse.flightgear.pireprecorder.FGFlightDataRetrievalImpl;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mbuse
 */
public class RouteDataMain {
 
    public static void main(String... args) {
        FGFlightDataRetrievalImpl retrieval = new FGFlightDataRetrievalImpl();
        retrieval.setHost("localhost");
        retrieval.setPort(5500);
        
        FGFlightDataRetrievalImpl.RouteInformation info = retrieval.getRouteInformation();
        
        System.out.println(info);
        
    }
}
