/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ballantines.avionics.flightgear.connect;

import java.util.Map;

/**
 * This interface defines access to the Flightgear property tree.
 * 
 * <p>
 *  Properties are identified by their complete path 
 *  (with trailing slash but without leading slashes). Some examples:
 * </p>
 * 
 * <ul>
 *   <li>/consumables/fuel/total-fuel-lbs</li>
 *   <li>/controls/flight/elevator-trim</li>
 * </ul>
 * 
 * This is true for all methods of this service.
 * 
 * @author mbuse
 */
public interface PropertyService {
    
    /**
     * Reads properties from Flightgear and stores them into the provided map.
     * 
     * @param properties    A map containing the requested properties as keys.
     * @return  the same map, filled with the property values.
     */
    Map<String,Object> readProperties(Map<String, Object> properties);
    
    /**
     * Reads the given properties from Flightgear and returns them as a map.
     * @param properties    A sequence of property names.
     * @return  a map filled with properties
     */
    Map<String, Object> readProperties(String... properties);
    
    /** Reads a single property from Flightgear
     * 
     * @param <T>           T (because DocLint sucks!)
     * @param property      The name of the property
     * @return the value
     */
    <T> T readProperty(String property);
    
    /**
     * Writes properties to Flightgear.
     * 
     * @param properties    A map containing propertys and their values
     */
    void writeProperties(Map<String, Object> properties);
    
    /**
     * Writes a single property to Flightgear.
     * 
     * @param property      The property name
     * @param value         The value of the object.
     */
    void writeProperty(String property, Object value);
}
