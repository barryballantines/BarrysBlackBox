/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mbuse
 */
public class L {
    
    public static L forClass(Class<?> type) {
        Logger  logger = Logger.getLogger(type.getName());
        return new L(logger);
    }
    
    private L(Logger logger) {
        this.logger = logger;
    }
    
    public void info(String msg, Object... args) {
        if (logger.isLoggable(Level.INFO)) {
            this.logger.info(String.format(msg, args));
        }
    }
    
    public void trace(String msg, Object... args) {
        if (logger.isLoggable(Level.FINER)) {
            this.logger.finer(String.format(msg, args));
        }
    }
    
    public void warn(String msg, Object... args) {
        if (logger.isLoggable(Level.WARNING)) {
            this.logger.warning(String.format(msg, args));
        }
    }
    
    public void error(Exception ex, String msg, Object... args) {
        if (logger.isLoggable(Level.SEVERE)) {
            this.logger.log(Level.SEVERE, String.format(msg, args), ex);
        }
    }
    
    private Logger logger;
}
