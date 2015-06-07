/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox;

/**
 *
 * @author mbuse
 */
public class FlightTrackingResult {
    public int flightTimeMinutes;
    public int landingRateFPM;
    public long fuelConsumption;
    
    public int[] getFlightTimeHoursAndMinutes() {
        int[] hhmm = new int[2];
        hhmm[0] = (int) (flightTimeMinutes/60);
        hhmm[1] = (int) (flightTimeMinutes - (60*hhmm[0]));
        return hhmm;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + this.flightTimeMinutes;
        hash = 61 * hash + this.landingRateFPM;
        hash = 61 * hash + (int) (this.fuelConsumption ^ (this.fuelConsumption >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FlightTrackingResult other = (FlightTrackingResult) obj;
        if (this.flightTimeMinutes != other.flightTimeMinutes) {
            return false;
        }
        if (this.landingRateFPM != other.landingRateFPM) {
            return false;
        }
        if (this.fuelConsumption != other.fuelConsumption) {
            return false;
        }
        return true;
    }
    
    
}
