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

    
    
    
}
