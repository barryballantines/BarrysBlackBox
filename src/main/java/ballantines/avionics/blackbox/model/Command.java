package ballantines.avionics.blackbox.model;

/**
 * This enumeration defines global actions that can be started from the toolbar
 * but also from other locations.
 * 
 * The idea of Commands is to de-couple the implementation of the action from its
 * trigger (Separating Business Logic from UI).
 * 
 * Commands are distributed via Services.commandPipe.
 */
public enum Command {
    
    START_RECORDING,
    FINISH_RECORDING,
    UPLOAD_PIREP,
    DOWNLOAD_BID,
    
}
