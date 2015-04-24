package de.mbuse.flightgear.pireprecorder;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class PIREPForm implements Initializable {

    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final NumberFormat TWO_DIGITS_FORMAT = new DecimalFormat("00");
    private static final NumberFormat FUEL_FORMAT = DecimalFormat.getIntegerInstance();
    
    public static Parent create(FlightDataRetrieval retrieval) throws IOException {
        PIREPForm controller = new PIREPForm();
        controller.setFlightDataRetrieval(retrieval);
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
    @FXML private Button startupBtn;
    @FXML private Button shutdownBtn;
    
    private FlightDataRetrieval retrieval;
    private Calendar departureTime;
    private Calendar arrivalTime;
    private long departureFuel;
    private long arrivalFuel;

    public void setFlightDataRetrieval(FlightDataRetrieval flightDataRetrieval) {
        this.retrieval = flightDataRetrieval;
    }
    
    @FXML
    private void startupBtnPressed(ActionEvent event) {
        System.out.println("Startup button pressed");
        
        String airport = retrieval.getAirport();
        departureTime = retrieval.getTimeUTC();
        departureFuel = retrieval.getFuel();
        
        departureAirportLbl.setText(airport);
        departureTimeLbl.setText(TIME_FORMAT.format(departureTime.getTime()));
        departureFuelLbl.setText(FUEL_FORMAT.format(departureFuel));
        
        arrivalAirportLbl.setText("----");
        arrivalTimeLbl.setText("--:--");
        arrivalFuelLbl.setText("----");
        
        fuelConsumptionLbl.setText("----");
        flightTimeLbl.setText("--:--");
        
        startupBtn.setDisable(true);
        shutdownBtn.setDisable(false);
               
    }
    
    @FXML 
    private void shutdownBtnPressed(ActionEvent event) {
        System.out.println("Shutdown button pressed");
        arrivalTime = retrieval.getTimeUTC();
        arrivalFuel = retrieval.getFuel();
       
        arrivalAirportLbl.setText(retrieval.getAirport());
        arrivalTimeLbl.setText(TIME_FORMAT.format(arrivalTime.getTime()));
        arrivalFuelLbl.setText(FUEL_FORMAT.format(arrivalFuel));
        
            
        long fuelConsumption = departureFuel - arrivalFuel;

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
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        shutdownBtn.setDisable(true);
    }    
}
