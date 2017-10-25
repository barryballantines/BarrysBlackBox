package ballantines.avionics.kacars;

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mbuse
 */
public class KAcarsConfigTest {
    
    public KAcarsConfigTest() {
    }

    @Test
    public void testFullKAcarsSUrl() throws Exception {
        internalTest("http://www.myvirtualairlines.com/action.php/kacars_free",
                "http://www.myvirtualairlines.com/action.php/kacars_free", 
                "http://www.myvirtualairlines.com");
    }

    @Test
    public void testKAcarsSUrlWithoutPath() throws Exception {
        internalTest("http://www.myvirtualairlines.com",
                "http://www.myvirtualairlines.com/action.php/kacars_free", 
                "http://www.myvirtualairlines.com");
        internalTest("http://www.myvirtualairlines.com/",
                "http://www.myvirtualairlines.com/action.php/kacars_free", 
                "http://www.myvirtualairlines.com");
    }
    
    @Test
    public void testSecureKAcarsSUrlWithoutPath() throws Exception {
        internalTest("https://www.myvirtualairlines.com",
                "https://www.myvirtualairlines.com/action.php/kacars_free", 
                "https://www.myvirtualairlines.com");
    }
    
    @Test
    public void testKAcarsSUrlWithPathAndPort() throws Exception {
        internalTest("http://www.myvirtualairlines.com:8080/action.php/kacars_free",
                "http://www.myvirtualairlines.com:8080/action.php/kacars_free", 
                "http://www.myvirtualairlines.com:8080");
    }
    
    @Test
    public void testKAcarsSUrlWithoutPathAndWithPort() throws Exception {
        internalTest("http://www.myvirtualairlines.com:8080",
                "http://www.myvirtualairlines.com:8080/action.php/kacars_free", 
                "http://www.myvirtualairlines.com:8080");
    }
    
    @Test
    public void testKAcarsSUrlAsHostname() throws Exception {
        internalTest("www.myvirtualairlines.com",
                "http://www.myvirtualairlines.com/action.php/kacars_free", 
                "http://www.myvirtualairlines.com");
    }
    
    @Test
    public void testCustomKAcarsUrl() throws Exception {
        internalTest("http://localhost:8080/protocol/custom/kacars",
                "http://localhost:8080/protocol/custom/kacars", 
                "http://localhost:8080");
    }
    
    @Test
    public void testDevelopmentUrl() throws Exception {
        internalTest("http://localhost:8080/myvirtualairlines/action.php/kacars_free",
                "http://localhost:8080/myvirtualairlines/action.php/kacars_free", 
                "http://localhost:8080/myvirtualairlines/");
    }
    
    
    protected void internalTest(String configuredUrl, String expKacarsUrl, String expVAUrl) throws IOException {
        KAcarsConfig config = new KAcarsConfig();
        config.url = configuredUrl;
        
        assertEquals(config.getKAcarsURL().toString(), expKacarsUrl);
        assertEquals(config.getVAHomeUrl().toString(), expVAUrl);
    }
    

    
    
}
