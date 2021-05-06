package gui;

import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import logic.Card;
import logic.CardTriple;

import java.util.Optional;

/**
 * Eigene Klasse für den Dialog der das Zeigen einer Karte auf der Hand und falls keine vorhanden,
 * keiner Karte ermöglicht.
 * Notwendig, da die JavaFX Dialoge nicht vorsehen, dass ein Dialog mehrere Buttons haben können,
 * die ein Ergebnis vom Typ Card liefern und sich bei dem Klick schließen.
 * Die Fensterdekoration wurde entfernt, da eine Interaktion vom User zwingend erforderlich ist.
 *
 * @author Michael Smirnov
 */
public class ShowCardsDialog {

    //Die Stage des Dialogs
    private final Stage stage;
    //Die ausgewählte Karte
    private Card selectedOption = null;

    /**
     * Konstruktor der den Dialog erstellt.
     *
     * @param parent              das Hauptfenster
     * @param suspicion           die aktuelle Verdächtigung auf die eine Karte gezeigt werden soll.(Vorauswahl)
     * @param possibleCardsToShow die Karten, welche der Spieler daraufhin zeigen könnte.
     */
    public ShowCardsDialog(Window parent, CardTriple suspicion, CardTriple possibleCardsToShow) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(40, 20, 20, 40));

        Button characterButton = createButton(suspicion.getCharacter());
        Button weaponButton = createButton(suspicion.getWeapon());
        Button roomButton = createButton(suspicion.getRoom());
        Button showNothingButton = createButton(null);

        showNothingButton.setDisable(true);

        Label desc = new Label("Zeige eine deiner Karten!");
        desc.setStyle("-fx-font-weight: bold;");

        grid.add(desc, 0, 0);
        grid.add(characterButton, 0, 1);
        grid.add(weaponButton, 0, 2);
        grid.add(roomButton, 0, 3);
        grid.add(showNothingButton, 0, 4);

        characterButton.setDisable(!possibleCardsToShow.hasCharacter());
        weaponButton.setDisable(!possibleCardsToShow.hasWeapon());
        roomButton.setDisable(!possibleCardsToShow.hasRoom());

        if (possibleCardsToShow.isEmpty()) { //Wenn keine vorhanden dann nur aktivieren
            showNothingButton.setDisable(false);
        }

        Scene scene = new Scene(grid);
        stage = new Stage();
        stage.initOwner(parent);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
    }

    /**
     * Hilfsmethode um einen Button mir EventHandler zu erstellen.
     *
     * @param card Die Karte zu dem der Button erstellt werden soll.
     * @return der erstellte Button.
     */
    private Button createButton(Card card) {
        Button button;
        if (card != null) {
            button = new Button(card.getName());
        } else {
            button = new Button("Zeige nichts");
        }
        button.setOnAction(e -> {
            selectedOption = card;
            stage.close();
        });
        return button;
    }

    /**
     * Das Zeigen des Dialogs und warten auf Userinput.
     * Ist nicht schließbar da für das Spiel notw.
     *
     * @return Optional<Card> ob eine Karte gewählt wurde oder nicht (Empty == keine)
     */
    public Optional<Card> showDialog() {
        selectedOption = null;
        stage.setOnCloseRequest(Event::consume);
        stage.showAndWait();
        return Optional.ofNullable(selectedOption);
    }
}
