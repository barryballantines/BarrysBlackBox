/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.mbuse.flightgear.pireprecorder;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 *
 * @author mbuse
 */
public class RoutePanel implements Initializable {
    
    public static Parent create(Services services) throws IOException {
        RoutePanel controller = new RoutePanel();
        controller.setRetrieval(services.getFlightDataRetrieval());
        FXMLLoader loader = new FXMLLoader(PIREPForm.class.getResource("/fxml/route.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }

    @FXML private ProgressBar flightProgress;
    @FXML private Label departureAirportLbl;
    @FXML private Label destinationAirportLbl;
    @FXML private Label estimatedTimeOfArrivalLbl;
    @FXML private Label estimatedTimeEnrouteLbl;
    @FXML private Label elapsedTimeLbl;
    @FXML private Label takeoffTimeLbl;
    @FXML private Label totalDistanceLbl;
    @FXML private Label remainingDistanceLbl;
    @FXML private Label elapsedDistanceLbl;
    
    private FlightDataRetrieval retrieval;
    private Timer timer;

    
    public void setRetrieval(FlightDataRetrieval retrieval) {
        this.retrieval = retrieval;
    }

    public FlightDataRetrieval getRetrieval() {
        return retrieval;
    }
    
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        timer = new Timer("Route Information Timer");
        timer.schedule(new RouteInformationRetrievalTask(), 2000, 10000);
    }
    
    protected void updateData(RouteInformation data) {
        double progress = (data.totalDistance>0.0)
                ? 1.0 - data.distanceRemaining / data.totalDistance
                : 0.0;
        
        flightProgress.setProgress(progress);
        
        departureAirportLbl.setText(data.departure);
        destinationAirportLbl.setText(data.destination);
        
        int ete = (int) data.estimatedTimeToDestination;
        Calendar eta = Calendar.getInstance();
        eta.add(Calendar.SECOND, ete);
        
        estimatedTimeEnrouteLbl.setText(Formats.secondsToHHMM(ete));
        estimatedTimeOfArrivalLbl.setText(Formats.toUTC(eta));
        
        elapsedTimeLbl.setText(Formats.secondsToHHMM((int) data.flightTime));
        elapsedDistanceLbl.setText(Formats.nauticalMiles(data.totalDistance - data.distanceRemaining));
        
        totalDistanceLbl.setText(Formats.nauticalMiles(data.totalDistance));
        remainingDistanceLbl.setText(Formats.nauticalMiles(data.distanceRemaining));
        
        Calendar to = Calendar.getInstance();
        to.add(Calendar.SECOND, (int) -data.flightTime);
        
        takeoffTimeLbl.setText(Formats.toUTC(to));
        
        
    }
    
    
    class RouteInformationRetrievalTask extends TimerTask {

        @Override
        public void run() {
            try {
                final RouteInformation route = getRetrieval().getRouteInformation();

                Platform.runLater(new Runnable() { @Override public void run() {
                    updateData(route);
                }});
            } catch(Exception e) {
                System.err.println("Cannot retrieve route information: " + e);
            }
        }
        
    }
}
