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
 * @author mbuse
 */
@Root(name="sitedata", strict = false)
public class LoginStatus {
    
    @Element @Path("info")
    private int loginStatus = -1;
    
    public boolean isLoggedIn() {
        return loginStatus == 1;
    }

    @Override
    public String toString() {
        return "{ 'class':'" + getClass().getName() 
                + "', loginStatus:" + loginStatus + '}';
    }
    
    
}
