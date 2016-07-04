package ballantines.javafx;

import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.event.EventHandler;

/** 
 * A replacement for controlsfx dialogs as shown in the post
 * 
 * http://stackoverflow.com/questions/26341152/controlsfx-dialogs-deprecated-for-what
 * 
 */
public class FxDialogs {
    
    private String title;
    private String masthead;
    private String message;
    private String[] actions;
    
    FxDialogs() {
        title = "";
        masthead="";
        message="";
        actions= new String[0];
    }
    
    public static FxDialogs create() {
        return new FxDialogs();
    }
    public FxDialogs title(String text) {
        this.title = text;
        return this;
    }
    public FxDialogs masthead(String text) {
        this.masthead = text;
        return this;
    }
    public FxDialogs message(String text) {
        this.message = text;
        return this;
    }
    
    public FxDialogs actions(String... actions) {
        this.actions = actions;
        return this;
    }
    
    public void showException(Exception ex) {
        FxDialogs.showException(this.title, this.masthead, this.message, ex);
    }
    
    public String showConfirm() {
        return FxDialogs.showConfirm(this.title, this.masthead, this.message, this.actions);
    }
    
    public void showInformation() {
        FxDialogs.showInformation(title, masthead, message);
    }
    
    public void showWarning() {
        FxDialogs.showWarning(title, masthead, message);
    }
    
    public String showTextInput(String defaultText) {
        return FxDialogs.showTextInput(title, masthead, message, defaultText);
    }

    // === STATIC API ===
    
    public static void showInformation(String title, String masthead, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(masthead);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static void showWarning(String title, String masthead, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(masthead);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static void showError(String title, String masthead, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(masthead);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static void showException(String title, String masthead, String message, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(masthead);
        alert.setContentText(message);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("Details:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String OK = "OK";
    public static final String CANCEL = "Cancel";

    public static String showConfirm(String title, String masthead, String message, String... options) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(masthead);
        alert.setContentText(message);

        //To make enter key press the actual focused button, not the first one. Just like pressing "space".
        alert.getDialogPane().addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    event.consume();
                    try {
                        Robot r = new Robot();
                        r.keyPress(java.awt.event.KeyEvent.VK_SPACE);
                        r.keyRelease(java.awt.event.KeyEvent.VK_SPACE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
            
        if (options == null || options.length == 0) {
            options = new String[]{OK, CANCEL};
        }

        List<ButtonType> buttons = new ArrayList<>();
        for (String option : options) {
            buttons.add(new ButtonType(option));
        }

        alert.getButtonTypes().setAll(buttons);

        Optional<ButtonType> result = alert.showAndWait();
        if (!result.isPresent()) {
            return CANCEL;
        } else {
            return result.get().getText();
        }   
    }

    public static String showTextInput(String title, String masthead, String message, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle(title);
        dialog.setHeaderText(masthead);
        dialog.setContentText(message);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        } else {
            return null;
        }

    }

}