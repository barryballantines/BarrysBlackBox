package de.mbuse.flightgear.pireprecorder;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;


public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FlightDataRetrieval retrieval = new FGFlightDataRetrievalImpl();
        
        TabPane root = new TabPane();
        
        
        Tab pirep = createTab(PIREPForm.create(retrieval), "PIREP Form");
        Tab route = createTab(RoutePanel.create(retrieval), "Route");
        Tab config = createTab(ConfigurationForm.create(retrieval), "Configuration");
        
        root.getTabs().addAll(pirep, route, config);
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/pirep.css");
        
        stage.setTitle("PIREP Recorder");
        stage.setScene(scene);
        stage.show();
        System.out.println("PIREP Recorder started...");
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
