package ballantines.avionics.blackbox;

import ballantines.avionics.blackbox.panel.AboutPanel;
import ballantines.avionics.blackbox.panel.PIREPFilingForm;
import ballantines.avionics.blackbox.panel.PIREPForm;
import ballantines.avionics.blackbox.panel.RoutePanel;
import ballantines.avionics.blackbox.panel.PositionPanel;
import ballantines.avionics.blackbox.panel.ConfigurationForm;
import ballantines.avionics.blackbox.panel.Toolbar;
import ballantines.avionics.blackbox.udp.UDPServer;
import ballantines.avionics.blackbox.util.Log;
import de.mbuse.pipes.Pipes;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class MainApp extends Application {
    private static Log L = Log.forClass(MainApp.class);
    @Override
    public void start(Stage stage) throws Exception {
        L.info("Barry's BlackBox is starting...");
        Services services = Services.get();
        services.init();
        BorderPane root = new BorderPane();
        
        TabPane tabs = new TabPane();
        
        Tab pirep = createTab(PIREPForm.create(services), "Overview");
        Tab route = createTab(RoutePanel.create(services), "Route");
        Tab pirepFiling = createTab(PIREPFilingForm.create(services), "PIREP");
        Tab position = createTab(PositionPanel.create(services), "Position");
        Tab config = createTab(ConfigurationForm.create(services), "Configuration");
        Tab about = createTab(AboutPanel.create(), "About");
        tabs.getTabs().addAll(pirep, route, pirepFiling, position, config, about);
        
        root.setTop(Toolbar.create(services));
        root.setCenter(tabs);
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/pirep.css");
        
        stage.setTitle("Barry's BlackBox");
        stage.setScene(scene);
        stage.show();
        
        final UDPServer udpServer = new UDPServer();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent t) {
                udpServer.stop();
            }
        });
        
        services.flightDataPipe.connectTo(udpServer.flightDataPipe);
        
        Pipes.connect(services.udpServerRunningPipe, udpServer.runningPipe);
        Pipes.connect(services.udpServerPortPipe, udpServer.portPipe);
        
    }

    @Override
    public void stop() throws Exception {
        Services.get().shutdown();
        L.info("Barry's BlackBox stopped.");
    }
    
    
    
    
    private static Tab createTab(Parent panel, String title) {
        Tab tab = new Tab();
        tab.setText(title);
        tab.setClosable(false);
        tab.setContent(panel);
        return tab;
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
