/*
 * 
 */
package ballantines.avionics.blackbox;

import ballantines.avionics.blackbox.log.FlightLogger;
import ballantines.avionics.blackbox.log.LogEvent;
import ballantines.avionics.blackbox.util.Log;
import ballantines.avionics.kacars.model.Flight;
import ballantines.avionics.kacars.model.PIREPRequest;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.Callback;

/**
 *
 */
public class PIREPFilingForm implements Initializable, PipeUpdateListener {
    
    public static Parent create(Services services) throws IOException {
        PIREPFilingForm controller = new PIREPFilingForm();
        controller.setServices(services);
        
        FXMLLoader loader = new FXMLLoader(ACARSLogPanel.class.getResource("/fxml/pirepfiling.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }
    
    private static Log L = Log.forClass(PIREPFilingForm.class);
    // PIPES
    private final Pipe<LogEvent> eventPipe = Pipe.newInstance("PIREPFilingForm.event", this);
    private final Pipe<Flight> flightPipe = Pipe.newInstance("PIREPFilingForm.flight", this);
    private final Pipe<FlightTrackingResult> resultPipe = Pipe.newInstance("PIREPFilingForm.flight", this);

    @FXML private TextField paxUI;
    @FXML private TextField flightTimeUI;
    @FXML private TextField registrationUI;
    @FXML private TextField cargoUI;
    @FXML private TextField flightNumberUI;
    @FXML private Label messageUI;
    @FXML private TextArea commentsUI;
    @FXML private TextField fuelUsedUI;
    @FXML private TextField arrIcaoUI;
    @FXML private TextField depIcaoUI;
    @FXML private TextField landingUI;
    @FXML private Button loadDataBtnUI;
    @FXML private Button filePirepBtnUI;
    @FXML private ListView<LogEvent> logUI;
    
    private Services services;
    private FlightLogger logger;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logUI.setCellFactory(new Callback<ListView<LogEvent>, ListCell<LogEvent>>() {
            @Override
            public ListCell<LogEvent> call(ListView<LogEvent> p) {
                return new LogEventCell();
            }
        });
        
        logger = new FlightLogger(services);   
        
        eventPipe.connectTo(logger.eventPipe);
        flightPipe.connectTo(logger.flightBidPipe);
        resultPipe.connectTo(services.flightTrackingResultPipe);
    }

    @Override
    public void pipeUpdated(Pipe model) {
        L.pipeUpdated(model);
            
        if (model == flightPipe) {
            Flight flight = flightPipe.get();
            setFlightData(flight);
        }
        else if (model == resultPipe) {
            FlightTrackingResult result = resultPipe.get();
            setFlightTrackingResultData(result);
        }
        else if (model == eventPipe) {
            LogEvent event = eventPipe.get();
            addLogEvent(event);
        }
    }
    
    

    public void setServices(Services services) {
        this.services = services;
    }
    
    
    @FXML
    void loadDataBtnPressed(ActionEvent event) {
        Flight flight = flightPipe.get();
        FlightTrackingResult result = resultPipe.get();
        List<LogEvent> events = logger.getEvents();
        
        clearForm();
        if (flight!=null) {
            setFlightData(flight);
        }
        if (result!=null) {
            setFlightTrackingResultData(result);
        }
        if (events!=null) {
            setLogEvents(events);
        }
    }

    @FXML
    void filePirepBtnPressed(ActionEvent event) {
        PIREPRequest request = new PIREPRequest();
        request.flightNumber = flightNumberUI.getText();
        request.registration = registrationUI.getText();
        request.depICAO = depIcaoUI.getText();
        request.arrICAO = arrIcaoUI.getText();
        request.pax = parseInt(paxUI);
        request.cargo = parseInt(cargoUI);
        request.fuelUsed = parseInt(fuelUsedUI);
        request.flightTime = flightTimeUI.getText();
        request.comments = commentsUI.getText();
        
        StringBuilder logBuilder = new StringBuilder();
        for (LogEvent e : logUI.getItems()) {
            logBuilder.append(e.getFormattedMessage());
            logBuilder.append("<br />");
        }
        request.log = logBuilder.toString();
        
        setMessage(Color.BLACK, "Submitting PIREP");
        
        try {
            L.info("Submit PIREP request: %s", request);
            boolean success = services.getKacarsClient().filePIREP(request);
            if (success) {
                setMessage(Color.GREEN, "PIREP was filed successfully.");
            } else {
                setMessage(Color.RED, "Filing PIREP failed.");
            }
        } catch (Exception ex) {
            L.error(ex, "Error while filing PIREP report: %s", ex.getMessage());
            setMessage(Color.RED, "Error: " + ex.getMessage());
        }
        
    }
    
    private void setFlightData(final Flight flight) {
        Platform.runLater(new Runnable() { public void run() {
            flightNumberUI.setText(flight.flightNumber);
            registrationUI.setText(flight.aircraftReg);
            depIcaoUI.setText(flight.depICAO);
            arrIcaoUI.setText(flight.arrICAO);
            paxUI.setText("" + flight.aircraftMaxPax);
            cargoUI.setText("" + flight.aircraftCargo);
        }});
    }
    
    private void setFlightTrackingResultData(final FlightTrackingResult result) {
        Platform.runLater(new Runnable() { public void run() {
            int[] hhmm = result.getFlightTimeHoursAndMinutes();
            fuelUsedUI.setText("" + result.fuelConsumption);
            flightTimeUI.setText(String.format("%d.%02d", hhmm[0], hhmm[1]));
            landingUI.setText("" + result.landingRateFPM);
            filePirepBtnUI.setDisable(false);
        }});
    }
    
    private void setLogEvents(final List<LogEvent> events) {
        Platform.runLater(new Runnable() { public void run() {
            ObservableList<LogEvent> items = logUI.getItems();
            items.clear();
            items.addAll(events);
        }});
    }
    
    private void addLogEvent(final LogEvent event) {
        Platform.runLater(new Runnable() { public void run() {
            ObservableList<LogEvent> items = logUI.getItems();
            if (event.getType()==LogEvent.Type.FIRST_MESSAGE) {
                items.clear();
            }
            items.add(event);
        }});
    }
    
    private int parseInt(TextField field) {
        String value = field.getText();
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            L.debug("Failed to parse %s to integer: %s", value, ex);
            return 0;
        }
    }
    
    private void setMessage(final Color color, final String msg) {
        Platform.runLater(new Runnable() { public void run() {
            messageUI.setTextFill(color);
            messageUI.setText(msg); 
        }});
    }

    private void clearForm() {
        Platform.runLater(new Runnable() { public void run() {
            flightNumberUI.setText("");
            registrationUI.setText("");
            depIcaoUI.setText("");
            arrIcaoUI.setText("");
            paxUI.setText("");
            cargoUI.setText("");
            fuelUsedUI.setText("");
            flightTimeUI.setText("");
            landingUI.setText("");
            logUI.getItems().clear();
            commentsUI.setText("");
            filePirepBtnUI.setDisable(true);
            loadDataBtnUI.setDisable(false);
        }});
    }
    
    private static class LogEventCell extends ListCell<LogEvent> {
        
        @Override
        protected void updateItem(LogEvent event, boolean empty) {
            super.updateItem(event, empty);
            setText(event==null ? "" : event.getFormattedMessage());
        }
        
    }
}
