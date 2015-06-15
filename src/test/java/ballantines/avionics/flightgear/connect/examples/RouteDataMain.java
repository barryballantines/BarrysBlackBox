package ballantines.avionics.flightgear.connect.examples;


import ballantines.avionics.flightgear.connect.HttpPropertyServiceImpl;
import ballantines.avionics.flightgear.connect.PropertyService;
import ballantines.avionics.flightgear.connect.ServerConfig;
import ballantines.avionics.blackbox.service.FGFlightDataRetrievalImpl;
import ballantines.avionics.blackbox.model.RouteInformation;
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
        HttpPropertyServiceImpl propertyService = new HttpPropertyServiceImpl();
        FGFlightDataRetrievalImpl retrieval = new FGFlightDataRetrievalImpl(propertyService);
        propertyService.setServerConfig(new ServerConfig("localhost", 5500));
        
        RouteInformation info = retrieval.getRouteInformation();
        
        System.out.println(info);
        
    }
}
