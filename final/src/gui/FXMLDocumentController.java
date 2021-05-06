package gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Initialisiert das Spielfeld und implementiert das FXML in Javastruktur.
 * Leitet alles an die JavaFX GUI Klasse weiter.
 *
 * @author Michael Smirnov
 */
public class FXMLDocumentController implements Initializable {
    //Die richtige JavaFX GUI
    private JavaFXGUI gui;

    //Das Wrappt das Spielfeldbild
    @FXML
    private Pane paneGameFieldWrapper;
    //Der Spielfeldbildkontainer
    @FXML
    private ImageView imgViewGameField;
    //Der Würfelbilderkontainer
    @FXML
    private ImageView imgViewDice;
    //Das GridPane in dem die Notizen enthalten sind
    @FXML
    private GridPane gridPaneNotes;
    //Die Personen auf der Hand
    @FXML
    private ListView<String> listViewCharacters;
    //Die Waffen auf der Hand
    @FXML
    private ListView<String> listViewWeapons;
    //Die Räume auf der Hand
    @FXML
    private ListView<String> listViewRooms;
    //Der Anklagebutton
    @FXML
    private Button btnAccusation;
    //Der Menüeintrag um ein Spiel zu laden
    @FXML
    private MenuItem menuItemLoadGame;
    //Der Menüeintrag um ein Spiel zu speichern
    @FXML
    private MenuItem menuItemSaveGame;
    //Der Menüeintrag um ein neues Spiel zu starten
    @FXML
    private MenuItem menuItemNewGame;
    //Die Checkbox, welche die möglichen Schritte, gegeben der Würfelaugen auf dem Spielfeld anzeigt.
    @FXML
    private CheckBox chkbxPossibleMoves;


    /**
     * Kümmert sich um den Mausklick in das Spielfeld.
     *
     * @param mouseEvent der Linksklick
     */
    @FXML
    void handleMouseClick(MouseEvent mouseEvent) {
        gui.handleMouseClick(mouseEvent);
    }

    /**
     * Kümmert sich um den Klick auf den Anklagebutton.
     */
    @FXML
    void handleAccusation() {
        gui.handleAccusation();
    }

    /**
     * Kümmert sich um den Klick auf den Menüeintrag Neues Spiel.
     */
    @FXML
    void handleNewGame() {
        gui.handleNewGame();
    }

    /**
     * Kümmert sich um den Klick auf die Ckeckbox für die möglichen Schritte.
     */
    @FXML
    void handleTogglePossibleMoves() {
        gui.handleTogglePossibleMoves(this.chkbxPossibleMoves.isSelected());
    }

    /**
     * Kümmert sich um den Klick auf den Menüeintrag Spiel Speichern.
     */
    @FXML
    void handleSaveGame() {
        gui.handleSaveGame();
    }

    /**
     * Kümmert sich um den Klick auf den Menüeintrag Spiel Laden.
     */
    @FXML
    void handleLoadGame() {
        gui.handleLoadGame();
    }

    /**
     * Initialisiert die GUI, jedoch bereits mit Stage, was das schließen des Programms während der
     * initialisierung wesentlich einfacher macht, da kein Exception-Handling notwendig ist, sondern immer
     * stage.close() benutzt werden kann.
     *
     * @param stage die Stage des Programms
     */
    public void initializeWithStage(Stage stage) {
        gui = new JavaFXGUI(stage, imgViewGameField, imgViewDice, listViewCharacters, listViewWeapons,
                listViewRooms, paneGameFieldWrapper, gridPaneNotes, btnAccusation,
                menuItemLoadGame, menuItemSaveGame, menuItemNewGame);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Muss implementiert werden
    }
}
