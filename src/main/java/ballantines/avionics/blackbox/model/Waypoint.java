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
    public double freq = Double.NaN;
    public int    track;
    public int    dist;

    
    public boolean isRadioFix() {
        return freq!=Double.NaN;
    }
    
    public boolean isVOR() {
        return isRadioFix() && freq >= 108.0 && freq <= 117.95;
    }
    
    public boolean isNDB() {
        return isRadioFix() && freq >= 190. && freq <= 535.;
    }
    
    @Override
    public String toString() {
        return (freq == Double.NaN) 
                ?  String.format(Locale.US, "Waypoint[%s, %.6f, %.6f, %03d°, %dnm]", ident, lat, lon, track, dist)
                :  String.format(Locale.US, "Waypoint[%s, %.6f, %.6f, %3.2f, %03d°, %dnm]", ident, lat, lon, freq, track, dist);
    }

    @Override
    public boolean equals(Object obj) {
        Waypoint other = (Waypoint) obj;
        return (obj instanceof Waypoint)
                && Objects.equals(ident, other.ident) 
                && Objects.equals(lon, other.lon)
                && Objects.equals(lat, other.lat)
                && Objects.equals(freq, other.freq)
                && Objects.equals(track, other.track)
                && Objects.equals(dist, other.dist);
                
    }

    @Override
    public int hashCode() {
        return Objects.hash(ident, lon, lat, freq, track, dist);
    }
    
    
    
    
    
    
}
