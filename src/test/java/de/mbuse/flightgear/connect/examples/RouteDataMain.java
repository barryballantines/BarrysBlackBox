package de.mbuse.flightgear.connect.examples;


import de.mbuse.flightgear.connect.HttpPropertyServiceImpl;
import de.mbuse.flightgear.connect.PropertyService;
import de.mbuse.flightgear.connect.ServerConfig;
import de.mbuse.flightgear.pireprecorder.FGFlightDataRetrievalImpl;
import de.mbuse.flightgear.pireprecorder.RouteInformation;
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
