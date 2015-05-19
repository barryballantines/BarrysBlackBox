/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.mbuse.flightgear.pireprecorder;

import de.mbuse.flightgear.pireprecorder.udp.FlightData;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
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
public class RoutePanel implements Initializable, PipeUpdateListener<FlightData> {
    
    private static final int ONE_DAY = 60 * 60 * 24;
    
    public static Parent create(Services services) throws IOException {
        RoutePanel controller = new RoutePanel();
        controller.setServices(services);
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
    
    private Services services;
    private Pipe<FlightData> flightDataPipe = Pipe.newInstance("routePanel.flightData", this);

    public void setServices(Services services) {
        this.services = services;
    }

    public Services getServices() {
        return services;
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        flightDataPipe.connectTo(services.flightDataPipe);
    }

    @Override
    public void pipeUpdated(Pipe<FlightData> pipe) {
        System.out.println("[ROUTE] Model updated : " + pipe.id() + " -> " + pipe.get());
        
        updateData(pipe.get());
    }
    
    
    
    protected void updateData(final FlightData data) {
        if (data==null) {
            return;
        }
        Platform.runLater(new Runnable() { @Override public void run() {
            double totalDistance = data.getTotalDistance();
            double remainingDistance = data.getRemainingDistance();
            double flightTime = data.getFlightTime();
            double progress = (totalDistance>0.0)
                    ? 1.0 - remainingDistance / totalDistance
                    : 0.0;

            if (progress<=0.0 && flightProgress.getProgress()<0.0) {
                flightProgress.setProgress(-1.0);
            } 
            else {
                flightProgress.setProgress(progress);
            }

            departureAirportLbl.setText(data.getDeparture());
            destinationAirportLbl.setText(data.getDestination());

            int ete = (int) data.getETE();
            if (ete<=ONE_DAY) {
                Calendar eta = Calendar.getInstance();
                eta.add(Calendar.SECOND, ete);

                estimatedTimeEnrouteLbl.setText(Formats.secondsToHHMM(ete));
                estimatedTimeOfArrivalLbl.setText(Formats.toUTC(eta));
            }
            else {
                estimatedTimeEnrouteLbl.setText("unknown");
                estimatedTimeOfArrivalLbl.setText("unknown");
            }

            elapsedTimeLbl.setText(Formats.secondsToHHMM((int) flightTime));
            elapsedDistanceLbl.setText(Formats.nauticalMiles(totalDistance - remainingDistance));

            totalDistanceLbl.setText(Formats.nauticalMiles(totalDistance));
            remainingDistanceLbl.setText(Formats.nauticalMiles(remainingDistance));

            Calendar to = Calendar.getInstance();
            to.add(Calendar.SECOND, (int) -flightTime);

            takeoffTimeLbl.setText(Formats.toUTC(to));
        }});
        
        
    }
}
