package ballantines.avionics.blackbox.service;

import ballantines.avionics.kacars.KAcarsConfig;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author mbuse
 */
public class PreferencesPersistenceServiceImplTest {
    
    public PreferencesPersistenceServiceImplTest() {
    }
    
    private JSONArray backup;
    private PreferencesPersistenceServiceImpl service = new PreferencesPersistenceServiceImpl();

   
    @Before
    public void setup() {
        this.backup = service.exportPreferences();
    }

    @After
    public void tearDown() {
        service.importPreferences(this.backup);
    }

    // === TESTS ===
    
    @Test
    public void testReadWriteStoredKACARSConfigs() {
        List<KAcarsConfig> expected = createTestConfigs();
        service.writeStoredKACARSConfigs(expected);
        
        List<KAcarsConfig> actual = service.readStoredKACARSConfigs();
        
        Assert.assertArrayEquals("storedKACARSConfigs", expected.toArray(), actual.toArray());
    }
    
    // === TESTS ===
    
    private List<KAcarsConfig> createTestConfigs() {
        List<KAcarsConfig> list = new ArrayList<>();
        list.add(createKAcarsConfig("www.virtuallufthansa.de", "LH123", "secret1"));
        list.add(createKAcarsConfig("www.virtualthaiairways.th", "TV123", "secret2"));
        list.add(createKAcarsConfig("www.virtualnameseairlines.vn", "VN342", "secret3"));
        
        return list;
    }
    
    private KAcarsConfig createKAcarsConfig(String url, String user, String password) {
        KAcarsConfig config = new KAcarsConfig();
        config.url = url;
        config.pilotID = user;
        config.password = password;
        config.enabled = true;
        config.liveUpdateEnabled = true;
        config.liveUpdateIntervalMS = 60000;
        config.timeout = 2000;
        return config;
    }
    
}
