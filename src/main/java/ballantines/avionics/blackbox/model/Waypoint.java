package ballantines.avionics.blackbox.model;

import java.util.Locale;
import java.util.Objects;

/**
 *
 * @author mbuse
 */
public class Waypoint {
    
    public String ident;
    public double lon;
    public double lat;

    @Override
    public String toString() {
        return String.format(Locale.US, "Waypoint[%s, %.6f, %.6f]", ident, lat, lon);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Waypoint)
                && Objects.equals(ident, ((Waypoint) obj).ident) 
                && Objects.equals(lon, ((Waypoint) obj).lon)
                && Objects.equals(lat, ((Waypoint) obj).lat);
                
    }

    @Override
    public int hashCode() {
        return Objects.hash(ident, lon, lat);
    }
    
    
    
    
    
    
}
