package gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import logic.Character;
import logic.*;
import logic.exceptions.CluedoException;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Siehe Kommentar bzgl. "Anmerkung für Async* Klassen" in JavaFXGUI.
 * Diese Klasse kümmert sich um den Kontextwechsel "rein".
 * Die Magie steckt in doInJavaFx(Callable).
 *
 * @author Michael Smirnov
 */
public class AsyncJavaFXGUI implements GUIConnector {
    //Die eigentliche JavaFX GUI an die, die Aufrufe weitergeleitet werden.
    private final JavaFXGUI gui;
    //Ein zukünfiges Ereignis, welches signalisiert, dass die Operation fertig ist.
    private CompletableFuture<Void> finished;

    /**
     * Hilfsklasse für doInJavaFxWithCluedoException.
     * Diese innere Exception Klasse verpackt checked Exceptions und ist selber eine unchecked Exception.
     *
     * @author Michael Smirnov
     */
    private static class WrappedCheckedException extends RuntimeException {
        //Die verpackte Exception.
        private final Throwable inner;

        /**
         * Konstruktor.
         *
         * @param inner die zu verpackende Exception
         */
        private WrappedCheckedException(Throwable inner) {
            this.inner = inner;
        }

        /**
         * Entpackt das enthaltene Throwable und versucht diese auf CluedoException zu casten.
         * Wirft eine Runtime Exception wenn die enthaltene Exception nicht castbar ist.
         *
         * @return die Exception gecastet als CluedoException.
         */
        private CluedoException unwrap() {
            if (this.inner instanceof CluedoException) {
                return (CluedoException) this.inner;
            } else {
                throw new RuntimeException();
            }
        }
    }


    /**
     * Konstruktor.
     *
     * @param gui die JavaFX GUI welche die eigentliche Funktionalität enthält
     */
    public AsyncJavaFXGUI(JavaFXGUI gui) {
        this.gui = gui;
    }

    /**
     * Hilfsmethode um GUI-Operationen auszuführen, welche CluedoExceptions werfen.
     *
     * Um die CluedoException innerhalb des JavaFX-Kontextes hier wieder rauszugeben,
     * wird diese in dem Lambda in eine WrappedCheckedException verpackt und außerhalb
     * des doInJavaFx wieder entpackt.
     *
     * @param runnable Die auszuführende Operation.
     * @throws CluedoException falls in der Operation ein Problem auftritt.
     */
    private void doInJavaFxWithCluedoException(Cluedo.Runnable runnable) throws CluedoException {
        try {
            doInJavaFx(() -> {
                try {
                    runnable.run();
                } catch (CluedoException e) {
                    // Hier packen wir die Cluedo Exception ein.
                    throw new WrappedCheckedException(e);
                }
            });
        } catch (WrappedCheckedException wrappedCluedoException) {
            // Das hier muss unsere oben eingepackte Exception sein, also werfen wir sie wieder.
            throw wrappedCluedoException.unwrap();
        }
    }

    /**
     * Überladene Variante von doInJavaFx, welche ein Runnable statt einem Callable ausführt.
     * @param runnable Die auszuführende Operation.
     */
    private void doInJavaFx(Runnable runnable) {
        doInJavaFx(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Führt die übergebene Operation innerhalb des JavaFX Kontextes aus, damit dort JavaFX-Elemente
     * geändert werden können.
     *
     * @param callable die Operation, welche innerhalb des JavaFX Kontextes ausgeführt werden soll.
     * @param <T>      Der Rückgabtyp dieser Operation.
     * @return Das Ergebnis der ausgeführten Operation.
     */
    private <T> T doInJavaFx(Callable<T> callable) {
        // 1. Die Operation wird in einen FutureTask verpackt, damit man darauf warten kann (get).
        FutureTask<T> futureTask = new FutureTask<>(callable);

        // 2. Die Operation wird aufgerufen in dem JavaFX Kontext.
        Platform.runLater(futureTask);

        try {
            // 3. Es wird gewartet, bis die Operation in dem JavaFX Kontext zurückkehrt (return).
            T result = futureTask.get();
            // 4. Wenn die Operation makeFinishedHandler aufgerufen hat
            if (this.finished != null) {
                // wird gewartet, bis die JavaFXGUI den finishedHandler aufruft
                this.finished.get();
                // und die Future weggeräumt. Sie hat ihren Zweck erfüllt.
                this.finished = null;
            }
            return result;
        } catch (InterruptedException e) {
            // Interrupted Exception kann nur auftreten, wenn man diesen Thread interruptet.
            // Das passiert in diesem Programm nicht, daher kann auch keine InterruptedException auftreten.
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            // Wenn in der Operation eine Exception geworfen wird, packt FutureTask diese in eine ExecutionException ein und wirft diese.
            Throwable exceptionInTask = e.getCause();

            // Wenn in dem Task eine Runtime Exception geworfen wurde,
            if (exceptionInTask instanceof RuntimeException) {
                // werfen wir sie einfach wieder.
                throw (RuntimeException) exceptionInTask;
            }

            //Die Operationen können nur unchecked Exceptions werfen.
            //Daher sollte das hier nie passieren.
            throw new RuntimeException(exceptionInTask);
        }
    }


    /**
     * Erstellt die finished Future und gibt einen EventHandler zurück, der die Future abschließt.
     * z.B. um mittzuteilen, dass eine Animation abgeschlossen wurde.
     * @return Der EventHandler, der finished abschließt
     */
    public EventHandler<ActionEvent> makeFinishedHandler() {
        this.finished = new CompletableFuture<>();
        return (event -> {
            finished.complete(null);
        });
    }

    @Override
    public void updateDice(int dice) {
        doInJavaFx(() -> gui.updateDice(dice));
    }

    @Override
    public void drawCharacterOnCorridor(Character character, int characterIndex) {
        doInJavaFx(() -> gui.drawCharacterOnCorridor(character, characterIndex));
    }

    @Override
    public void drawCharacterInRoom(Character character, int characterIndex) {
        doInJavaFx(() -> gui.drawCharacterInRoom(character, characterIndex));
    }

    @Override
    public void setWeapon(Room room, Weapon weapon) throws CluedoException {
        doInJavaFxWithCluedoException(() -> gui.setWeapon(room, weapon));
    }

    @Override
    public void handleException(CluedoException e) {
        doInJavaFx(() -> gui.handleException(e));
    }

    @Override
    public void redrawGUI() {
        doInJavaFx(gui::redrawGUI);
    }

    @Override
    public void showIllegalStepMessage() {
        doInJavaFx(gui::showIllegalStepMessage);
    }

    @Override
    public void drawPossibleMoves(Set<Position> possibleMoves) {
        doInJavaFx(() -> gui.drawPossibleMoves(possibleMoves));
    }

    @Override
    public void clearPossibleMoves() {
        doInJavaFx(gui::clearPossibleMoves);
    }

    @Override
    public CardTriple handleExpressSuspicion(Card enteredRoom) {
        return doInJavaFx(() -> gui.handleExpressSuspicion(enteredRoom));
    }

    @Override
    public void handleAISuspicion(String playerName, CardTriple suspicion) {
        doInJavaFx(() -> gui.handleAISuspicion(playerName, suspicion));
    }

    @Override
    public void handleOwnSuspicionResult(Player[] allPlayers, Card[] shownByAI, CardTriple suspicion) throws CluedoException {
        doInJavaFxWithCluedoException(() -> gui.handleOwnSuspicionResult(allPlayers, shownByAI, suspicion));
    }

    @Override
    public void handleOthersSuspicionResult(Player[] allPlayers, Player currentPlayer, Card[] shownCards, CardTriple suspicion) throws CluedoException {
        doInJavaFxWithCluedoException(() -> gui.handleOthersSuspicionResult(allPlayers, currentPlayer, shownCards, suspicion));
    }

    @Override
    public Card handleShowCard(CardTriple suspicion, CardTriple possibleCardsToShow) {
        return doInJavaFx(() -> gui.handleShowCard(suspicion, possibleCardsToShow));
    }

    @Override
    public void handleGameWon(CardTriple solution, Player winner) {
        doInJavaFx(() -> gui.handleGameWon(solution, winner));
    }

    @Override
    public void handleGameLost(CardTriple wrongSolution, CardTriple solution, Player loser) {
        doInJavaFx(() -> gui.handleGameLost(wrongSolution, solution, loser));
    }
}
