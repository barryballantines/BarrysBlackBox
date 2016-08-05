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
import java.util.Objects;
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
    @FXML private Button  sendToFGBtn;
    
    private Services services;
    private RouteFinderService routeFinderService = new RouteFinderService();
    
    private Pipe<Flight> flightBidPipe = Pipe.newInstance("routeFinderPanel.flightBid", this);
    private Pipe<String[]> formDataPipe = Pipe.newInstance("routeFinderPanel.formData", new String[]{"",""}, this);
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
        routeFinderService.setPropertyService(services.getPropertyService());
        flightBidPipe.connectTo(services.flightBidPipe);
        reloadBrowser();
        
        /** ^
         **/
        browser.getEngine().documentProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> observable, Document oldDocument, Document newDocument) {
                if (newDocument!=null) {
                    browser.setDisable(false);
                    RouteFinderForm form = routeFinderService.extractRouteFinderForm(newDocument);
                    List<Waypoint> routeInfo = routeFinderService.extractDetailedRouteInformation(newDocument);
                    if (form!=null) {
                        // We are showing the form page
                        L.debug("RouteFinderPanel showing form: %s", form);
                    }
                    else if (routeInfo!=null) {
                        L.debug("RouteFinderPanel showing result: route of %d waypoints: %s", routeInfo.size(), routeInfo);
                    }
                    else {
                        // We are showing the stale page ("Do many requests at once...")
                        L.debug("RouteFinderPanel showing stale page...");
                        
                    }
                    routeFinderFormPipe.set(form);
                    detailedRouteInfoPipe.set(routeInfo);
                }
                else {
                    // loading next page...
                    browser.setDisable(true);
                    RouteFinderForm form = routeFinderFormPipe.get();
                    if (form!=null) {
                        // Submitting form request...
                        L.debug("Submitting %s", form);
                        formDataPipe.set(new String[] {
                            Objects.toString(form.getDeparture(), ""),
                            Objects.toString(form.getDestination(), "")
                        });
                    }
                }
            }
        });
    }
    
    @Override
    public void pipeUpdated(Pipe pipe) {
        L.pipeUpdated(pipe);
        if (flightBidPipe == pipe) {
            setFormData(flightBidPipe.get());
        }
        else if (formDataPipe == pipe) {
            fillFormFields(formDataPipe.get());
        }
        else if (routeFinderFormPipe == pipe) {
            fillFormFields(formDataPipe.get());
        }
        else if (detailedRouteInfoPipe == pipe) {
            if (detailedRouteInfoPipe.get()==null) {
                downloadBtn.setDisable(true);
                sendToFGBtn.setDisable(true);
            }
            else {
                downloadBtn.setDisable(false);
                sendToFGBtn.setDisable(false);
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
    
    @FXML
    public void handleSendToFGAction(ActionEvent event) {
        try {
            List<Waypoint> route = detailedRouteInfoPipe.get();
            routeFinderService.sendRouteToFlightGear(route);
            FxDialogs.showInformation("Send Route to FG", 
                    "The Route was successfully sent to FlightGear.", 
                    "Check the FG Route Manager to select runways and activate your route.");
        } catch (Exception ex) {
            L.error(ex, "Failed to send Route to FlightGear");
            FxDialogs.create()
                    .title("Error sending route to FlightGear")
                    .masthead("Failed to send route to FlightGear.")
                    .message("The following error occured: '" + ex.getMessage() + "'.")
                    .showException(ex);
        }
    }
    
    private void storeRouteXML(List<Waypoint> route) {
        Waypoint departure = route.get(0);
        Waypoint destination = route.get(route.size()-1);
        String fileName = departure.ident + "-" + destination.ident + ".xml";
        File directory = getRouteStorageDirectory();
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Route");
        fileChooser.setInitialFileName(fileName);
        if (directory!=null) {
            fileChooser.setInitialDirectory(directory);
        }
        
        File file = fileChooser.showSaveDialog(null);
        if (file!=null) {
            try {
                routeFinderService.saveRouteToFile(route, file);
                L.info("Route saved to %s", file.getAbsolutePath());
                FxDialogs.showInformation("Route saved.", 
                        "Route file saved successfully", 
                        "The route is saved to \n" + file.getAbsolutePath() + "\n" 
                        + "You can use this file with the FlightGear Route Manager.");
                storeRouteStorageDirectory(file.getParentFile());
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
    
    private File getRouteStorageDirectory() {
        File dir = services.getPersistenceService().readRoutesDirectory();
        if (dir==null) {
            String userHomePath = System.getProperty("user.home");
            if (userHomePath!=null) {
                dir = new File(userHomePath);
            }
        }
        return dir;
    }
    
    private void storeRouteStorageDirectory(File dir) {
        if (dir==null) {
            return;
        }
        File oldDir = services.getPersistenceService().readRoutesDirectory();
        if (!dir.equals(oldDir)) {
           services.getPersistenceService().writeRoutesDirectory(dir);
        }
    }
    
    private void setFormData(Flight bid) {
        if (bid == null) {
            // do not change anything!
        }
        else {
            String departure = bid.depICAO;
            String destination = bid.arrICAO;
            formDataPipe.set(new String[] {
                Objects.toString(departure, ""),
                Objects.toString(destination, "")
            });
        }
    }
    
    private void fillFormFields(String[] formData) {
        RouteFinderForm form = routeFinderFormPipe.get();
        if (form==null) {
            return;
        }
        if (formData!=null && formData.length==2) {
            form.setDeparture(formData[0]);
            form.setDestination(formData[1]);
        }
    }
    
    private void reloadBrowser() {
        browser.getEngine().load(routeFinderService.getRouteFinderURL());
    }
    
}
