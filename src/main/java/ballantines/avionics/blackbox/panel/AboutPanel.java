package ballantines.avionics.blackbox.panel;

import ballantines.avionics.blackbox.Services;
import ballantines.avionics.kacars.KAcarsConfig;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class AboutPanel implements Initializable {

     public static Parent create() throws IOException {
        AboutPanel controller = new AboutPanel();
        FXMLLoader loader = new FXMLLoader(AboutPanel.class.getResource("/fxml/about.fxml"));
        loader.setController(controller);
        return (Parent) loader.load();
    }
    
    @FXML
    private WebView aboutTextUI;
    @FXML
    private BorderPane outerPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        URL aboutUrl = getClass().getResource("/html/about.html");
        getWebEngine().load(aboutUrl.toExternalForm());
        
    }

    
    private WebEngine getWebEngine() {
        return aboutTextUI.getEngine();
    }
    
    @FXML
    void aboutButtonPressed(ActionEvent event) {
        URL aboutUrl = getClass().getResource("/html/about.html");
        getWebEngine().load(aboutUrl.toExternalForm());
    }
    
    @FXML
    void donateButtonPressed(ActionEvent event)  {
        URL aboutUrl = getClass().getResource("/html/donate.html");
        getWebEngine().load(aboutUrl.toExternalForm());
        /*
         try {
             java.awt.Desktop.getDesktop().browse(new URI("http://www.facebook.com"));
         } catch (Exception ex) {
             Logger.getLogger(AboutPanel.class.getName()).log(Level.SEVERE, null, ex);
         }
         */
    }

    @FXML
    void vaHomeButtonPressed(ActionEvent event) {
        KAcarsConfig config = Services.get().kacarsConfigPipe.get();
        if (config!=null) {
            String urlString = config.url;
            if (urlString != null && urlString.length() > 0) {
                try {
                    URL url = new URL(urlString);
                    String vaHomeUrl = url.getProtocol() + "://" + url.getHost();
                    getWebEngine().load(vaHomeUrl);
                }
                catch (MalformedURLException ex) {
                    // sorry...
                }
            }
        }
    }

    @FXML
    void facebookButtonPressed(ActionEvent event) throws MalformedURLException {
        getWebEngine().load("https://m.facebook.com/BallantinesAvionics");
    }
    
}
