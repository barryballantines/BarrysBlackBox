/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox;

import ballantines.avionics.flightgear.connect.HttpPropertyServiceImpl;
import ballantines.avionics.flightgear.connect.ServerConfig;
import ballantines.avionics.blackbox.Services;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mbuse
 */
public class ServicesTest {
    
    public ServicesTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        initialServerConfig = services.getServerConfigFromUserPreferences();
        services.init();
    }
    
    @After
    public void tearDown() {
        services.writeServerConfigToUserPreferences(initialServerConfig);
    }

    /**
     * Test of get method, of class Services.
     */
    @Test
    public void testServerConfigPipeSet() {
        ServerConfig newConfig = new ServerConfig("testhost", 1234);
        services.serverConfigPipe.set(newConfig);
        
        HttpPropertyServiceImpl ps = (HttpPropertyServiceImpl) services.getPropertyService();
        assertEquals("ServerConfig not updated!", newConfig, ps.getServerConfig());
    }
    
    private Services services = Services.get();
    private ServerConfig initialServerConfig;
    
}
