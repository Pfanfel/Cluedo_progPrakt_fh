package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import logic.exceptions.CluedoException;

import java.io.IOException;

/**
 * Das Spiel Cluedo mit einer JavaFX GUI.
 *
 * @author Michael Smirnov
 */
public class Cluedo extends Application {

    /**
     * Ein eigenes Runnable Interface, welches auch Cluedo Exceptions werfen kann.
     */
    public interface Runnable {
        void run() throws CluedoException;
    }

    /**
     * Die Einstiegsmethode des Spiels.
     *
     * @param args Die Kommandozeilenargumente.
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
        //Der Loader muss zuerst geladen werden, sonst ist der Loader null und getController liefert NullPointerException
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            primaryStage.close();
            return;
        }
        FXMLDocumentController controller = loader.getController();
        primaryStage.setTitle("Cluedo");
        primaryStage.setMinWidth(1400);
        primaryStage.setMinHeight(800);
        primaryStage.setScene(new Scene(root, 1630, 800));
        primaryStage.show();
        //Initialisiert das Spiel
        controller.initializeWithStage(primaryStage);
        //Letze Instanz die unerwarte Fehler fängt
        Thread.currentThread().setUncaughtExceptionHandler((Thread th, Throwable ex) -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unerwarteter kritischer Fehler");
            alert.setHeaderText("Kritischer Fehler!");
            alert.setContentText("Entschuldigung, das hätte nicht passieren dürfen!\nDas Spiel wird beendet ¯\\_(ツ)_/¯");
            alert.showAndWait();
            primaryStage.close();
        });
    }


}