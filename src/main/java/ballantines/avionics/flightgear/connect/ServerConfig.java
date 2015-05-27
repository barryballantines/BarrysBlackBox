/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ballantines.avionics.flightgear.connect;

import java.util.Objects;

/**
 *
 * @author mbuse
 */
public class ServerConfig {
    
    private String protocol;
    private String host;
    private int port;

    public ServerConfig(String protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public ServerConfig(String host, int port) {
        this("http", host, port);
    }
    
    public ServerConfig() {
        this("http", "localhost", 5500);
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final ServerConfig other = (ServerConfig) obj;
        if (!Objects.equals(this.protocol, other.protocol)) {
            return false;
        }
        if (!Objects.equals(this.host, other.host)) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        return true;
    }
    
    public String fullAddress() {
        return protocol + "://" + host + ":" + port;
    }

    @Override
    public String toString() {
        return fullAddress();
    }
    
    
    
    
    
}
