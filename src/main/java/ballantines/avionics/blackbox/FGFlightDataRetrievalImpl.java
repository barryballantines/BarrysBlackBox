/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ballantines.avionics.blackbox;

import ballantines.avionics.flightgear.connect.PropertyService;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 * @author mbuse
 */
public class FGFlightDataRetrievalImpl implements FlightDataRetrieval {
    
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    
    private PropertyService propertyService;

    public FGFlightDataRetrievalImpl(PropertyService service) {
        this.propertyService = service;
    }
    
    public void setPropertyService(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    public PropertyService getPropertyService() {
        return propertyService;
    }
    
    @Override
    public String getAirport() {
        return propertyService.readProperty("/sim/airport/closest-airport-id");
    }

    @Override
    public long getFuel() {
        Double value = propertyService.readProperty("/consumables/fuel/total-fuel-lbs");
        return Math.round(value);
    }

    @Override
    public double getGroundspeed() {
        Double value = propertyService.readProperty("/velocities/groundspeed-kt");
        return value;
    }
    
    @Override
    public Calendar getTime() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(UTC);
        return cal;
    }
    
    public AircraftInformation getAircraftInformation() {
        Map<String,Object> p = propertyService.readProperties(
                "/sim/description",
                
        );
        
        AircraftInformation ac = new AircraftInformation();
        
        ac.name = (String) propertyService.readProperty("/sim/description");
        ac.fuelLbs = getFuel();
        
        return ac;
    }
    
    @Override
    public RouteInformation getRouteInformation() {
        String root = "/autopilot/route-manager";
        Map<String,Object> p = propertyService.readProperties(
                root + "/departure/airport",
                root + "/destination/airport",
                root + "/total-distance",
                root + "/distance-remaining-nm",
                root + "/flight-time",
                root + "/ete" );
        RouteInformation info = new RouteInformation();
        
        info.departure = (String) p.get(root + "/departure/airport");
        info.destination = (String) p.get(root + "/destination/airport");
        info.totalDistance = (Double) p.get(root + "/total-distance");
        info.distanceRemaining = (Double) p.get(root + "/distance-remaining-nm");
        info.flightTime = (Double) p.get(root + "/flight-time");
        info.estimatedTimeToDestination = (Double) p.get(root + "/ete");
        
        return info;
    }
}
