/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.kacars.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.simpleframework.xml.core.Persister;

/**
 *
 * @author mbuse
 */
public class LoginStatusTest {
    
    private static final String FULL_LOGIN_STATUS_XML 
            = "<?xml version=\"1.0\"?>\n"
            + "<sitedata>"
                + "<info>"
                    + "<loginStatus>1</loginStatus>"
                    + "<logTimeSetting>0</logTimeSetting>"
                    + "<logPauseSetting>0</logPauseSetting>"
                    + "<version>1.0.1.1</version>"
                    + "<forceOut>1</forceOut>"
                    + "<charter>1</charter>"
                + "</info>"
            + "</sitedata>";

    @Test
    public void testDeserializeFull() throws Exception {
        Persister persister = new Persister();
        
        LoginStatus s = persister.read(LoginStatus.class, FULL_LOGIN_STATUS_XML);
        
        assertEquals("isLoggedIn", true, s.isLoggedIn());
        
    }
    
}
