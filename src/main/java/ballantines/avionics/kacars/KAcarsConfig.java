/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.kacars;

import java.util.Objects;

/**
 *
 * @author mbuse
 */
public class KAcarsConfig {
    
    public String url = null;
    public String pilotID = null;
    public String password = null;
    public boolean enabled = false;
    public boolean liveUpdateEnabled = false;
    public int liveUpdateIntervalMS = 30000;
    public int timeout = 10000;

    public KAcarsConfig() {
    }

    public KAcarsConfig(KAcarsConfig config) {
        this.url = config.url;
        this.pilotID = config.pilotID;
        this.password = config.password;
        this.enabled = config.enabled;
        this.liveUpdateEnabled = config.liveUpdateEnabled;
        this.liveUpdateIntervalMS = config.liveUpdateIntervalMS;
        this.timeout = config.timeout;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.url);
        hash = 67 * hash + Objects.hashCode(this.pilotID);
        hash = 67 * hash + Objects.hashCode(this.password);
        hash = 67 * hash + (this.enabled ? 1 : 0);
        hash = 67 * hash + Objects.hashCode(this.timeout);
        hash = 67 * hash + Objects.hashCode(this.liveUpdateIntervalMS);
        hash = 67 * hash + (this.liveUpdateEnabled ? 1 : 0);
                
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
        final KAcarsConfig other = (KAcarsConfig) obj;
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        if (!Objects.equals(this.pilotID, other.pilotID)) {
            return false;
        }
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        if (this.enabled != other.enabled) {
            return false;
        }
        if (this.timeout != other.timeout) {
            return false;
        }
        if (this.liveUpdateEnabled != other.liveUpdateEnabled) {
            return false;
        }
        if (this.liveUpdateIntervalMS != other.liveUpdateIntervalMS) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Config{" + "url=" + url
                + ", user=" + pilotID
                + ", password=" + password 
                + ", enabled=" + enabled 
                + ", liveUpdateEnabled=" + liveUpdateEnabled
                + ", liveUpdateIntervalMS=" + liveUpdateIntervalMS + "}";
    }

    
    
    
}
