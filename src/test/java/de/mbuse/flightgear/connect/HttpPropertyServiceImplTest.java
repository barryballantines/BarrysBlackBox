/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.mbuse.flightgear.connect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;
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
public class HttpPropertyServiceImplTest {
    
    private MockHttpPropertyService service = new MockHttpPropertyService();
    
    public HttpPropertyServiceImplTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
        service.getSubmitUrls().clear();
    }

    /**
     * Test of readProperties method, of class HttpPropertyServiceImpl.
     */
    @org.junit.Test
    public void testFindRootAndDepth() {
        System.out.println("testFindRootAndDepth");
        Set<String> props = new HashSet<String>();
        
        props.add("/controls/flight/elevator");
        service.assertRootAndDepth(props, "/controls/flight/elevator", 0);
        
        props.add("/controls/flight/elevator-trim");
        service.assertRootAndDepth(props, "/controls/flight", 1);
        
        props.add("/controls/flight/yet/another/test");
        service.assertRootAndDepth(props, "/controls/flight", 3);
        
    }
    
    @org.junit.Test
    public void testReadProperties_Map() {
        Map<String, Object> p = new HashMap<>();
        
        p.put("/consumables/fuel/total-fuel-kg", null);
        p.put("/consumables/fuel/total-fuel-lbs", null);
        p.put("/consumables/fuel/tank/empty", null);
        p.put("/consumables/fuel/tank[1]/empty", null);
        p.put("/consumables/fuel/tank[2]/empty", null);
        
        service.readProperties(p);
        
        assertPropertyValues(p, "/consumables/fuel/total-fuel-kg", 8957.598911);
        assertPropertyValues(p, "/consumables/fuel/total-fuel-lbs", 19748.12518);
        assertPropertyValues(p, "/consumables/fuel/tank/empty", false);
        assertPropertyValues(p, "/consumables/fuel/tank[1]/empty", false);
        assertPropertyValues(p, "/consumables/fuel/tank[2]/empty", false);
    }
    
    @org.junit.Test
    public void testWriteProperties_Map() {
        Map<String, Object> p = new HashMap<>();
        
        p.put("/consumables/fuel/total-fuel-kg", 8957.598911);
        p.put("/consumables/fuel/total-fuel-lbs", 19748.12518);
        p.put("/consumables/fuel/tank/empty", true);
        p.put("/consumables/fuel/tank[1]/empty", false);
        p.put("/consumables/fuel/tank[2]/empty", false);
        
        service.writeProperties(p);
        
        assertEquals("# of write requests", 4, service.getSubmitUrls().size());
    }
    
    
    
    private void assertPropertyValues(Map<String,Object> p, String property, Object value) {
        assertEquals(property, value, p.get(property));
    }
    
    private static class MockHttpPropertyService extends HttpPropertyServiceImpl {
        
        public void assertRootAndDepth(Set<String> props, String root, int d) {
            
            RootAndDepth r = super.findRootAndDepth(props);
            assertEquals("root", root, r.root);
            assertEquals("depth", d, r.depth);
        }

        @Override
        protected JSONObject readPropertyNode(String property, int depth) {
            return new JSONObject(CONSUMABLES_JSON);
        }

        @Override
        protected void submitUrl(String url) {
            System.out.println("submitUrl: " + url);
            submitUrls.add(url);
        }

        public Set<String> getSubmitUrls() {
            return submitUrls;
        }
        
        
        private Set<String> submitUrls = new HashSet<>();
    }
    
    
   private static final String CONSUMABLES_JSON = "{\"path\":\"/consumables\",\"name\":\"consumables\",\"type\":\"-\",\"index\":0,\"nChildren\":1,\"children\":[{\"path\":\"/consumables/fuel\",\"name\":\"fuel\",\"type\":\"-\",\"index\":0,\"nChildren\":9,\"children\":[{\"path\":\"/consumables/fuel/tank\",\"name\":\"tank\",\"type\":\"-\",\"index\":0,\"nChildren\":17,\"children\":[{\"path\":\"/consumables/fuel/tank/name\",\"name\":\"name\",\"value\":\"LH Wing\",\"type\":\"unspecified\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank/level-kg\",\"name\":\"level-kg\",\"value\":\"3426.153298\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank/density-kgpm3\",\"name\":\"density-kgpm3\",\"value\":\"805.2335393\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank/capacity-m3\",\"name\":\"capacity-m3\",\"value\":\"4.255208415\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank/unusable-m3\",\"name\":\"unusable-m3\",\"value\":\"0\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank/level-m3\",\"name\":\"level-m3\",\"value\":\"4.254856673\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank/level-norm\",\"name\":\"level-norm\",\"value\":\"0.9999173384\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank/density-ppg\",\"name\":\"density-ppg\",\"value\":\"6.719999558\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank/level-lbs\",\"name\":\"level-lbs\",\"value\":\"7553.37506\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank/level-gal_us\",\"name\":\"level-gal_us\",\"value\":\"1124.01422\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank/level-gal_imp\",\"name\":\"level-gal_imp\",\"value\":\"935.9376239\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank/capacity-gal_us\",\"name\":\"capacity-gal_us\",\"value\":\"1124.10714\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank/unusable-gal_us\",\"name\":\"unusable-gal_us\",\"value\":\"0\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank/capacity-gal_imp\",\"name\":\"capacity-gal_imp\",\"value\":\"936.0149965\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank/unusable-gal_imp\",\"name\":\"unusable-gal_imp\",\"value\":\"0\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank/empty\",\"name\":\"empty\",\"value\":\"false\",\"type\":\"bool\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank/selected\",\"name\":\"selected\",\"value\":\"true\",\"type\":\"bool\",\"index\":0,\"nChildren\":0}]},{\"path\":\"/consumables/fuel/tank[1]\",\"name\":\"tank\",\"type\":\"-\",\"index\":1,\"nChildren\":17,\"children\":[{\"path\":\"/consumables/fuel/tank[1]/name\",\"name\":\"name\",\"value\":\"RH Wing\",\"type\":\"unspecified\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[1]/level-kg\",\"name\":\"level-kg\",\"value\":\"3426.153298\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[1]/density-kgpm3\",\"name\":\"density-kgpm3\",\"value\":\"805.2335393\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[1]/capacity-m3\",\"name\":\"capacity-m3\",\"value\":\"4.255208415\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[1]/unusable-m3\",\"name\":\"unusable-m3\",\"value\":\"0\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[1]/level-m3\",\"name\":\"level-m3\",\"value\":\"4.254856673\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[1]/level-norm\",\"name\":\"level-norm\",\"value\":\"0.9999173384\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[1]/density-ppg\",\"name\":\"density-ppg\",\"value\":\"6.719999558\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[1]/level-lbs\",\"name\":\"level-lbs\",\"value\":\"7553.37506\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[1]/level-gal_us\",\"name\":\"level-gal_us\",\"value\":\"1124.01422\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[1]/level-gal_imp\",\"name\":\"level-gal_imp\",\"value\":\"935.9376239\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[1]/capacity-gal_us\",\"name\":\"capacity-gal_us\",\"value\":\"1124.10714\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[1]/unusable-gal_us\",\"name\":\"unusable-gal_us\",\"value\":\"0\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[1]/capacity-gal_imp\",\"name\":\"capacity-gal_imp\",\"value\":\"936.0149965\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[1]/unusable-gal_imp\",\"name\":\"unusable-gal_imp\",\"value\":\"0\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[1]/empty\",\"name\":\"empty\",\"value\":\"false\",\"type\":\"bool\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[1]/selected\",\"name\":\"selected\",\"value\":\"true\",\"type\":\"bool\",\"index\":0,\"nChildren\":0}]},{\"path\":\"/consumables/fuel/tank[2]\",\"name\":\"tank\",\"type\":\"-\",\"index\":2,\"nChildren\":17,\"children\":[{\"path\":\"/consumables/fuel/tank[2]/name\",\"name\":\"name\",\"value\":\"Center Main\",\"type\":\"unspecified\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[2]/level-kg\",\"name\":\"level-kg\",\"value\":\"2105.292315\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[2]/density-kgpm3\",\"name\":\"density-kgpm3\",\"value\":\"805.2335393\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[2]/capacity-m3\",\"name\":\"capacity-m3\",\"value\":\"2.614863135\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[2]/unusable-m3\",\"name\":\"unusable-m3\",\"value\":\"0\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[2]/level-m3\",\"name\":\"level-m3\",\"value\":\"2.614511459\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[2]/level-norm\",\"name\":\"level-norm\",\"value\":\"0.9998655086\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[2]/density-ppg\",\"name\":\"density-ppg\",\"value\":\"6.719999558\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[2]/level-lbs\",\"name\":\"level-lbs\",\"value\":\"4641.37506\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[2]/level-gal_us\",\"name\":\"level-gal_us\",\"value\":\"690.6808579\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[2]/level-gal_imp\",\"name\":\"level-gal_imp\",\"value\":\"575.1121202\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[2]/capacity-gal_us\",\"name\":\"capacity-gal_us\",\"value\":\"690.7737611\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[2]/unusable-gal_us\",\"name\":\"unusable-gal_us\",\"value\":\"0\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[2]/capacity-gal_imp\",\"name\":\"capacity-gal_imp\",\"value\":\"575.1894783\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[2]/unusable-gal_imp\",\"name\":\"unusable-gal_imp\",\"value\":\"0\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[2]/empty\",\"name\":\"empty\",\"value\":\"false\",\"type\":\"bool\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/tank[2]/selected\",\"name\":\"selected\",\"value\":\"true\",\"type\":\"bool\",\"index\":0,\"nChildren\":0}]},{\"path\":\"/consumables/fuel/total-fuel-kg\",\"name\":\"total-fuel-kg\",\"value\":\"8957.598911\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/total-fuel-lbs\",\"name\":\"total-fuel-lbs\",\"value\":\"19748.12518\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/total-fuel-gal_us\",\"name\":\"total-fuel-gal_us\",\"value\":\"2938.709297\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/total-fuel-gals\",\"name\":\"total-fuel-gals\",\"value\":\"2938.709297\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/total-fuel-gal_imp\",\"name\":\"total-fuel-gal_imp\",\"value\":\"2446.987368\",\"type\":\"double\",\"index\":0,\"nChildren\":0},{\"path\":\"/consumables/fuel/total-fuel-norm\",\"name\":\"total-fuel-norm\",\"value\":\"0.9999051564\",\"type\":\"double\",\"index\":0,\"nChildren\":0}]}]}";
}
