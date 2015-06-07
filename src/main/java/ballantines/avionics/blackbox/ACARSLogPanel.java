/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ballantines.avionics.blackbox;

import ballantines.avionics.blackbox.log.FlightLogger;
import ballantines.avionics.blackbox.log.LogEvent;
import ballantines.avionics.kacars.model.Flight;
import ballantines.avionics.kacars.model.PIREPRequest;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;

/**
 *
 * @author mbuse
 */
public class ACARSLogPanel implements Initializable, PipeUpdateListener {

    
    public static Parent create(Services services) throws IOException {
        ACARSLogPanel controller = new ACARSLogPanel();
        controller.setServices(services);
        
        FXMLLoader loader = new FXMLLoader(ACARSLogPanel.class.getResource("/fxml/acarsLog.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger = new FlightLogger(services);   
        
        eventPipe.connectTo(logger.eventPipe);
        flightPipe.connectTo(logger.flightBidPipe);
        resultPipe.connectTo(services.flightTrackingResultPipe);
    }

    
    @Override
    public void pipeUpdated(Pipe pipe) {
        if (pipe == eventPipe) {
            logField.setText("");
            for (LogEvent event : logger.getEvents()) {
                logField.appendText(event.getFormattedMessage() + "\n");
            }
        }
        else if (pipe == resultPipe) {
            FlightTrackingResult result = resultPipe.get();
            if (result==null) {
                filePirepBtn.disableProperty().set(true);
                setMessage(Color.BLACK, "Finish your flight before submissing");
            }
            else {
                filePirepBtn.disableProperty().set(false);
                setMessage(Color.BLACK, "Press button to submit PIREP request");
            }
        }
        
    }

    public void filePirep(ActionEvent evt) {
        try {
            Flight flight = flightPipe.get();
            if (flight==null) {
                // try...
                flight = services.getKacarsClient().getBid();
            }

            if (flight==null) {
                setMessage(Color.ORANGE, "No matching Flight Bid found");  
            } else {
                PIREPRequest pirep = new PIREPRequest(flight);
                FlightTrackingResult result = resultPipe.get();
                pirep.fuelUsed = (int) result.fuelConsumption;
                pirep.landing = result.landingRateFPM;
                int[] hhmm = result.getFlightTimeHoursAndMinutes();
                pirep.flightTime = String.format("%d.%02d", hhmm[0], hhmm[1]);
                StringWriter logWriter = new StringWriter();
                StringBuilder logBuilder = new StringBuilder();
                for (LogEvent e : logger.getEvents()) {
                    logBuilder.append(e.getFormattedMessage());
                    logBuilder.append("<br />");
                }
                pirep.log = logBuilder.toString();
                pirep.comments = "";
                
                setMessage(Color.BLACK, "Submitting...");
                
                boolean success = services.getKacarsClient().filePIREP(pirep);
                
                if (success) {
                    setMessage(Color.GREEN, "PIREP successfully submitted");
                }
                else {
                    setMessage(Color.ORANGE, "PIREP failed.");
                }
            }
        } catch (Exception ex) {
            setMessage(Color.RED, "Error: " + ex.getMessage());
            System.err.println("An error occured" + ex);
            ex.printStackTrace();
        }
    }
    
    
    
    public void setServices(Services services) {
        this.services = services;
    }

    public Services getServices() {
        return services;
    }
    
    private void appendEvent(final LogEvent event) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                logField.appendText(event.getFormattedMessage() + "\n");
            }
        });
    }
    
    private void setMessage(final Color color, final String msg) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {acarsMessageLbl.setTextFill(color);
            acarsMessageLbl.setText(msg); 
            }
        });
    }
    
    // PIPES
    private final Pipe<LogEvent> eventPipe = Pipe.newInstance("ACARSLogPanel.event", this);
    private final Pipe<Flight> flightPipe = Pipe.newInstance("ACARSLogPanel.flight", this);
    private final Pipe<FlightTrackingResult> resultPipe = Pipe.newInstance("ACARSLogPanel.flight", this);
    
    // COMPONENTS
    @FXML private TextArea logField;
    @FXML private Button filePirepBtn;
    @FXML private Label acarsMessageLbl;
    
    // SERVICES
    private FlightLogger logger;
    private Services services;
}
