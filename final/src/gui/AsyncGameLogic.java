package gui;

import javafx.concurrent.Task;
import logic.GUIConnector;
import logic.GameLogic;
import logic.Position;
import logic.exceptions.CluedoException;
import logic.json.InitialGameDataJSON;

import java.io.File;

/**
 * Siehe Kommentar bzgl. "Anmerkung für Async* Klassen" in JavaFXGUI.
 * Diese Klasse kümmert sich um den Kontextwechsel "raus".
 * Die Magie steckt in doOutOfJavaFx.
 *
 * @author Michael Smirnov
 */
public class AsyncGameLogic {
    //Die eigentliche Logik welche sich um das Spiel kümmert.
    private final GameLogic logic;
    //Die eigentliche JavaFX GUI.
    private final JavaFXGUI gui;

    /**
     * Konstruktor.
     *
     * @param logic Die Spiellogik mit der gesamten Funktionalität.
     * @param gui   Die JavaFX GUI mit der gesamten Funktionalität.
     */
    public AsyncGameLogic(GameLogic logic, JavaFXGUI gui) {
        this.logic = logic;
        this.gui = gui;
    }

    /**
     * Führt die übergebene Operation außerhalb des JavaFX Kontextes aus, damit dort blockiert werden kann.
     * <p>
     * Startet einen Task mit dem übergeben Runnable, welcher in einem neuen Tread läuft.
     * Setzt Eventhandler für den Fall, dass der Task fehlschlägt oder erfolgreich beendet.
     *
     * @param runnable Die Aktion, welche nicht im JavaFX Kontext ausgeführt werden soll.
     *                 Das Interface wurde erweitert, da das Java Runnable keine checked Exception
     *                 wirft.
     */
    public void doOutOfJavaFx(Cluedo.Runnable runnable) {
        // 1. Während eine Operation in der Logik läuft, soll die GUI nicht interagierbar sein.
        gui.disableClicks();
        // 2. Die Operation wird in einen Task gepackt, damit man onFailed und onSuccess Handler
        // installieren kann, die in dem JavaFX-Kontext laufen.
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                runnable.run();
                return null;
            }
        };
        //3. Falls die übergebene Operation fehlschlägt, soll
        //      1. Die GUI wieder interagierbar sein
        //      2. Eine CluedoException angezeigt werden.
        task.setOnFailed(workerStateEvent -> {
            // Dieser EventHandler läuft innerhalb des JavaFX-Kontextes und darf daher GUI-Methoden aufrufen.
            gui.enableClicks();
            Throwable exception = workerStateEvent.getSource().getException();
            if (exception instanceof CluedoException) {
                gui.handleException((CluedoException) exception);
            } else if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            } else {
                //Das dürfte nicht passieren, da Cluedo.Runnable nur CluedoException und RuntimeException zulässt.
                throw new RuntimeException(exception);
            }
        });
        //4. Falls die Operation erfolgreich abgeschlossen wurde, soll die GUI wieder interagierbar sein.
        task.setOnSucceeded(workerStateEvent -> {
            gui.enableClicks();
        });
        //5. Um diesen Task außerhalb ausführen zu können, muss ein neuer Thread gestartet werden.
        final Thread taskThread = new Thread(task);
        taskThread.setDaemon(true);
        taskThread.start();
    }

    /**
     * Delegiert die Initialisierung an GameLogic#init.
     *
     * @param gui Der GUIConnector der in der GameLogic verwendet werden soll.
     */
    public void init(GUIConnector gui) {
        doOutOfJavaFx(() -> {
            logic.init(gui);
        });
    }

    /**
     * Delegiert den Spielzug an GameLogic#makeMove.
     * Führt einen Spielzug auf dem Spielfeld aus, nachdem der Spieler auf das Spielfeld geklickt hat.
     *
     * @param gameCellPosition Die Position des geklickten Spielfeldelementes.
     */
    public void makeMove(Position gameCellPosition) {
        doOutOfJavaFx(() -> {
            logic.makeMove(gameCellPosition);
        });
    }

    /**
     * Lädt eine Spielstandsdatei aus dem Dateisystem.
     *
     * @param selectedFile        Die Datei welche geladen werden soll.
     * @param initialGameDataJSON Die Initialisierungsdatei, welche die Ausgangspositionen, Räume,
     *                            Waffen, Personen und das Spielfeld enthält.
     */
    public void loadGame(File selectedFile, InitialGameDataJSON initialGameDataJSON) {
        doOutOfJavaFx(() -> {
            logic.loadGame(selectedFile, initialGameDataJSON);
        });
    }

    /**
     * Speichert den aktuellen Spielstand in das Dateisystem.
     *
     * @param selectedFile Die Datei, in welche gespeichert werden soll.
     */
    public void saveGame(File selectedFile) {
        doOutOfJavaFx(() -> {
            logic.saveGame(selectedFile);
        });
    }
}
