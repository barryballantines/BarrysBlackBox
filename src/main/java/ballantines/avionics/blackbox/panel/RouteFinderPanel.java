package ballantines.avionics.blackbox.panel;

import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.util.Calculus;
import ballantines.avionics.blackbox.util.Log;
import ballantines.avionics.kacars.model.Flight;
import ballantines.avionics.blackbox.model.Waypoint;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    
    @FXML
    public void handleResetAction(ActionEvent event) {
        reloadBrowser();
    }
    
    @FXML 
    public void handleDownloadAction(ActionEvent event) {
        List<Waypoint> route = extractWaypoints();
        if (route!=null && route.size()>2) {
            storeRouteXML(route);
        }
    }
    
    private List<Waypoint> extractWaypoints() {
        try {
            BufferedReader reader = new BufferedReader(new StringReader(detailedRouteInfoPipe.get()));
            List<Waypoint> route = new ArrayList<>();

            String line = reader.readLine();
            int identStart  = line.indexOf("ID");
            int identStop   = line.indexOf("FREQ");
            int coordsStart = line.indexOf("Coords");
            int coordsStop  = line.indexOf("Name/Remarks");
            line = reader.readLine();
            while (line!=null) {
                String coords = line.substring(coordsStart, coordsStop).trim();
                String[] latlon = coords.split(" ");
                
                Waypoint wp = new Waypoint();
                wp.ident = line.substring(identStart,identStop).trim();
                wp.lat = Calculus.parseDegreeToDecimal(latlon[0].trim());
                wp.lon = Calculus.parseDegreeToDecimal(latlon[1].trim());
                route.add(wp);
                
                line = reader.readLine();
            }
            
            return route;
            
        } catch (IOException ex) {
            L.error(ex, "Failed to extract route.");
        }
        return null;
    }
    
    private void storeRouteXML(List<Waypoint> route) {
        Waypoint departure = route.remove(0);
        Waypoint destination = route.remove(route.size()-1);
        String fileName = departure.ident + "-" + destination.ident + ".xml";
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Route");
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(null);
        try {
            file.createNewFile();
            PrintWriter w = new PrintWriter(file);
            
            w.println("<?xml version=\"1.0\"?>");
            w.println("<PropertyList>");            
            w.println("  <version type=\"int\">2</version>");           
            w.println("  <departure>");           
            w.println("    <airport type=\"string\">"+departure.ident+"</airport>");           
            w.println("  </departure>");           
            w.println("  <destination>");       
            w.println("    <airport type=\"string\">"+destination.ident+"</airport>");       
            w.println("  </destination>");
            w.println("  <route>");
            
            for (int i=0; i<route.size(); i++) {
                Waypoint wp = route.get(i);
                String n = (i==0) ? "" : " n=\""+i+"\"";
                String lon = String.format(Locale.US, "%.6f", wp.lon);
                String lat = String.format(Locale.US, "%.6f", wp.lat);
                w.println("    <wp"+n+">");
                w.println("      <type type=\"string\">basic</type>");
                w.println("      <ident type=\"string\">"+wp.ident+"</ident>");
                w.println("      <lon type=\"double\">"+lon+"</lon>");
                w.println("      <lat type=\"double\">"+lat+"</lat>");
                w.println("   </wp>");
            }
            w.println("  </route>");
            w.println("</PropertyList>");
            w.flush();
            w.close();
            
        } catch (IOException ex) {
            L.error(ex, "Failed to store route xml.");
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
