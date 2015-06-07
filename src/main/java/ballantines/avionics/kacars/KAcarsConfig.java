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

    public KAcarsConfig() {
    }

    public KAcarsConfig(KAcarsConfig config) {
        this.url = config.url;
        this.pilotID = config.pilotID;
        this.password = config.password;
        this.enabled = config.enabled;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.url);
        hash = 67 * hash + Objects.hashCode(this.pilotID);
        hash = 67 * hash + Objects.hashCode(this.password);
        hash = 67 * hash + (this.enabled ? 1 : 0);
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
        return true;
    }

    @Override
    public String toString() {
        return "Config{" + "url=" + url + ", user=" + pilotID + ", password=" + password + ", enabled=" + enabled + '}';
    }

    
    
    
}
