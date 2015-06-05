/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.kacars.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 *
 */
@Root(name="aircraftdata", strict=false)
public class AircraftDataList {
    
    public static final List<AircraftData> EMPTY_LIST = Collections.emptyList();
    
    @ElementList(name = "info")
    private List<String> aircraftDataEntries;
    
    private List<AircraftData> aircraftDatas = null;
    
    
    public List<AircraftData> getAircrafts() {
        if (aircraftDatas==null && aircraftDataEntries!=null) {
            List<AircraftData> data = new ArrayList<>();
            
            Iterator<String> iter = aircraftDataEntries.iterator();
            while (iter.hasNext()) {
                data.add(new AircraftData(iter.next(), iter.next()));
            }
            aircraftDatas = data;
        }
        else {
            return EMPTY_LIST;
        }
        
        return aircraftDatas;
    }
    
}
