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
    DESCEND,
    LANDING,
    SHUTDOWN;
}
