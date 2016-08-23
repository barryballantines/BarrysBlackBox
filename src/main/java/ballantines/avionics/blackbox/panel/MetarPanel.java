package ballantines.avionics.blackbox.panel;

import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.udp.FlightData;
import ballantines.avionics.blackbox.util.Log;
import ballantines.avionics.kacars.model.Flight;
import ballantines.javafx.FxDialogs;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class MetarPanel implements PipeUpdateListener, Initializable {

    private static Log L = Log.forClass(MetarPanel.class);
    private static final String FG_METAR_PROPERTY = "/environment/metar/data";
    private static final String METAR_URL_PATTERN = "http://www.aviationweather.gov/metar/data?ids=%s&format=decoded&date=0&hours=0&taf=on&layout=off";
    
    @FXML private Button nearestBtn;
    @FXML private Button arrivalBtn;
    @FXML private Button sendToFGBtn;
    @FXML private TextField icaoTF;
    @FXML private WebView metarBrowser;
    @FXML private Button departureBtn;

    private final Pipe<String> icaoPipe = Pipe.newInstance("metarPanel.icao", this);
    private final Pipe<String> metarPipe = Pipe.newInstance("metarPanel.metar", this);
    
    private Services services;

    public static Parent create(Services services) throws IOException {
        MetarPanel controller = new MetarPanel();
        controller.setServices(services);
        FXMLLoader loader = new FXMLLoader(PIREPForm.class.getResource("/fxml/metar.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateIcaoTF(null);
        updateMetarBrowser(null);
        
        /** fix for 'www.aviationweather.gov':
         * we are removing the head element to get rid of the stylesheets,
         * so the layout will be more 'responsive'...
         **/
        metarBrowser.getEngine().documentProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> observable, Document oldDocument, Document newDocument) {
                if (newDocument!=null) {
                    NodeList heads = newDocument.getElementsByTagName("head");
                    if (heads.getLength()>0) {
                        Element head = (Element) heads.item(0);
                        Element html = (Element) head.getParentNode();
                        html.removeChild(head);
                    }
                }
                
                String metar = extractMetarFromDocument(newDocument);
                metarPipe.set(metar);
            }
        });
    }
    
    private String extractMetarFromDocument(Document document) {
        if (document == null) {
            return null;
        }
        else {
            NodeList tables = document.getElementsByTagName("table");
            
            if (tables.getLength()==0) {
                return null;
            }
            
            Element metarTable = (Element) tables.item(0);
            NodeList rows = metarTable.getElementsByTagName("tr");
            
            for (int i=0; i< rows.getLength(); i++) {
                Element row = (Element) rows.item(i);
                NodeList cells = row.getElementsByTagName("td");
                if (cells.getLength()==2) {
                    String label = cells.item(0).getTextContent().trim();
                    if ("Text:".equals(label)) {
                        String metar = cells.item(1).getTextContent();
                        return metar.trim();
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public void pipeUpdated(Pipe pipe) {
        L.pipeUpdated(pipe);
        if (icaoPipe == pipe) {
            String icao = trimIcao(icaoPipe.get());
            updateIcaoTF(icao);
            updateMetarBrowser(icao);           
        }
        else if (metarPipe == pipe) {
            String metar = metarPipe.get();
            sendToFGBtn.setDisable(metar==null);
        }
    }
    
    private void updateIcaoTF(String icao) {
        if (icao!=null) {
            icaoTF.setText(icao);
        }
        else {
            icaoTF.setText("");
        }
    }
    
    private void updateMetarBrowser(String icao) {
        if (icao!=null) {
            metarBrowser.getEngine().load(String.format(METAR_URL_PATTERN, icao));
        } else {
            metarBrowser.getEngine().loadContent("<p>No station selected.</p>");
        }
    }
    
    private String trimIcao(String icao) {
        if (icao==null) {
            return null;
        }
        icao = icao.trim();
        icao = icao.toUpperCase();
        if (icao.length()==0) {
            return null;
        }
        return icao;
    }
    
    @FXML
    void handleDepartureBtnAction(ActionEvent event) {
        String departure = null;
        Flight flight = services.flightBidPipe.get();
        if (flight == null) {
            FlightData data = services.flightDataPipe.get();
            if (data!=null) {
                departure = trimIcao(data.getDeparture());
            }
        }
        else {
            departure = trimIcao(flight.depICAO);
        }
        icaoPipe.set(null); // force reload...
        if (departure!=null) {
            icaoPipe.set(departure);
        }
    }

    @FXML
    void handleArrivalBtnAction(ActionEvent event) {
        String arrival = null;
        Flight flight = services.flightBidPipe.get();
        if (flight == null) {
            FlightData data = services.flightDataPipe.get();
            if (data!=null) {
                arrival = trimIcao(data.getDestination());
            }
        }
        else {
            arrival = trimIcao(flight.arrICAO);
        }
        
        icaoPipe.set(null); // force reload...
        if (arrival!=null) {
            icaoPipe.set(arrival);
        }
    }

    @FXML
    void handleNearestBtnAction(ActionEvent event) {
        String icao = null;
        FlightData data = services.flightDataPipe.get();
        if (data!=null) {
            icao = trimIcao(data.getClosestAirport());
        }
        
        icaoPipe.set(null); // force reload...
        if (icao != null) {
            icaoPipe.set(icao);
        }
    }

    @FXML
    void handleIcaoTFAction(ActionEvent event) {
        String icao = trimIcao(icaoTF.getText());
        icaoPipe.set(null); // force reload...
        if (icao != null) {
            icaoPipe.set(icao);
        }
    }
    
    @FXML
    void handleSendToFlightGearAction(ActionEvent event) {
        String metar = metarPipe.get();
        try {
            services.getPropertyService().writeProperty(FG_METAR_PROPERTY, metar);
            FxDialogs.showInformation(
                    "Send Weather to FG", 
                    "The METAR string was successfully send to FlightGear.", 
                    "METAR: " + metar);
            L.info("METAR was successfully send to FlightGear: '%s'", metar);
        } catch (Exception ex) {
            L.error(ex, "Failed to send METAR information to FlightGear: METAR: '%s'", metar);
            FxDialogs.create()
                    .title("Error sending weather to FlightGear")
                    .masthead("Failed to send METAR information to FlightGear.")
                    .message("The following error occured: '" + ex.getMessage() + "'.")
                    .showException(ex);
        }
    }

    public void setServices(Services services) {
        this.services = services;
    }
    
    

}
