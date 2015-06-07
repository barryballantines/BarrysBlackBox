package ballantines.avionics.blackbox;

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
    private Calendar departureTime;
    private Calendar arrivalTime;
    private double departureFuel;
    private long arrivalFuel;
    
    private FuelChecker fuelChecker;
    private BlockTimeChecker blockTimeChecker;
    private LandingRateService landingRateService;
    
    private final Pipe<Boolean> isRecordingPipe = Pipe.newInstance("pirepForm.isRecording", this);
    private final Pipe<Double> landingRatePipe = Pipe.newInstance("pirepForm.landingRate", 0.0, this);
    
    private final Pipe<FlightTrackingResult> resultPipe = Pipe.newInstance("pirepForm.result", this);

    public void setServices(Services services) {
        this.services = services;
    }
    
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
    
    @FXML
    private void startupBtnPressed(ActionEvent event) {
        System.out.println("[PIREP FORM] Startup button pressed");
        
        FlightDataRetrieval retrieval = services.getFlightDataRetrieval();
        
        String airport = retrieval.getAirport();
        
        departureAirportLbl.setText(airport);
        
        setDepartureTimeGauge(retrieval.getTime());
        setDepartureFuelGauge(retrieval.getFuel());
        
        arrivalAirportLbl.setText("----");
        arrivalTimeLbl.setText("--:--");
        arrivalFuelLbl.setText("----");
        
        fuelConsumptionLbl.setText("----");
        flightTimeLbl.setText("--:--");
        landingRateLbl.setText("----");
        
        startupBtn.setDisable(true);
        shutdownBtn.setDisable(false);
        
        fuelChecker = new FuelChecker(this);
        blockTimeChecker = new BlockTimeChecker(this);
        landingRateService = new LandingRateService();
        
        services.flightDataPipe.addListener(fuelChecker);
        services.flightDataPipe.addListener(blockTimeChecker);
        
        landingRateService.flightDataPipe.connectTo(services.flightDataPipe);
        this.landingRatePipe.connectTo(landingRateService.landingRate, Pipes.MIN_TRANSFORM);
        
        isRecordingPipe.set(true);
               
    }
    
    
    @FXML 
    private void shutdownBtnPressed(ActionEvent event) {
        System.out.println("Shutdown button pressed");
        
        services.flightDataPipe.removeChangeListener(fuelChecker);
        services.flightDataPipe.removeChangeListener(blockTimeChecker);
        
        landingRateService.flightDataPipe.disconnectFrom(services.flightDataPipe);
        landingRatePipe.disconnectFrom(landingRateService.landingRate);
        
        FlightDataRetrieval retrieval = services.getFlightDataRetrieval();
        
        arrivalTime = retrieval.getTime();
        setArrivalFuelGauge(retrieval.getFuel());
       
        arrivalAirportLbl.setText(retrieval.getAirport());
        arrivalTimeLbl.setText(TIME_FORMAT.format(arrivalTime.getTime()));
        
            
        double fuelConsumption = getDepartureFuelGauge() - getArrivalFuelGauge();
        long diff = arrivalTime.getTimeInMillis() - departureTime.getTimeInMillis();

        long hours = (long) (diff / (60 * 60 * 1000));
        diff = diff - hours * (60 * 60 * 1000);
        long minutes = (long) (diff / (60 * 1000));
        
        String duration = TWO_DIGITS_FORMAT.format(hours)
                + ":" + TWO_DIGITS_FORMAT.format(minutes);

        fuelConsumptionLbl.setText(FUEL_FORMAT.format(fuelConsumption));
        flightTimeLbl.setText(duration);
        
        shutdownBtn.setDisable(true); 
        startupBtn.setDisable(false);
        
        FlightTrackingResult result = new FlightTrackingResult();
        result.flightTimeMinutes = (int) (diff /60000);
        result.fuelConsumption = (long) fuelConsumption;
        result.landingRateFPM = (int) Math.round(landingRatePipe.get() * 60);
        
        resultPipe.set(result);
        
        isRecordingPipe.set(false);
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        shutdownBtn.setDisable(true);
        
        Pipes.connect(isRecordingPipe, services.isRecordingPipe);
        Pipes.connect(resultPipe, services.flightTrackingResultPipe);
        
    }    

    @Override
    public void pipeUpdated(Pipe pipe) {
        System.out.println("[PIREP FORM] Model updated : " + pipe.id() + " -> " + pipe.get());
        
        if (pipe == landingRatePipe) {
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    long landingRate = Math.round(landingRatePipe.get() * 60);
                    landingRateLbl.setText("" + landingRate);
                }
                
            });
        }
    }
    
    
}
