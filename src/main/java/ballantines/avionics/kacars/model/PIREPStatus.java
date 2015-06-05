/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.kacars.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 *
 */
@Root(name="sitedata", strict = false)
public class PIREPStatus {
    
    @Element @Path("info")
    private int pirepStatus = -1;
    
    public boolean isSuccess() {
        return pirepStatus==1;
    }
    
}
