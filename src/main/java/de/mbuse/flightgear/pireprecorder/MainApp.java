package de.mbuse.flightgear.pireprecorder;

import de.mbuse.flightgear.pireprecorder.udp.UDPServer;
import de.mbuse.pipes.Pipes;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;


public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("PIREP Recorder starting...");
        Services services = Services.get();
        services.init();
        
        TabPane root = new TabPane();
        
        Tab pirep = createTab(PIREPForm.create(services), "PIREP Form");
        Tab route = createTab(RoutePanel.create(services), "Route");
        Tab parking = createTab(ParkingPositionPanel.create(services), "Parking");
        Tab config = createTab(ConfigurationForm.create(services), "Configuration");
        
        root.getTabs().addAll(pirep, route, parking, config);
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/pirep.css");
        
        stage.setTitle("PIREP Recorder");
        stage.setScene(scene);
        stage.show();
        
        UDPServer udpServer = new UDPServer();
        
        Pipes.connect(services.udpServerRunningPipe, udpServer.runningPipe);
        Pipes.connect(services.udpServerPortPipe, udpServer.portPipe);
    }

    @Override
    public void stop() throws Exception {
        Services.get().shutdown();
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
