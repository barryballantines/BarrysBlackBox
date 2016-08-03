package ballantines.avionics.blackbox.panel;

import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.util.Log;
import ballantines.avionics.kacars.model.Flight;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLInputElement;


public class RouteFinderPanel implements PipeUpdateListener, Initializable {

    private static Log L = Log.forClass(RouteFinderPanel.class);
    private static final String ROUTE_FINDER_URL = "http://rfinder.asalink.net/free/";
    
    @FXML private WebView browser;
    @FXML private Button  resetBtn;
    @FXML private Button  downloadBtn;
    
    private Services services;
    private Pipe<Flight> flightBidPipe = Pipe.newInstance("routeFinderPanel.flightBid", this);
    private Pipe<String> detailedRouteInfoPipe = Pipe.newInstance("routeFinderPanel.detailedRouteInfo", this);
    
    private Map<String, HTMLInputElement> inputFields = Collections.emptyMap();

    public static Parent create(Services services) throws IOException {
        RouteFinderPanel controller = new RouteFinderPanel();
        controller.setServices(services);
        FXMLLoader loader = new FXMLLoader(PIREPForm.class.getResource("/fxml/routefinder.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        flightBidPipe.connectTo(services.flightBidPipe);
        reloadBrowser();
        
        /** 
         **/
        browser.getEngine().documentProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> observable, Document oldDocument, Document newDocument) {
                
                HashMap<String, HTMLInputElement> newInputFields = new HashMap<>();
                if (newDocument!=null) {
                    NodeList inputs = newDocument.getElementsByTagName("input");
                    for (int i=0; i<inputs.getLength(); i++) {
                        HTMLInputElement  input = (HTMLInputElement) inputs.item(i);
                        newInputFields.put(input.getName(), input);
                    }
                    
                    inputFields = newInputFields;
                    
                    NodeList pres = newDocument.getElementsByTagName("pre");
                    if (pres.getLength()>0) {
                        String route = pres.item(0).getTextContent();
                        detailedRouteInfoPipe.set(route);
                    }
                    else {
                        detailedRouteInfoPipe.set(null);
                    }
                }
                fillFormFields();
                
            }
        });
    }
    
    
    
    @Override
    public void pipeUpdated(Pipe pipe) {
        L.pipeUpdated(pipe);
        if (flightBidPipe == pipe) {
            fillFormFields();
        }
        else if (detailedRouteInfoPipe == pipe) {
            if (detailedRouteInfoPipe.get()==null) {
                downloadBtn.setDisable(true);
            }
            else {
                downloadBtn.setDisable(false);
            }
        }
    }
    
    private void fillFormFields() {
        Flight flight = flightBidPipe.get();
        if (flight==null) return;
        HTMLInputElement from = inputFields.get("id1");
        HTMLInputElement to = inputFields.get("id2");
        if (from != null) {
            from.setValue(flight.depICAO);
        }
        if (to != null) {
            to.setValue(flight.arrICAO);
        }
    }
    
    private void reloadBrowser() {
        browser.getEngine().load(ROUTE_FINDER_URL);
    }

    public void setServices(Services services) {
        this.services = services;
    }
    
    

}
