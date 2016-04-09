/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox.util;

import de.mbuse.pipes.Pipe;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mbuse
 */
public class Log {
    
    public static Log forClass(Class<?> type) {
        Logger  logger = Logger.getLogger(type.getName());
        return new Log(logger);
    }
    
    private Log(Logger logger) {
        this.logger = logger;
    }
    
    public void info(String msg, Object... args) {
        if (logger.isLoggable(Level.INFO)) {
            this.logger.info(String.format(msg, args));
        }
    }
    
    public void debug(String msg, Object... args) {
        if (logger.isLoggable(Level.FINE)) {
            this.logger.log(Level.FINE, String.format(msg, args));
        }
    }
    
    public void trace(String msg, Object... args) {
        if (logger.isLoggable(Level.FINER)) {
            this.logger.finer(String.format(msg, args));
        }
    }
    
    public void pipeUpdated(Pipe pipe) {
        Object value = pipe.get();
        trace("Pipe updated: %s -> %s", pipe.id(), (value==null) ? "null" : value.toString());
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
    
    public boolean isInfo() {
        return logger.isLoggable(Level.INFO);
    }
    
    public boolean isTrace() {
        return logger.isLoggable(Level.FINER);
    }
    
    private Logger logger;
}
