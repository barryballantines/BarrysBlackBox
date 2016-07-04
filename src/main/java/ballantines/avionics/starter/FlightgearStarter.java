package ballantines.avionics.starter;

import ballantines.avionics.blackbox.model.Position;
import ballantines.avionics.blackbox.service.PersistenceService;
import ballantines.avionics.blackbox.service.PreferencesPersistenceServiceImpl;
import ballantines.avionics.blackbox.udp.FlightData;
import java.util.Locale;

/**
 *
 * @author mbuse
 */
public class FlightgearStarter {
    
    public static void main(String... args) {
        String flightgearStartCmd = "C:\\Private\\FlightGear-3.6.0\\bin\\fgfs.exe" +
                "  --fg-root=C:\\Private\\FlightGear-3.6.0/data" +
                "  --fg-aircraft=C:/Users/mbuse/Documents/FlightGear/Aircraft" +
                "  --aircraft=777-200" +
                "  --disable-random-objects" +
                "  --prop:/sim/rendering/random-vegetation=false" +
                "  --disable-hud-3d" +
                "  --disable-specular-highlight" +
                "  --disable-ai-models" +
                "  --disable-ai-traffic" +
                "  --enable-real-weather-fetch" +
                "  --prop:/sim/menubar/autovisibility/enabled=1" +
                "  --geometry=800x600" +
                "  --bpp=32" +
                "  --timeofday=noon" +
                "  --enable-terrasync" +
                "  --httpd=5500" +
                "  --disable-fgcom" +
                "  --generic=socket,out,2,localhost,5555,udp,blackbox";
        String winCmdPrefix = "cmd /C ";
        
        PersistenceService persistence = new PreferencesPersistenceServiceImpl();
        
        Position pos = persistence.readKnownParkingPosition("EDDH");
        
        String flightDataArgs = 
                String.format(Locale.US, " --lon=%f --lat=%f --heading=%.0f", pos.lon, pos.lat, pos.hdg);
        
        System.out.println("Would execute: \n" + winCmdPrefix + flightgearStartCmd + flightDataArgs);
        
        try {
            Runtime.getRuntime().exec(winCmdPrefix + flightgearStartCmd + flightDataArgs);
        } catch (Throwable th) {
            
        }
    }
    
}
