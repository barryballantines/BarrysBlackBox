/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.log;

/**
 *
 * @author mbuse
 */
public enum FlightPhase {
    BOARDING,
    PUSHBACK,
    TAXI,
    TAKEOFF,
    CLIMB,
    CRUISE,
    DESCENT,
    LANDING,
    SHUTDOWN;
    
    @Override
    public String toString() {
        switch (this) {
            case BOARDING: return "Boarding";
            case PUSHBACK: return "Pushback";
            case TAXI: return "Taxi";
            case TAKEOFF: return "Takeoff";
            case CLIMB: return "Climb";
            case CRUISE: return "Cruise";
            case DESCENT: return "Descent";
            case LANDING: return "Landing";
            case SHUTDOWN: return "Disembarking";
        }
        return "";
    }
}
