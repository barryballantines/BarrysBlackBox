package ballantines.avionics.blackbox.panel;

import ballantines.avionics.blackbox.service.BlockTimeChecker;
import ballantines.avionics.blackbox.service.FlightDataRetrieval;
import ballantines.avionics.blackbox.service.FuelChecker;
import ballantines.avionics.blackbox.service.LandingRateService;
import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.model.FlightTrackingResult;
import ballantines.avionics.blackbox.model.TrackingData;
import ballantines.avionics.blackbox.util.Log;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import de.mbuse.pipes.Pipes;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class PIREPForm implements Initializable, PipeUpdateListener<Object> {
    
    private static Log L = Log.forClass(PIREPForm.class);

    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final NumberFormat TWO_DIGITS_FORMAT = new DecimalFormat("00");
    private static final NumberFormat FUEL_FORMAT = DecimalFormat.getIntegerInstance();
    
    static {
        TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public static Parent create(Services services) throws IOException {
        PIREPForm controller = new PIREPForm();
        controller.setServices(services);
        FXMLLoader loader = new FXMLLoader(PIREPForm.class.getResource("/fxml/pirep.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }
    
    
    @FXML private Label departureAirportLbl;
    @FXML private Label departureTimeLbl;
    @FXML private Label departureFuelLbl;
    @FXML private Label arrivalAirportLbl;
    @FXML private Label arrivalTimeLbl;
    @FXML private Label arrivalFuelLbl;
    @FXML private Label fuelConsumptionLbl;
    @FXML private Label flightTimeLbl;
    @FXML private Label landingRateLbl;
    @FXML private Button startupBtn;
    @FXML private Button shutdownBtn;
    
    private Services services;
    //private Calendar departureTime;
    //private Calendar arrivalTime;
    //private double departureFuel;
    //private long arrivalFuel;
    
    private FuelChecker fuelChecker;
    private BlockTimeChecker blockTimeChecker;
    private LandingRateService landingRateService;
    
    private final Pipe<Boolean> isRecordingPipe = Pipe.newInstance("pirepForm.isRecording", this);
    private final Pipe<Double> landingRatePipe = Pipe.newInstance("pirepForm.landingRate", 0.0, this);
    
    private final Pipe<FlightTrackingResult> resultPipe = Pipe.newInstance("pirepForm.result", this);
    private final Pipe<TrackingData> trackingDataPipe = Pipe.newInstance("pirepForm.trackingData", this);

    public void setServices(Services services) {
        this.services = services;
    }
    
    /*
    public void setDepartureTimeGauge(Calendar cal) {
        departureTime = cal;
        departureTimeLbl.setText(TIME_FORMAT.format(departureTime.getTime()));
    }
    
    public void setDepartureFuelGauge(double fuel) {
        departureFuel = fuel;
        departureFuelLbl.setText(FUEL_FORMAT.format(departureFuel));
    }
    
    public double getDepartureFuelGauge() {
        return departureFuel;
    }
    
    public void setArrivalFuelGauge(long fuel) {
        arrivalFuel = fuel;
        arrivalFuelLbl.setText(FUEL_FORMAT.format(arrivalFuel));     
    }
    
    public long getArrivalFuelGauge() {
        return arrivalFuel;
    }
    */
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        shutdownBtn.setDisable(true);
        
        // TODO: FuelChecker and BlockTimeChecker should not use "this", but the trackingDataPipe!!!
        fuelChecker = new FuelChecker(this.services);
        blockTimeChecker = new BlockTimeChecker(this.services);
        landingRateService = new LandingRateService();
        
        Pipes.connect(isRecordingPipe, services.isRecordingPipe);
        // this is source of flight tracking results...
        services.flightTrackingResultPipe.connectTo(resultPipe);
        
        // the tracking data is initialized from user prefs in Services...
        Pipes.connect(services.trackingDataPipe, trackingDataPipe);
        
        L.trace("Initialized");
    }    
    
    @FXML
    private void startupBtnPressed(ActionEvent event) {
        L.info("Startup button pressed");
        
        FlightDataRetrieval retrieval = services.getFlightDataRetrieval();
        
        
        landingRateService.flightDataPipe.connectTo(services.flightDataPipe);
        this.landingRatePipe.connectTo(landingRateService.landingRate, Pipes.MIN_TRANSFORM);
        
        
        TrackingData trackingData = new TrackingData();
        trackingData.departureAirport = retrieval.getAirport();
        trackingData.departureTime = null;
        trackingData.departureFuel = 0;
        trackingData.trackingStarted = true;
        
        resultPipe.set(null);
        trackingDataPipe.set(trackingData);
        
        fuelChecker.connect();
        blockTimeChecker.connect();
        
        isRecordingPipe.set(true);
               
    }
    
    
    @FXML 
    private void shutdownBtnPressed(ActionEvent event) {
        L.info("Shutdown button pressed");
        
        // == DISCONNECT SERVICES ===
        
        fuelChecker.disconnect();
        blockTimeChecker.disconnect();
        
        landingRateService.flightDataPipe.disconnectFrom(services.flightDataPipe);
        landingRatePipe.disconnectFrom(landingRateService.landingRate);
        
        // === DATA ===
        FlightDataRetrieval retrieval = services.getFlightDataRetrieval();
        TrackingData trackingData = new TrackingData(trackingDataPipe.get());
        
        trackingData.trackingFinished = true;
        trackingData.arrivalAirport = retrieval.getAirport();
        trackingData.arrivalFuel = (int) retrieval.getFuel();
        trackingData.arrivalTime = retrieval.getTime();
        trackingData.landingRateFPM = (int) Math.round(landingRatePipe.get() * 60);
        
        trackingDataPipe.set(trackingData);
        
        FlightTrackingResult result = new FlightTrackingResult();
        result.flightTimeMinutes = trackingData.getFlightTimeInMinutes();
        result.fuelConsumption = trackingData.getFuelConsumption();
        result.landingRateFPM = trackingData.landingRateFPM;
        
        resultPipe.set(result);
        isRecordingPipe.set(false);
        
    }
    
    

    @Override
    public void pipeUpdated(Pipe pipe) {
        L.pipeUpdated(pipe);
        
        if (pipe == landingRatePipe) {
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    long landingRate = Math.round(landingRatePipe.get() * 60);
                    landingRateLbl.setText("" + landingRate);
                }
                
            });
        }
        
        if (pipe == trackingDataPipe) {
            TrackingData data = trackingDataPipe.get();
            updateUI(data);
        }
    }
    
    private void updateUI(TrackingData trackingData) {
        final TrackingData data = (trackingData==null)
                ? new TrackingData() // EMPTY
                : trackingData;
        Platform.runLater(new Runnable() { public void run() {
            if (data.trackingStarted) {
                departureAirportLbl.setText(data.departureAirport);
                departureTimeLbl.setText(format(data.departureTime));
                departureFuelLbl.setText(FUEL_FORMAT.format(data.departureFuel));
            } 
            else {
                departureAirportLbl.setText("----");
                departureTimeLbl.setText("--:--");
                departureFuelLbl.setText("----");
            }

            if (data.trackingFinished) {
                arrivalAirportLbl.setText(data.arrivalAirport);
                arrivalTimeLbl.setText(format(data.arrivalTime));
                arrivalFuelLbl.setText(FUEL_FORMAT.format(data.arrivalFuel));

                fuelConsumptionLbl.setText(FUEL_FORMAT.format(data.getFuelConsumption()));
                flightTimeLbl.setText(data.getFlightTimeFormatted());
                landingRateLbl.setText(""+data.landingRateFPM);
                
                startupBtn.setDisable(false);
                shutdownBtn.setDisable(true);
            }
            else {
                arrivalAirportLbl.setText("----");
                arrivalTimeLbl.setText("--:--");
                arrivalFuelLbl.setText("----");

                fuelConsumptionLbl.setText("----");
                flightTimeLbl.setText("--:--");
                landingRateLbl.setText("----");
                
                startupBtn.setDisable(true);
                shutdownBtn.setDisable(false);
            }
        }});
    }
    
    private String format(Calendar date) {
        return date==null ? "--:--" : TIME_FORMAT.format(date.getTime());
    }
    
}
