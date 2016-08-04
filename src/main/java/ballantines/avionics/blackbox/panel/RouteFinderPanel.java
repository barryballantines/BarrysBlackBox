package ballantines.avionics.blackbox.panel;

import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.util.Log;
import ballantines.avionics.kacars.model.Flight;
import ballantines.avionics.blackbox.model.Waypoint;
import ballantines.avionics.blackbox.service.RouteFinderService;
import ballantines.avionics.blackbox.service.RouteFinderService.RouteFinderForm;
import ballantines.javafx.FxDialogs;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.w3c.dom.Document;

public class RouteFinderPanel implements PipeUpdateListener, Initializable {

    private static Log L = Log.forClass(RouteFinderPanel.class);
    
    @FXML private WebView browser;
    @FXML private Button  resetBtn;
    @FXML private Button  downloadBtn;
    
    private Services services;
    private RouteFinderService routeFinderService = new RouteFinderService();
    
    private Pipe<Flight> flightBidPipe = Pipe.newInstance("routeFinderPanel.flightBid", this);
    private Pipe<RouteFinderForm> routeFinderFormPipe = Pipe.newInstance("routeFinderPanel.routeFinderForm", this);
    private Pipe<List<Waypoint>> detailedRouteInfoPipe = Pipe.newInstance("routeFinderPanel.detailedRouteInfo", this);

    public static Parent create(Services services) throws IOException {
        RouteFinderPanel controller = new RouteFinderPanel();
        controller.setServices(services);
        FXMLLoader loader = new FXMLLoader(PIREPForm.class.getResource("/fxml/routefinder.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }

    public void setServices(Services services) {
        this.services = services;
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
                if (newDocument!=null) {
                    RouteFinderForm form = routeFinderService.extractRouteFinderForm(newDocument);
                    List<Waypoint> routeInfo = routeFinderService.extractDetailedRouteInformation(newDocument);
                    routeFinderFormPipe.set(form);
                    detailedRouteInfoPipe.set(routeInfo);
                }
            }
        });
    }
    
    
    
    @Override
    public void pipeUpdated(Pipe pipe) {
        L.pipeUpdated(pipe);
        if (flightBidPipe == pipe) {
            fillFormFields();
        }
        else if (routeFinderFormPipe == pipe) {
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
    
    @FXML
    public void handleResetAction(ActionEvent event) {
        reloadBrowser();
    }
    
    @FXML 
    public void handleDownloadAction(ActionEvent event) {
        List<Waypoint> route = detailedRouteInfoPipe.get();
        if (route!=null && route.size()>2) {
            storeRouteXML(route);
        }
    }
    
    private void storeRouteXML(List<Waypoint> route) {
        Waypoint departure = route.get(0);
        Waypoint destination = route.get(route.size()-1);
        String fileName = departure.ident + "-" + destination.ident + ".xml";
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Route");
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(null);
        if (file!=null) {
            try {
                routeFinderService.saveRouteToFile(route, file);
                L.info("Route saved to %s", file.getAbsolutePath());
                FxDialogs.showInformation("Route saved.", 
                        "Route file saved successfully", 
                        "The route is saved to \n" + file.getAbsolutePath() + "\n" 
                        + "You can use this file with the FlightGear Route Manager.");
            } catch (IOException ex) {
                FxDialogs.create()
                    .title("Error saving route to file")
                    .masthead("Failed to save '" + file.getName() + "': " + ex.getMessage())
                    .message("Barry's BlackBox failed to save the current route information to the following file: \n"
                        + file.getAbsolutePath() + "\n")
                    .showException(ex);
            }
        }
    }
    
    private void fillFormFields() {
        RouteFinderForm form = routeFinderFormPipe.get();
        if (form==null) {
            return;
        }
        
        Flight flight = flightBidPipe.get();
        if (flight!=null) {
            form.setDeparture(flight.depICAO);
            form.setDestination(flight.arrICAO);
        }
    }
    
    private void reloadBrowser() {
        browser.getEngine().load(routeFinderService.getRouteFinderURL());
    }
    
}
