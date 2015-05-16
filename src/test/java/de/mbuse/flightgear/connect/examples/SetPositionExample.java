package de.mbuse.flightgear.connect.examples;

import de.mbuse.flightgear.connect.HttpPropertyServiceImpl;
import de.mbuse.flightgear.connect.PropertyService;
import de.mbuse.flightgear.connect.ServerConfig;
import java.util.HashMap;
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
public class SetPositionExample {
    
    public static void main(String... args) {
        ServerConfig config = new ServerConfig("localhost", 5500);
        PropertyService service = new HttpPropertyServiceImpl(config);
        
        Map<String, Object> position = new HashMap<>();
        position.put("/position/latitude-deg", 52.31835581);
        position.put("/position/longitude-deg", 4.796849554);
        position.put("/position/altitude-ft",34.3224337705);
        position.put("/orientation/heading-deg", 266.888);
        
        service.writeProperties(position);
    }
}
