package ballantines.avionics.blackbox.panel;

import ballantines.avionics.blackbox.Services;
import ballantines.avionics.blackbox.util.Log;
import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;


public class RouteFinderPanel implements PipeUpdateListener, Initializable {

    private static Log L = Log.forClass(RouteFinderPanel.class);
    private static final String ROUTE_FINDER_URL = "http://rfinder.asalink.net/free/";
    
    @FXML private WebView browser;
    
    private Services services;

    public static Parent create(Services services) throws IOException {
        RouteFinderPanel controller = new RouteFinderPanel();
        controller.setServices(services);
        FXMLLoader loader = new FXMLLoader(PIREPForm.class.getResource("/fxml/routefinder.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        reloadBrowser();
        
        /** 
         **/
        browser.getEngine().documentProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> observable, Document oldDocument, Document newDocument) {
                /*
                if (newDocument!=null) {
                    NodeList heads = newDocument.getElementsByTagName("head");
                    if (heads.getLength()>0) {
                        Element head = (Element) heads.item(0);
                        Element html = (Element) head.getParentNode();
                        html.removeChild(head);
                    }
                }
                        */
            }
        });
    }
    
    
    
    @Override
    public void pipeUpdated(Pipe pipe) {
        L.pipeUpdated(pipe);
    
    }
    
    private void reloadBrowser() {
        browser.getEngine().load(ROUTE_FINDER_URL);
    }

    public void setServices(Services services) {
        this.services = services;
    }
    
    

}
