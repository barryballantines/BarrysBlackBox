/*
 * 
 */
package ballantines.avionics.blackbox.panel;

import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.model.FlightTrackingResult;
import ballantines.avionics.blackbox.log.FlightLogger;
import ballantines.avionics.blackbox.log.LogEvent;
import ballantines.avionics.blackbox.model.Command;
import ballantines.avionics.blackbox.util.Log;
import ballantines.avionics.kacars.KAcarsClient;
import ballantines.avionics.kacars.model.Flight;
import ballantines.avionics.kacars.model.PIREPRequest;
import ballantines.javafx.FxDialogs;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import de.mbuse.pipes.Pipes;
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
        
        FXMLLoader loader = new FXMLLoader(PIREPFilingForm.class.getResource("/fxml/pirepfiling.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }
    
    private static Log L = Log.forClass(PIREPFilingForm.class);
    // PIPES
    private final Pipe<LogEvent> eventPipe = Pipe.newInstance("PIREPFilingForm.event", this);
    private final Pipe<Flight> flightPipe = Pipe.newInstance("PIREPFilingForm.flight", this);
    private final Pipe<Command> commandPipe = Pipe.newInstance("PIREPFilingForm.command", this);
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
        flightPipe.connectTo(services.flightBidPipe);
        resultPipe.connectTo(services.flightTrackingResultPipe);
        
        Pipes.connect(services.commandPipe, commandPipe);
        
        logger.restoreLogEvents();
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
        else if (model == commandPipe) {
            if (commandPipe.get() == Command.UPLOAD_PIREP) {
                filePirep();
            }
            else if (commandPipe.get() == Command.START_RECORDING) {
                refreshPirepData();
            }
            else if (commandPipe.get() == Command.DOWNLOAD_BID) {
                downloadFlightBid();
            }
        }
    }
    
    

    public void setServices(Services services) {
        this.services = services;
    }
    
    
    @FXML
    void loadDataBtnPressed(ActionEvent event) {
        refreshPirepData();
    }
    
    private void downloadFlightBid() {
        KAcarsClient client = services.getKacarsClient();
        if (client.isEnabled()) {
            String response = FxDialogs.create()
                .title("Downloading Flight Bid...")
                .masthead("Downloading current flight bid")
                .message("Downloading the current flight bid will override the current flight data. Are you sure?")
                .actions(FxDialogs.YES, FxDialogs.NO)
                .showConfirm();
            if (response == FxDialogs.YES) {
                try {
                    Flight f = client.getBid();
                    services.flightBidPipe.set(f==null ? new Flight() : f);
                    
                } catch (Exception ex) {
                    FxDialogs.create()
                        .title("Error downloading flight bid")
                        .masthead("Flight bid download failed: " + ex.getMessage())
                        .message("The kACARS client reports an error. \n"
                        + "Please check your configuration and your Virtual Airlines account and try again.")
                        .showException(ex);
                }
            }
        }
    }
    
    private void refreshPirepData() {
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
        services.fireCommand(Command.UPLOAD_PIREP);
    }
    
    private void filePirep() {
        String response = FxDialogs.create()
                .title("Upload PIREP...")
                .masthead("Upload PIREP")
                .message("Are you sure you want to upload your PIREP now?")
                .actions(FxDialogs.YES, FxDialogs.NO)
                .showConfirm();
        if (response == FxDialogs.YES) {
            PIREPRequest request = new PIREPRequest();
            request.flightNumber = flightNumberUI.getText();
            request.registration = registrationUI.getText();
            request.depICAO = depIcaoUI.getText();
            request.arrICAO = arrIcaoUI.getText();
            request.pax = parseInt(paxUI);
            request.cargo = parseInt(cargoUI);
            request.fuelUsed = parseInt(fuelUsedUI);
            request.flightTime = flightTimeUI.getText();
            request.landing = parseInt(landingUI);
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
                    FxDialogs.create()
                            .title("PIREP uploaded")
                            .masthead("PIREP Uploaded")
                            .message("Your PIREP has been filed sucessfully.")
                            .showInformation();
                } else {
                    setMessage(Color.RED, "Filing PIREP failed.");
                    FxDialogs.create()
                            .title("PIREP failed")
                            .masthead("PIREP failed")
                            .message("Your PIREP was rejected by the virtual airline.")
                            .showInformation();
                }
            } catch (Exception ex) {
                L.error(ex, "Error while filing PIREP report: %s", ex.getMessage());
                setMessage(Color.RED, "Error: " + ex.getMessage());
                FxDialogs.create()
                        .title("Error uploading PIREP")
                        .masthead("PIREP upload failed:" + ex.getMessage())
                        .message("The PIREP client reports an error. \n"
                        + "Please check your configuration and your Virtual Airlines account and try again.")
                        .showException(ex);


            }
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
    
    @FXML
    private void updateFlightData() {
        Flight flight = flightPipe.get();
        flight = (flight==null) ? new Flight() : (Flight) flight.clone();
        
        flight.flightNumber = flightNumberUI.getText().trim();
        flight.aircraftReg = registrationUI.getText().trim();
        flight.depICAO = depIcaoUI.getText().trim();
        flight.arrICAO = arrIcaoUI.getText().trim();
        flight.aircraftMaxPax = parseInt(paxUI, flight.aircraftMaxPax);
        flight.aircraftCargo = parseInt(cargoUI, flight.aircraftCargo);
        
        services.flightBidPipe.set(flight);
    }
    
    private void setFlightTrackingResultData(final FlightTrackingResult result) {
        Platform.runLater(new Runnable() { public void run() {
            if (result==null) {
                fuelUsedUI.setText("");
                flightTimeUI.setText("");
                landingUI.setText("");
            }
            else {
                int[] hhmm = result.getFlightTimeHoursAndMinutes();
                fuelUsedUI.setText("" + result.fuelConsumption);
                flightTimeUI.setText(String.format("%d.%02d", hhmm[0], hhmm[1]));
                landingUI.setText("" + result.landingRateFPM);
                filePirepBtnUI.setDisable(false);
            }
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
        return parseInt(field, 0);
    }
    
    private int parseInt(TextField field, int defaultValue) {
        String value = field.getText();
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            L.debug("Failed to parse %s to integer: %s", value, ex);
            return defaultValue;
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
