package ballantines.avionics.blackbox.service;

import ballantines.avionics.blackbox.udp.FlightData;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author mbuse
 */
public class LandingRateServiceTest {
    
    private LandingRateService service;
    
    private static final double[] AIRBORNE = new double[] { 0., 0., 0. };
    private static final double[] TOUCHDOWN = new double[] { 1., 0., 1. };
    private static final double[] GROUNDED = new double[] {1., 1., 1. };

    public LandingRateServiceTest() {
        service = new LandingRateService();
    }
    
    @Test
    public void testLandingRateService() {
        setData(-1000, AIRBORNE);
        setData(-900, AIRBORNE);
        setData(-800, AIRBORNE);
        setData(-700, AIRBORNE);
        setData(-600, AIRBORNE);
        setData(-500, AIRBORNE);
        setData(-400, AIRBORNE);
        setData(-300, AIRBORNE);
        setData(-200, AIRBORNE);
        setData(-100, AIRBORNE);
        setData(-100, AIRBORNE);
        setData(-100, TOUCHDOWN);
        setData(-50, TOUCHDOWN);
        setData(-5, GROUNDED);
        setData(-1, GROUNDED);
        setData(-1, GROUNDED);
        setData(-1, GROUNDED);
        setData(-1, GROUNDED);
        setData(-1, GROUNDED);
        setData(-1, GROUNDED);
        
        Assert.assertEquals("Wrong Landing Rate", -175., service.landingRate.get(), 0.1);
    }
    
    private void setData(double vs, double[] wow) {
        JSONObject json = new JSONObject();
        json.put("vertical-speed", vs);
        json.put("wow", new JSONArray(wow));
        service.flightDataPipe.set(new FlightData(json));
    }
    
    
}
