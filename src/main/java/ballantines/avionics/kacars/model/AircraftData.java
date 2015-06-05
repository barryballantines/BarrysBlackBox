/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.kacars.model;

import java.util.List;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 *
 */
public class AircraftData {
    
    private String icao;
    private String registration;

    public AircraftData(String icao, String registration) {
        this.icao = icao;
        this.registration = registration;
    }

    public String getICAO() {
        return icao;
    }

    public String getRegistration() {
        return registration;
    }
    
}
