package gui;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.StringConverter;
import logic.Character;
import logic.*;
import logic.exceptions.CluedoException;
import logic.exceptions.ExceptionType;
import logic.json.InitialCharacterJSON;
import logic.json.InitialGameDataJSON;
import logic.json.InitialRoomJSON;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Klasse welche die eigentliche JavaFX GUI des Spiels darstellt.
 * Implementiert das Interface GUIConnector um von der Logik aus aufgerufen werden zu können.
 * Sie ist der Einstiegspunkt des Programms und stößt das initiale Laden der Spiellogik an.
 * <p>
 * Anmerkung für Async* Klassen:
 * Die Interaktion zwischen JavaFXGUI und GameLogic wird mittels thread-basierter Koroutinen abgebildet,
 * um "einfach" Animationen unterstützen zu können, ohne dass GameLogic oder JavaFXGUI Code stark angepasst werden müssen.
 * <p>
 * Die Imlpementation besteht aus zwei Teilen: AsyncJavaFXGUI und AsyncGameLogic. Diese
 * implementieren dieselben Methoden, wie ihr "normales" Gegenstück, aber wechseln dabei den Threadkontext.
 * <p>
 * Damit wir blockierende Aufrufe machen können, müssen wir "raus" aus dem JavaFX-Application Thread.
 * Da immer nur ein JavaFX-Application Thread zur Zeit laufen darf.
 * Dadurch, dass diese Aufrufe aber nicht mehr in einem JavaFX-Application Thread passieren,
 * dürfen sie nicht auf die JavaFX-Elemente zugreifen. Daher müssen wir erstmal wieder "rein" in
 * einen JavaFX-Application Thread
 * <p>
 * Um diesen Kontextwechsel "rein" auszuführen wird der Logic die AsyncJavaFXGUI übergeben. Diese führt
 * GUI-Operation asynchron in einem JavaFX-Application Thread aus und wartet (blockiert) auf das
 * Ende der GUI-Operation.
 * <p>
 * Aufrufe an die Logik, die wiederrum die GUI aufrufen, müssen erst einmal "raus" aus dem JavaFX-Application Thread, damit sie blockieren können.
 * Deswegen wird an diesen Stellen nicht logic.xyz() aufgerufen, sondern asyncLogic.xyz()
 * <p>
 * Aufrufe in der Logik, welche nicht die GUI aufrufen (zB getter), können direkt erfolgen,
 * da der Kontext nicht gewechselt werden muss.
 */
public class JavaFXGUI implements GUIConnector {
    //Die Breite des Spielfeldbildes in Pixeln.
    private static final int GAME_FIELD_IMAGE_WIDTH = 2664;
    //Die Höhe des Spielfeldbildes in Pixeln.
    private static final int GAME_FIELD_IMAGE_HEIGHT = 2780;
    //Das Seitenverhäldniss des Spielfeldbildes
    private static final double GAME_FIELD_IMAGE_ASPECT_RATIO = (double) GAME_FIELD_IMAGE_WIDTH / GAME_FIELD_IMAGE_HEIGHT;
    //Der Abstand des Spielbretts zu der Bildkante links in Pixeln.
    private static final int GAME_FIELD_BORDER_LEFT = 68;
    //Der Abstand des Spielbretts zu der Bildkante oben in Pixeln.
    private static final int GAME_FIELD_BORDER_TOP = 78;
    //Die Breite des Spielbretts auf dem sich die Spielfiguren bewegen können in Pixeln.
    private static final int GAME_FIELD_WIDTH = 2515;
    //Die Höhe des Spielbretts auf dem sich die Spielfiguren bewegen können in Pixeln.
    private static final int GAME_FIELD_HEIGHT = 2630;
    //Die Anzal der Spielfeldzellen in der horizontalen.
    private static final int GAME_FIELD_CELLS_HORIZONTAL = 24;
    //Die Anzal der Spielfeldzellen in der vertikalen.
    private static final int GAME_FIELD_CELLS_VERTICAL = 25;
    //Anzahl der Würfelseiten.
    private static final int DICE_FACES = 6;
    //Größe der Spielfiguren
    private static final int CHARACTER_CIRCLE_SIZE = 8;
    //Minimale Anzahl von Spielern
    private static final int MIN_PLAYER_COUNT = 3;
    //Max. Anzahl der Spieler
    private static final int MAX_PLAYER_COUNT = 6;
    //Linienbreite der möglichen Schritte
    private static final int POSSIBLE_MOVES_CIRCLE_STROKE_WIDTH = 3;

    //Indizes der Notizen im Gridpane mit den Notizen
    //Die Spalte bei der die Notizen anfangen
    private static final int NOTES_START_COL_INDEX = 1;
    //Die Spalte bei der die Notizen enden
    private static final int NOTES_END_COL_INDEX = 6;
    //Die Spalte des menschlichen Spielers
    private static final int NOTES_SELF_COL_INDEX = 1;
    //Die Zeile bei der die Notizen anfangen
    private static final int NOTES_START_ROW_INDEX = 1;
    //Die Zeile bei der die Notizen enden
    private static final int NOTES_END_ROW_INDEX = 23;
    //Die Leerzeile zwischen Waffen und Personen
    private static final int NOTES_CHARACTER_AND_WEAPON_DIVIDER_ROW_INDEX = 7;
    //Die Leerzeile zwischen Waffen und Räumen
    private static final int NOTES_WEAPON_AND_ROOM_DIVIDER_ROW_INDEX = 14;

    //Der Abstand in der vertikalen zwischen den Waffen in einem Raum
    private static final double WEAPON_LABEL_GAP_FAKTOR = 0.7;
    //Der Abstand in der vertikalen zwischen den Spielfiguren in einem Raum
    private static final double CHARACTER_CIRCLE_GAP_FAKTOR = 0.6;
    //Schwelle bei Doubleungenauigkeiten
    public static final double DOUBLE_EPSILON = 0.001;
    //Animationszeit einer Verschiebung in Sekunden
    public static final double ANIMATION_TIME_SECONDS = 1.0;
    //Abstand von Bildrand oben zu erstem Feld (Prozentual)
    private static final double OFFSET_PERCENTAGE_TOP = (double) GAME_FIELD_BORDER_TOP / GAME_FIELD_IMAGE_HEIGHT;
    //Abstand von Bildrand links zu erstem Feld (Prozentual)
    private static final double OFFSET_PERCENTAGE_LEFT = (double) GAME_FIELD_BORDER_LEFT / GAME_FIELD_IMAGE_WIDTH;
    //Prozentuale höhe einer Spielfeldzelle
    private static final double CELL_HEIGHT_PERCENTAGE = (((double) GAME_FIELD_HEIGHT / GAME_FIELD_CELLS_VERTICAL) / GAME_FIELD_IMAGE_HEIGHT);
    //Prozentuale breite einer Spielfeldzelle
    private static final double CELL_WIDTH_PERCENTAGE = (((double) GAME_FIELD_WIDTH / GAME_FIELD_CELLS_HORIZONTAL) / GAME_FIELD_IMAGE_WIDTH);

    //Die aktuelle Breite des Spielfeldbildes in abh. von der aktuellen Fenstergrösse
    private final NumberBinding currImageWidth;
    //Die aktuelle Höhe des Spielfeldbildes in abh. von der aktuellen Fenstergrösse
    private final NumberBinding currImageHeight;

    //Die Stage des Hauptprogramms
    private final Stage stage;
    //Das Spielfeldbild
    private final Image gameFieldImg = new Image(getClass().getResourceAsStream("assets/Spielplan.jpg"));
    //Der Spielfeldbildkontainer
    private final ImageView imgViewGameField;
    //Der Würfelbilderkontainer
    private final ImageView imgViewDice;
    //Die Personen auf der Hand
    private final ListView<String> listViewCharacters;
    //Die Waffen auf der Hand
    private final ListView<String> listViewWeapons;
    //Die Räume auf der Hand
    private final ListView<String> listViewRooms;
    //Der Anklagebutton
    private final Button btnAccusation;
    //Hilfselement um den ImageView zu kapseln um das Bild abhängig von der
    //Fenstergröße zu skalieren
    private final Pane paneGameFieldWrapper;
    //Das GridPane in dem die Notizen enthalten sind
    private final GridPane gridPaneNotes;
    //Die Gruppe in der nur die Waffenlabes enthalten sind
    private final Group weaponsGrp = new Group();
    //Die Gruppe in der nur die Kreise der möglichen Schritte enthalten sind
    private final Group possibleMovesGrp = new Group();
    //Der Menüeintrag um ein Spiel zu laden
    private final MenuItem menuItemNewGame;
    //Der Menüeintrag um ein Spiel zu speichern
    private final MenuItem menuItemSaveGame;
    //Der Menüeintrag um ein neues Spiel zu starten
    private final MenuItem menuItemLoadGame;

    //Die Bilder der verschiedenen Würfelseiten
    private Image[] diceImages;
    //Die Labels der verschiedenen Waffen
    private Label[] weaponLables;
    //Die Kreise der Spielfiguren
    private Circle[] characterCircles;
    //Der Dialog, welcher die Verdächtigungsergebnisse zusammenfasst
    private Alert suspicionResultDialog;
    //Die Initialisierungsinformationen eines neuen Spiels
    private InitialGameDataJSON initialGameDataJSON;
    //Status ob die GUI bedienbar ist
    private boolean guiClickable = true;
    //Die eigetliche Spiellogik
    private GameLogic logic;
    //Wird verwendet für Aufrufe der Logik, die wiederum den GUIConnector aufrufen, also blockieren müssen.
    private AsyncGameLogic asyncLogic;
    //Wird zur Initialisierung verwendet und um das Ende der Animation zu signalisieren.
    private AsyncJavaFXGUI asyncGui;

    /**
     * Konstruktor der GUI
     *
     * @param stage                Die Stage der GUI
     * @param imgViewGameField     Der Spielfeldbildkontainer
     * @param imgViewDice          Der Würfelbilderkontainer
     * @param listViewCharacters   Die Personen auf der Hand
     * @param listViewWeapons      Die Waffen auf der Hand
     * @param listViewRooms        Die Räume auf der Hand
     * @param paneGameFieldWrapper Hilfselement um den ImageView des Spielfeldes zu kapseln
     * @param gridPaneNotes        Das GridPane in dem die Notizen enthalten sind
     * @param btnAccusation        Der Anklagebutton
     * @param menuItemLoadGame     Der Menüeintrag um ein neues Spiel zu starten
     * @param menuItemSaveGame     Der Menüeintrag um ein Spiel zu speichern
     * @param menuItemNewGame      Der Menüeintrag um ein Spiel zu laden
     */
    public JavaFXGUI(Stage stage, ImageView imgViewGameField, ImageView imgViewDice,
                     ListView<String> listViewCharacters, ListView<String> listViewWeapons,
                     ListView<String> listViewRooms, Pane paneGameFieldWrapper,
                     GridPane gridPaneNotes, Button btnAccusation, MenuItem menuItemLoadGame, MenuItem menuItemSaveGame, MenuItem menuItemNewGame) {
        this.stage = stage;
        this.imgViewGameField = imgViewGameField;
        this.imgViewDice = imgViewDice;
        this.listViewCharacters = listViewCharacters;
        this.listViewRooms = listViewRooms;
        this.listViewWeapons = listViewWeapons;
        this.paneGameFieldWrapper = paneGameFieldWrapper;
        this.gridPaneNotes = gridPaneNotes;
        this.currImageWidth = Bindings.min(imgViewGameField.fitWidthProperty(), imgViewGameField.fitHeightProperty().multiply(GAME_FIELD_IMAGE_ASPECT_RATIO));
        this.currImageHeight = Bindings.min(imgViewGameField.fitHeightProperty(), imgViewGameField.fitWidthProperty().divide(GAME_FIELD_IMAGE_ASPECT_RATIO));
        this.btnAccusation = btnAccusation;
        this.menuItemNewGame = menuItemNewGame;
        this.menuItemSaveGame = menuItemSaveGame;
        this.menuItemLoadGame = menuItemLoadGame;

        try {
            this.initialGameDataJSON = loadInitialGameData("/logic/config/InitialGameDataCluedo.json");
            startGame();
        } catch (CluedoException e) {
            handleException(e);
        }
        //Eventhandler welcher sich darum kümmert, wenn das Hauptspiel geschlossen werden soll
        stage.setOnCloseRequest(windowEvent -> {
            if (this.guiClickable) {
                boolean success = handleExitGame();
                if (success) {
                    closeSuspicionResultDialog();
                } else {
                    windowEvent.consume();
                }
            } else {
                windowEvent.consume();
            }
        });
    }

    /**
     * Setzt das Spielfeld und bindet die Properties für die Skalierung
     */
    private void setGameFieldImage() {
        imgViewGameField.setImage(this.gameFieldImg);
        imgViewGameField.fitWidthProperty().bind(paneGameFieldWrapper.widthProperty());
        imgViewGameField.fitHeightProperty().bind(paneGameFieldWrapper.heightProperty());
    }

    /**
     * Startet ein neues Cluedo Spiel.
     *
     * @throws CluedoException falls beim vorbereiten des Spiels auftreten.
     */
    private void startGame() throws CluedoException {
        Optional<StartGameInfo> res = startGameDialog();
        if (res.isEmpty()) {
            throw new CluedoException(ExceptionType.CanceledAtGameStart);
        }
        this.clearGUI();
        StartGameInfo startGameInfo = res.get();
        int playerCount = startGameInfo.getPlayerCount();
        AIDifficulty[] difficultiesFromDialog = startGameInfo.getPlayerDifficulties();
        this.logic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, playerCount, difficultiesFromDialog);
        //Die AsyncJavaFXGUI braucht eine Referenz auf this, um später GUI Operationen ausführen zu können.
        this.asyncGui = new AsyncJavaFXGUI(this);
        //Die AsyncGameLogic braucht eine Referenz auf die logic, um später Logik-Operationen ausführen zu können.
        //Und eine Referenz auf this, um die GUI während der Animationen zu blockieren und Fehler anzuzeigen.
        this.asyncLogic = new AsyncGameLogic(logic, this);
        this.initGui(logic.getPlayers(), logic.getCharacters(), logic.getWeapons());
        //Wir übergeben der Logik die asyncGui, damit alle Aufrufe blockierend passieren.
        this.asyncLogic.init(asyncGui);
    }

    /**
     * Initialisiert die GUI zum start eines neuen Spiels.
     *
     * @param players    die Spieler, die in dem Spiel teilnehmen.
     * @param characters die Personen im Spiel, die in dem Spiel teilnehmen.
     */
    private void initGui(Player[] players, Character[] characters, Weapon[] weapons) {
        this.characterCircles = new Circle[characters.length];
        this.initCircles(characters);
        this.weaponLables = new Label[weapons.length];
        this.setGameFieldImage();
        this.loadImages();
        this.paneGameFieldWrapper.getChildren().add(possibleMovesGrp);
        this.initNoteDescription();
        this.initNotes(players[0]);
        this.initOwnCardInfo(players[0].getCards());
        this.paneGameFieldWrapper.getChildren().add(this.weaponsGrp);
        this.initWeapons();

    }

    /**
     * Setzt die GUI auf den Ausgangszustand zurück.
     *
     * @param players    die Spieler, die in dem Spiel teilnehmen.
     * @param characters die Personen im Spiel, die in dem Spiel teilnehmen.
     * @throws CluedoException falls beim Initialisieren Fehler passieren.
     */
    private void resetGUI(Player[] players, Character[] characters) throws CluedoException {
        this.characterCircles = new Circle[characters.length];
        this.initCircles(characters);
        this.paneGameFieldWrapper.getChildren().add(possibleMovesGrp);
        this.initNotes(players[0]);
        this.initOwnCardInfo(players[0].getCards());
        this.paneGameFieldWrapper.getChildren().add(weaponsGrp);
        this.initWeapons();
    }


    /**
     * Initialisiert die Anzeigen der eigenen Karten.
     *
     * @param ownCards die eigenen Handkarten.
     */
    private void initOwnCardInfo(List<Card> ownCards) {
        //Die Textboxen für die 3 verschiedenen Kartentypen holen
        List<String> characters = new ArrayList<>();
        List<String> weapons = new ArrayList<>();
        List<String> rooms = new ArrayList<>();
        Card.splitCards(ownCards, characters, weapons, rooms);
        listViewCharacters.setItems(FXCollections.observableList(characters));
        listViewWeapons.setItems(FXCollections.observableList(weapons));
        listViewRooms.setItems(FXCollections.observableList(rooms));
    }

    /**
     * Holt ein JavaFX Element aus einem GridPane.
     *
     * @param row die Zeile.
     * @param col die Spalte.
     * @return das JavaFX Element an der Stelle.
     */
    private Node getNodeByRowAndCol(int row, int col) {
        Node res = null;
        ObservableList<Node> children = this.gridPaneNotes.getChildren();
        boolean found = false;
        for (Node node : children) {
            Integer currRow = GridPane.getRowIndex(node);
            Integer currCol = GridPane.getColumnIndex(node);
            if (!found && currRow != null && currCol != null && currRow == row && currCol == col) {
                res = node;
                found = true;
            }
        }
        return res;
    }

    /**
     * Befüllt die Beschreibung der Notizen aus der Initialisierungsdatei.
     * Geht davon aus, dass die Anzahl der Notizen zur GUI passt.
     */
    private void initNoteDescription() {
        InitialCharacterJSON[] players = this.initialGameDataJSON.getPlayers();
        String[] weapons = this.initialGameDataJSON.getWeapons();
        InitialRoomJSON[] rooms = this.initialGameDataJSON.getRooms();
        int playerIndex = 0;
        int weaponIndex = 0;
        int roomIndex = 0;
        //Läuft die erste Spalte ab und befüllt diese
        for (int currRow = NOTES_START_ROW_INDEX; currRow <= NOTES_END_ROW_INDEX; currRow++) {
            if (currRow < NOTES_CHARACTER_AND_WEAPON_DIVIDER_ROW_INDEX) {
                Label currLabel = (Label) getNodeByRowAndCol(currRow, 0);
                currLabel.setText(players[playerIndex].getName());
                playerIndex++;
            }
            if (currRow > NOTES_CHARACTER_AND_WEAPON_DIVIDER_ROW_INDEX
                    && currRow < NOTES_WEAPON_AND_ROOM_DIVIDER_ROW_INDEX) {
                Label currLabel = (Label) getNodeByRowAndCol(currRow, 0);
                currLabel.setText(weapons[weaponIndex]);
                weaponIndex++;
            }
            if (currRow > NOTES_WEAPON_AND_ROOM_DIVIDER_ROW_INDEX) {
                Label currLabel = (Label) getNodeByRowAndCol(currRow, 0);
                currLabel.setText(rooms[roomIndex].getName());
                roomIndex++;
            }
        }
        //Läuft die erste Zeile ab und befüllt diese
        for (int currCol = NOTES_START_COL_INDEX; currCol <= NOTES_END_COL_INDEX; currCol++) {
            Label currLabel = (Label) getNodeByRowAndCol(0, currCol);
            currLabel.setText(players[currCol - 1].getName());
        }
    }

    /**
     * Hilfsmethode um die Zeilenindices der GUI in die Indices der Notizen
     * zu konvertieren.
     *
     * @param gridRow der Zeilenindice in den Notizen der GUI.
     * @return der Zeilenindice der Notizen der Spieler.
     */
    private static int mapGridpaneIndexToNoteIndex(int gridRow) {
        switch (gridRow) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return gridRow - 1;
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
                return gridRow - 2;
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
                return gridRow - 3;
            default:
                throw new RuntimeException("Existiert nicht im Grid");
        }
    }


    /**
     * Initialisiert die Notizen in der GUI mit den geladenen Notizen des menschlichen Spielers.
     *
     * @param self der menschliche Spieler.
     */
    private void initNotes(Player self) {
        int noteSelfCounter = 0;
        for (int currCol = NOTES_START_COL_INDEX; currCol <= NOTES_END_COL_INDEX; currCol++) {
            for (int currRow = NOTES_START_ROW_INDEX; currRow <= NOTES_END_ROW_INDEX; currRow++) {
                if (currRow != NOTES_CHARACTER_AND_WEAPON_DIVIDER_ROW_INDEX
                        && currRow != NOTES_WEAPON_AND_ROOM_DIVIDER_ROW_INDEX) {
                    if (currCol == NOTES_SELF_COL_INDEX) {
                        //Ist okay
                        ComboBox<String> currBox = (ComboBox<String>) getNodeByRowAndCol(currRow, currCol);
                        List<String> noteSelfStringValues = Arrays.stream(NoteSelf.values())
                                .map(JavaFXGUI::noteSelfToString)
                                .collect(Collectors.toList());
                        currBox.getItems().addAll(noteSelfStringValues);

                        //currRow und currCol zu NoteSelf indices umrechnen
                        int finalCurrRow = currRow;
                        currBox.setOnAction(actionEvent -> {
                            String noteStr = currBox.getValue();
                            if (noteStr != null) { //Beim clear wird ein event gefeuert
                                NoteSelf note = StringToNoteSelf(noteStr);
                                int noteSelfIndex = mapGridpaneIndexToNoteIndex(finalCurrRow);
                                NoteSelf[] noteSelf = logic.getPlayers()[0].getNoteSelf();
                                noteSelf[noteSelfIndex] = note;
                            }
                        });

                        //Felder mit den Karten die bereits auf der Hand liegen initialisieren
                        currBox.setValue(noteSelfToString(self.getNoteSelf()[noteSelfCounter]));
                        noteSelfCounter++;
                        //Notizen der Anderen Spieler
                    } else {
                        //Ist okay
                        ComboBox<String> currBox = (ComboBox<String>) getNodeByRowAndCol(currRow, currCol);
                        List<String> noteOthersStringValues = Arrays.stream(NoteOthers.values())
                                .map(JavaFXGUI::noteOthersToString)
                                .collect(Collectors.toList());
                        currBox.getItems().addAll(noteOthersStringValues);


                        NoteOthers[][] noteOthers = self.getNoteOthers();
                        int noteOtherCardIndex = mapGridpaneIndexToNoteIndex(currRow);
                        int noteOtherCharacterIndex = currCol - 2;
                        currBox.setOnAction(actionEvent -> {
                            String value = currBox.getValue();
                            if (value != null) {
                                NoteOthers currNoteChange = stringToNoteOthers(value);
                                noteOthers[noteOtherCharacterIndex][noteOtherCardIndex] = currNoteChange;
                            }
                        });
                        NoteOthers currNodeNoteOthers = noteOthers[noteOtherCharacterIndex][noteOtherCardIndex];
                        String valueOfCurrNode = noteOthersToString(currNodeNoteOthers);
                        currBox.setValue(valueOfCurrNode);

                    }
                }
            }
        }
    }

    /**
     * Initialisiert die Waffen im Spiel auf der GUI.
     */
    private void initWeapons() {
        //Waffen holen
        Weapon[] allWeapons = logic.getWeapons();
        Room[] weaponInRooms = logic.getWeaponInRooms();
        //Waffe zu Raum zuordnen
        for (int i = 0; i < allWeapons.length; i++) {
            Label initWeaponLabel = new Label(logic.getWeapons()[i].getName());
            initWeaponLabel.setMouseTransparent(true);
            initWeaponLabel.setStyle("-fx-background-color: white; -fx-text-fill: red");
            this.weaponsGrp.getChildren().add(initWeaponLabel);
            weaponLables[i] = initWeaponLabel;

            weaponLables[i].setText(allWeapons[i].getName());
            //Waffe an eine Stelle in dem Raum schreiben
            setLabelPosition(weaponLables[i], i, weaponInRooms[i]);
        }
    }

    /**
     * Lädt die Bilder des Würfels.
     */
    private void loadImages() {
        this.diceImages = new Image[DICE_FACES];
        this.diceImages[0] = new Image(this.getClass().getResourceAsStream("assets/dice_one_200px.png"));
        this.diceImages[1] = new Image(this.getClass().getResourceAsStream("assets/dice_two_200px.png"));
        this.diceImages[2] = new Image(this.getClass().getResourceAsStream("assets/dice_three_200px.png"));
        this.diceImages[3] = new Image(this.getClass().getResourceAsStream("assets/dice_four_200px.png"));
        this.diceImages[4] = new Image(this.getClass().getResourceAsStream("assets/dice_five_200px.png"));
        this.diceImages[5] = new Image(this.getClass().getResourceAsStream("assets/dice_six_200px.png"));
    }


    /**
     * Rechnet Klickkoordinaten auf das Spielfeld in Spielfeldkoordinaten um.
     *
     * @param x x-Koordinate des Klicks
     * @param y y-Koordinate des Klicks
     * @return Die Spielfeldkoordinate.
     */
    private Position calcCellPosition(double x, double y) {
        //Aktuelle größe vom Bild
        double currImgWidth = imgViewGameField.boundsInParentProperty().get().getWidth();
        double currImgHeight = imgViewGameField.boundsInParentProperty().get().getHeight();

        int xPos = (int) Math.floor((x - OFFSET_PERCENTAGE_LEFT * currImgWidth) / (CELL_WIDTH_PERCENTAGE * currImgWidth));
        int yPos = (int) Math.floor((y - OFFSET_PERCENTAGE_TOP * currImgHeight) / (CELL_HEIGHT_PERCENTAGE * currImgHeight));
        return new Position(xPos, yPos);
    }


    /**
     * Berechnet die prozentuale Position einer Dimension innerhalb des aktuellen Spielfeldbildes.
     * Notwendig, da skalierbar.
     *
     * @param pos              die Position in Spielfeldzellen
     * @param offsetPercentage der Abstand zu dem Rand in Pixeln
     * @param cellPercentage   die Ausdehnung einer Spielfeldzelle in Pixeln
     * @return Die Prozentuale Position.
     */
    private double calcPositionPercentage(double pos, double offsetPercentage, double cellPercentage) {
        return offsetPercentage + (pos + 0.5) * cellPercentage;
    }

    /**
     * Setzt die Position eines Kreises.
     *
     * @param circle   der Kreis.
     * @param position die Porition an die der Kreis gesetzt werden soll. (Spielfeldzellen)
     */
    private void setCirclePosition(Circle circle, Position position) {
        int x = position.getX();
        int y = position.getY();
        setCirclePosition(circle, x, y);
    }

    @Override
    public void drawCharacterOnCorridor(Character character, int characterIndex) {
        Circle circle = this.characterCircles[characterIndex];
        this.clearPossibleMoves();
        animateTo(circle, character.getPosition());
    }

    /**
     * Animiert den Kreis an einen Position. (Spielfeldzellen)
     *
     * @param circle   Der zu bewegende Kreis.
     * @param position die Zielposition.
     */
    private void animateTo(Circle circle, Position position) {
        animateTo(circle, position.getX(), position.getY());
    }

    /**
     * Animiert den Kreis an einen Position. (Spielfeldzellen)
     *
     * @param circle Der zu bewegende Kreis.
     * @param x      x-Zielposition in Bildkoordinaten
     * @param y      y-Zielposition in Bildkoordinaten
     */
    private void animateTo(Circle circle, double x, double y) {
        double prevX = circle.getCenterX();
        double prevY = circle.getCenterY();
        setCirclePosition(circle, x, y);
        double currX = circle.getCenterX();
        double currY = circle.getCenterY();
        animateTo(circle, prevX, prevY, currX, currY);
    }

    /**
     * Animiert ein Label an einen Position. (Spielfeldzellen)
     *
     * @param label      das zu animierende Label
     * @param labelIndex der Index des Lables in dem globalen Labels Array
     * @param room       der Raum zu dem das Label animiert werden soll.
     */
    private void animateTo(Label label, int labelIndex, Room room) {
        double prevX = label.getLayoutX();
        double prevY = label.getLayoutY();
        setLabelPosition(label, labelIndex, room);
        double currX = label.getLayoutX();
        double currY = label.getLayoutY();
        animateTo(label, prevX, prevY, currX, currY);
    }

    /**
     * Animiert ein JavaFX Element von der Ausgangsposition zu der Zielposition.
     *
     * @param node das zu animierende JavaFX Element
     * @param srcX x-Koord. der Ausgangsposition
     * @param srcY y-Koord. der Ausgangsposition
     * @param dstX x-Koord. der Zielposition
     * @param dstY y-Koord. der Zielposition
     */
    private void animateTo(Node node, double srcX, double srcY, double dstX, double dstY) {
        if (!equalsDouble(srcX, dstX) || !equalsDouble(srcY, dstY)) {
            TranslateTransition transition = new TranslateTransition();
            transition.setDuration(Duration.seconds(ANIMATION_TIME_SECONDS));
            transition.setFromX(srcX - dstX);
            transition.setFromY(srcY - dstY);
            transition.setToX(0.0);
            transition.setToY(0.0);
            transition.setNode(node);

            transition.play();
            //Wenn die Animation am Ende ist (onFinished), soll der asyncGui mitgeteilt werden, dass sie weitermachen kann.
            transition.setOnFinished(asyncGui.makeFinishedHandler());
        }
    }

    /**
     * Vergleicht 2 Doubles auf gleichheit.
     *
     * @param a der eine Double Wert.
     * @param b der andere Double Wert.
     * @return ob diese gleich sind oder nicht.
     */
    private static boolean equalsDouble(double a, double b) {
        return Math.abs(a - b) < DOUBLE_EPSILON;
    }

    /**
     * Setzt ein Kreis an die Übergebene Spielfeldposition.
     *
     * @param x            x-Spielfeldposition (Zellen)
     * @param y            y-Spielfeldposition (Zellen)
     * @param playerCircle der zu setztende Kreis.
     */
    private void setCirclePosition(Circle playerCircle, double x, double y) {
        double xPercentage = calcPositionPercentage(x, OFFSET_PERCENTAGE_LEFT, CELL_WIDTH_PERCENTAGE);
        double yPercentage = calcPositionPercentage(y, OFFSET_PERCENTAGE_TOP, CELL_HEIGHT_PERCENTAGE);
        playerCircle.centerXProperty().bind(this.currImageWidth.multiply(xPercentage));
        playerCircle.centerYProperty().bind(this.currImageHeight.multiply(yPercentage));
    }

    @Override
    public void drawCharacterInRoom(Character character, int characterIndex) {
        this.clearPossibleMoves();
        int playerCount = this.characterCircles.length;
        double yOffset = ((-(playerCount - 1.0) / 2.0) + characterIndex) * CHARACTER_CIRCLE_GAP_FAKTOR + 0.5;
        double xOffset = 1.0;
        animateTo(this.characterCircles[characterIndex], character.getPosition().getX() - xOffset, character.getPosition().getY() + yOffset);
    }

    @Override
    public void setWeapon(Room room, Weapon weapon) throws CluedoException {
        int labelIndex = logic.getWeaponIndexByName(weapon.getName());
        Label labelToSet = weaponLables[labelIndex];
        animateTo(labelToSet, labelIndex, room);
    }

    /**
     * Setzt die Position eines Labels in einen Raum.
     *
     * @param label      der zu setzende Label.
     * @param labelIndex der Index des Labels in dem Waffenarray.
     * @param room       der Raum an den das Label gesetzt werden soll.
     */
    private void setLabelPosition(Label label, int labelIndex, Room room) {
        int labelCount = this.weaponLables.length;

        double yOffset = ((-(labelCount - 1.0) / 2.0) + labelIndex * WEAPON_LABEL_GAP_FAKTOR) + 1.0;

        double xPercentage = calcPositionPercentage(room.getMidPoint().getX(), OFFSET_PERCENTAGE_LEFT, CELL_WIDTH_PERCENTAGE);
        double yPercentage = calcPositionPercentage(room.getMidPoint().getY() + yOffset, OFFSET_PERCENTAGE_TOP, CELL_HEIGHT_PERCENTAGE);
        label.layoutXProperty().bind(this.currImageWidth.multiply(xPercentage));
        label.layoutYProperty().bind(this.currImageHeight.multiply(yPercentage));
    }

    @Override
    public void redrawGUI() {
        clearGUI();
        try {
            resetGUI(logic.getPlayers(), logic.getCharacters());
        } catch (CluedoException e) {
            handleException(e);
        }
    }

    /**
     * Initialisiert die Kreise in dem Spielerfigurenarray
     *
     * @param characters die Spielfiguren
     */
    private void initCircles(Character[] characters) {
        for (int i = 0; i < characters.length; i++) {
            this.characterCircles[i] = new Circle(CHARACTER_CIRCLE_SIZE);
            this.characterCircles[i].setMouseTransparent(true);
            this.characterCircles[i].setFill(JavaFXGUI.getColorFromCurrentPlayerIndex(i));
            setCirclePosition(this.characterCircles[i], characters[i].getPosition());
            this.characterCircles[i].toFront();
        }
        this.paneGameFieldWrapper.getChildren().addAll(Arrays.asList(characterCircles));
    }

    /**
     * Kümmert sich um einen Klick auf das Spielfeld.
     *
     * @param mouseEvent der Mausklick.
     */
    public void handleMouseClick(MouseEvent mouseEvent) {
        if (guiClickable) {
            //Position des clicks
            Position gameCellPosition = this.calcCellPosition(mouseEvent.getX(), mouseEvent.getY());
            if (this.logic.isValidPosition(gameCellPosition)) {
                asyncLogic.makeMove(gameCellPosition);
            }
        }
    }

    @Override
    public void updateDice(int dice) {
        assert dice >= 1;
        assert dice <= DICE_FACES;
        this.imgViewDice.setImage(this.diceImages[dice - 1]);
    }

    /**
     * Kümmert sich um die Exceptions, die im gesamten Spiel auftreten können.
     * Gibt einen Fehlerdialog mit passendem Fehlertext aus und beendet, je nach Fehler, das Spiel.
     *
     * @param e Die Aufgetretene Exception.
     */
    public void handleException(CluedoException e) {
        e.printStackTrace();
        boolean criticalError = false;
        StringBuilder alertText = new StringBuilder();
        ExceptionType type = e.getType();
        switch (type) {
            case InitialGameDataNotFound:
                alertText.append("Die Initialisierungsdatei wurde an der Stelle: ");
                alertText.append(System.lineSeparator());
                alertText.append(e.getPath());
                alertText.append(System.lineSeparator());
                alertText.append(" nicht gefunden!");
                criticalError = true;
                break;
            case InitialGameDataIOException:
                alertText.append("Beim laden der initialisierungsdatei ist ein Fehler aufgetreten");
                criticalError = true;
                break;
            case InitialGameDataSyntaxException:
                alertText.append("Beim laden der initialisierungsdatei ist ein Syntaxfehler aufgetreten");
                criticalError = true;
                break;
            case IllegalNoteOthersInSavedGame:
                alertText.append("Ungültiger Wert bei den Notizen über andere Spieler");
                break;
            case IllegalNoteSelfInSavedGame:
                alertText.append("Ungültiger Wert in eigenen Notizen");
                break;
            case CanceledAtGameStart:
                alertText.append("Das Spiel benötigt diese Informationen zum Starten");
                criticalError = true;
                break;
            case WeaponsInRoomsLength:
                alertText.append("In der Spielstandsdatei sind mehr Waffen als bei der Initialisierung des Spiels");
                break;
            case WeaponNameNotFound:
                alertText.append("Der Name der Waffe existiert nicht");
                break;
            case RoomNameNotFound:
                alertText.append("Der Name des Raumes existiert nicht");
                break;
            case CharacterNameNotFound:
                alertText.append("Der Name der Person existiert nicht");
                break;
            case AIDifficultyNotFound:
                alertText.append("Der Schwierigkeitsgrad der KI existiert nicht");
                break;
            case PlayerToLoadInWrongRoom:
                alertText.append("Die Spielerposition stimmt nicht mit dem Raum überein");
                break;
            case PlayerToLoadNotInRoomCenter:
                alertText.append("Der Spieler befindet sich nicht in der Mitte des Raums");
                break;
            case PlayerToLoadInWall:
                alertText.append("Die Spielerposition ist in einer Wand");
                break;
            case CardNameNotFound:
                alertText.append("Der Name der Karte existiert nicht");
                break;
            case NoteSelfLength:
                alertText.append("Die länge der eigenen Notizen stimmt nicht");
                break;
            case NoteOthersLength:
                alertText.append("Die länge der Notizen über andere Spieler stimmt nicht");
                break;
            case NoteOthersWrongFormat:
                alertText.append("Fehler beim laden der Eigenen Notizen der Spieler");
                break;
            case FileNotFound:
                alertText.append("Die Datei wurde nicht gefunden");
                break;
            case InvalidJSON:
                alertText.append("Die Datei enthält Syntaxfehler");
                break;
            case WritingError:
                alertText.append("Fehler beim schreiben des Spielstandes");
                break;
            case PlayerNameNotFound:
                alertText.append("Der Name des Spielers exisitert nicht");
                break;
            case PlayerNotFound:
                alertText.append("Der Spieler wurde nicht gefunden");
                break;
            case WeaponNotFound:
                alertText.append("Die Waffe wurde nicht gefunden");
                break;
            case CharacterNotFound:
                alertText.append("Die Person wurde nicht gefunden");
                break;
            case RequestedButNotInRoom:
                alertText.append("Der Spieler wurde in einen Raum gewünscht, befindet sich aber nicht in einem Raum");
                break;
            case NullInField:
                alertText.append("In der Spielstandsdatei steht null oder es fehlt ein Feld");
                break;
            case PlayerToLoadRequestedButNotInRoom:
                alertText.append("Der Spieler wurde gewünscht, steht aber nicht in einem Raum");
                break;
            case RestartGame: //Zum neustarten des Spieles -> Unterbrechen
                return;
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehlerdialog");
        alert.setHeaderText("Ein Fehler ist aufgetreten");
        alert.setContentText(alertText.toString());
        alert.showAndWait();

        if (criticalError) {
            stage.close();
        }

    }

    /**
     * Baut und zeigt einen Dialog der den Spieler 3 verschiedene Kartentypen auswählen lässt.
     * Gibt die drei ausgewählten Karten verschiedenen Typs wieder zurück.
     *
     * @param enteredRoom  der betretene Raum, falls es eine Verdächtigung ist. Bei Anklage null.
     * @param title        Der Titel des Dialogs.
     * @param headerText   Die Kopfzeile des Dialogs.
     * @param isCancelable Ob der Dialog schließbar sein soll oder nicht.
     * @return Drei ausgewählten Karten verschiedenen Kartentyps (Waffe,Raum,Person)
     */
    private Optional<CardTriple> chooseCardTripleDialog(Card enteredRoom, String title, String headerText, boolean isCancelable) {
        Dialog<CardTriple> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);

        //Um zu verhindern, dass der Spieler das Fenster schießen kann ohne den verdacht zu bestätigen
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        if (!isCancelable) {
            stage.initStyle(StageStyle.UNDECORATED);
        }
        // Set the button types.
        ButtonType confirmButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(confirmButton);

        ButtonType cancelButton;
        if (isCancelable) {
            cancelButton = new ButtonType("Abbrechen", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(cancelButton);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Card[] allCards = logic.getCards();
        List<Card> allCharacters = new ArrayList<>();
        List<Card> allWeapons = new ArrayList<>();
        List<Card> allRooms = new ArrayList<>();

        //Karten nach Typen aufteilen
        for (Card card : allCards) {
            switch (card.getType()) {
                case CHARACTER:
                    allCharacters.add(card);
                    break;
                case ROOM:
                    allRooms.add(card);
                    break;
                case WEAPON:
                    allWeapons.add(card);
                    break;
            }
        }
        //Konvertiert die Karte zu einem String ohne toSting()
        StringConverter<Card> converter = new StringConverter<>() {
            @Override
            public String toString(Card card) {
                return card.getName();
            }

            @Override
            public Card fromString(String s) {
                throw new RuntimeException("Nicht notw. da keine Freitextauswahl");
            }
        };

        ComboBox<Card> characterBox = new ComboBox<>();
        characterBox.setConverter(converter);
        //ComboBoxen mit den Werten aller mögl. Karten befüllen
        //Character
        for (Card character : allCharacters) {
            characterBox.getItems().add(character);
        }

        //Weapon
        ComboBox<Card> weaponBox = new ComboBox<>();
        weaponBox.setConverter(converter);
        for (Card weapon : allWeapons) {
            weaponBox.getItems().add(weapon);
        }

        //Room
        ComboBox<Card> roomBox = new ComboBox<>();
        roomBox.setConverter(converter);
        for (Card room : allRooms) {
            roomBox.getItems().add(room);
        }
        //Default Werte setzen
        characterBox.setValue(allCharacters.get(0));
        weaponBox.setValue(allWeapons.get(0));
        if (enteredRoom == null) {
            roomBox.setValue(allRooms.get(0));
        } else { //Es wurde ein Raum betreten
            roomBox.setValue(enteredRoom);
            roomBox.setDisable(true);
        }


        grid.add(new Label("Person:"), 0, 0);
        grid.add(characterBox, 1, 0);
        grid.add(new Label("Tatwaffe:"), 0, 1);
        grid.add(weaponBox, 1, 1);
        grid.add(new Label("Raum:"), 0, 2);
        grid.add(roomBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButton) {
                return new CardTriple(roomBox.getValue(), characterBox.getValue(), weaponBox.getValue());
            } else {
                return null;
            }
        });

        stage.setOnCloseRequest(Event::consume);

        return dialog.showAndWait();
    }

    /**
     * Zeigt den Dialog, falls eine ungültige Zielposition gewählt wurde.
     */
    public void showIllegalStepMessage() {
        StringBuilder msg = new StringBuilder();
        msg.append("Kein valider Schritt!");
        alertDialogModal("Cluedo", msg);
    }


    @Override
    public void drawPossibleMoves(Set<Position> possibleMoves) {
        //Neue Zeichnen
        for (Position p : possibleMoves) {
            Circle temp = new Circle(CHARACTER_CIRCLE_SIZE);
            double xPercentage = calcPositionPercentage(p.getX(), OFFSET_PERCENTAGE_LEFT, CELL_WIDTH_PERCENTAGE);
            double yPercentage = calcPositionPercentage(p.getY(), OFFSET_PERCENTAGE_TOP, CELL_HEIGHT_PERCENTAGE);
            temp.centerXProperty().bind(this.currImageWidth.multiply(xPercentage));
            temp.centerYProperty().bind(this.currImageHeight.multiply(yPercentage));
            temp.setFill(null);
            temp.setStroke(Color.BLACK);
            temp.setPickOnBounds(false);
            temp.setStrokeWidth(POSSIBLE_MOVES_CIRCLE_STROKE_WIDTH);
            temp.setMouseTransparent(true);
            this.possibleMovesGrp.getChildren().add(temp);
        }
    }

    @Override
    public void clearPossibleMoves() {
        //Guppe von alten Kreisen befreien
        this.possibleMovesGrp.getChildren().clear();
    }

    /**
     * Hilsfmethode welche drei Karten zu einem String baut.
     *
     * @param cardTriple die drei Karten.
     * @return der String mit den Karten.
     */
    private String cardTripleToString(CardTriple cardTriple) {
        StringBuilder result = new StringBuilder();
        result.append(cardTriple.getCharacter().getName());
        result.append(", ");
        result.append(cardTriple.getWeapon().getName());
        result.append(", ");
        result.append(cardTriple.getRoom().getName());
        return result.toString();
    }

    /**
     * Kümmert sich um das Fenster, welches dem menschlichen Spieler zeigt,
     * welche Karten ihm von wem auf einen Verdacht hin gezeigt wurden.
     *
     * @param allPlayers alle Spieler im Spiel.
     * @param shownByAI  die Karten welche von den KIs gezeigt wurden.
     * @param suspicion  die geäußerte Verdächtigung.
     * @throws CluedoException falls ein Fehler auftritt.
     */
    public void handleOwnSuspicionResult(Player[] allPlayers, Card[] shownByAI, CardTriple suspicion) throws CluedoException {
        StringBuilder ownSuspicionResult = new StringBuilder();
        ownSuspicionResult.append("Dir wurde gezeigt:\n");
        //Einer weniger, da nur die Karten der anderen vorhanden
        for (int shownByAIIndex = 0; shownByAIIndex < shownByAI.length; shownByAIIndex++) {
            //+1 da an 0-ter stelle der Menschl. Spieler steht
            ownSuspicionResult.append(allPlayers[shownByAIIndex + 1].getCharacter().getName());
            ownSuspicionResult.append(" hat ");
            if (shownByAI[shownByAIIndex] == null) {
                ownSuspicionResult.append("Nichts");
            } else {
                ownSuspicionResult.append(shownByAI[shownByAIIndex].getName());
            }
            ownSuspicionResult.append(" gezeigt\n");
        }
        ownSuspicionResult.append("\nDein Verdacht lautete:\n");
        ownSuspicionResult.append(cardTripleToString(suspicion));
        suspicionResultDialog("Ergebnis deiner Verdächtigung", ownSuspicionResult);
    }

    @Override
    public void handleOthersSuspicionResult(Player[] allPlayers, Player currentPlayer, Card[] shownCards, CardTriple suspicion) throws CluedoException {
        //Bekommt die Gezeigten karten aber zeigt nur an ob == null oder nicht.
        StringBuilder othersSuspicionResult = new StringBuilder();
        othersSuspicionResult.append("Du hast ");
        if (shownCards[0] != null) {
            othersSuspicionResult.append(shownCards[0].getName());
        } else {
            othersSuspicionResult.append("nichts");
        }
        othersSuspicionResult.append(" gezeigt\n");
        int shownCardsIndex = 1;
        for (int currAIPlayerIndex = 1; currAIPlayerIndex < allPlayers.length; currAIPlayerIndex++) {
            Player currentAIPlayer = allPlayers[currAIPlayerIndex];
            if (!currentPlayer.equals(currentAIPlayer)) {
                othersSuspicionResult.append(currentAIPlayer.getCharacter().getName());
                othersSuspicionResult.append(" hat ");
                if (shownCards[shownCardsIndex] != null) {
                    othersSuspicionResult.append("eine Karte");
                } else {
                    othersSuspicionResult.append("nichts");
                }
                othersSuspicionResult.append(" gezeigt\n");
                shownCardsIndex++;
            }
        }
        othersSuspicionResult.append("\nDer Verdacht lautete:\n");
        othersSuspicionResult.append(cardTripleToString(suspicion));
        suspicionResultDialog("Ergebnis einer Verdächtigung", othersSuspicionResult);

    }

    /**
     * Kümmert sich um den Dialog, wenn die KI eine Verdächtigung geäußert hat.
     *
     * @param playerName der Name des KI-Spielers, der die Verdächtigung geäußert hat.
     * @param suspicion  die geäußerte Verdächtigung.
     */
    public void handleAISuspicion(String playerName, CardTriple suspicion) {
        StringBuilder aiSuspicion = new StringBuilder();
        aiSuspicion.append(playerName);
        aiSuspicion.append(" hat die Verdächtigung:\n");
        aiSuspicion.append(cardTripleToString(suspicion));
        aiSuspicion.append(" geäußert.");
        alertDialogModal("Verdächtigung", aiSuspicion);
    }

    /**
     * Zeigt das Ergebniss einer Verdächtigungsrunde auf der GUI.
     *
     * @param title   der Titel des Dialogs.
     * @param message die Nachricht, welche im Dialog angezeigt wird.
     * @throws CluedoException Falls ein Fehler auftritt.
     */
    private void suspicionResultDialog(String title, StringBuilder message) throws CluedoException {
        disableClicks();
        btnAccusation.setDisable(false); //Aber Anklage muss trotzdem möglich sein!
        //Und die Buttons ausgraut
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true); //Damit es immer sichtbar ist auch wenn der Spieler Notizen einträgt
        alert.initModality(Modality.NONE);//Damit hintergrund Klickbar
        //Damit der Minimize Button nicht sichtbar ist -> Sieht anders aus als das Modal aus aber was solls
        stage.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message.toString());

        this.suspicionResultDialog = alert;
        alert.showAndWait();
        enableClicks();
        if (this.suspicionResultDialog == null) {
            throw new CluedoException(ExceptionType.RestartGame);
        } else {
            this.suspicionResultDialog = null;
        }

    }

    /**
     * Baut ein Dialog, der nichts anderes klickbar macht.
     *
     * @param title   der Titel des Dialogs.
     * @param message die Nachricht, welche im Dialog angezeigt wird.
     */
    private void alertDialogModal(String title, StringBuilder message) {
        alertDialogModal(title, message, true);
    }

    /**
     * Baut einen Dialog.
     *
     * @param title    der Titel des Dialogs.
     * @param message  die Nachricht, welche im Dialog angezeigt wird.
     * @param blocking ob klicks auf das Hauptfenster geblockt werden oder nicht.
     * @return den Alertdialog.
     */
    private Alert alertDialogModal(String title, StringBuilder message, boolean blocking) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message.toString());
        if (blocking) {
            alert.showAndWait();
        } else {
            alert.show();
        }
        return alert;
    }

    @Override
    public Card handleShowCard(CardTriple suspicion, CardTriple possibleCardsToShow) {
        ShowCardsDialog showCardsDialog = new ShowCardsDialog(stage,
                suspicion,
                possibleCardsToShow);

        Optional<Card> res = showCardsDialog.showDialog();

        return res.orElse(null); //Kann null sein, falls keine Karte auf der Hand.
    }

    @Override
    public void handleGameWon(CardTriple solution, Player winner) {
        StringBuilder message = new StringBuilder();
        message.append(winner.getCharacter().getName());
        message.append(" hat Gewonnen!\n");
        message.append("Die Lösung lautet:\n");
        message.append(cardTripleToString(solution));
        this.closeSuspicionResultDialog();
        Alert alert = alertDialogModal("Der Mord wurde gelöst!", message, false);
        alert.setOnHidden(dialogEvent -> handleRestartGame());
    }

    @Override
    public void handleGameLost(CardTriple wrongSolution, CardTriple solution, Player loser) {
        StringBuilder message = new StringBuilder();
        message.append(loser.getCharacter().getName());
        message.append(" hat verloren\n");
        message.append("Die Anklage lautete:\n");
        message.append(cardTripleToString(wrongSolution));
        message.append("\n");
        message.append("Die Lösung wäre:\n");
        message.append(cardTripleToString(solution));
        this.closeSuspicionResultDialog();
        Alert alert = alertDialogModal("Falsche Anklage!", message, false);
        alert.setOnHidden(dialogEvent -> handleRestartGame());
    }

    /**
     * Kümmert sich darum die GUI zu bereinigen und ein neues Spiel anzustoßen.
     */
    private void handleRestartGame() {
        clearGUI();
        startNewGame();
    }

    /**
     * Schließt das aktuelle Verdächtigungsergebnissfenster, falls es aktuell noch angezeigt wird.
     */
    private void closeSuspicionResultDialog() {
        if (this.suspicionResultDialog != null) {
            this.suspicionResultDialog.close();
            this.suspicionResultDialog = null;
        }
    }

    /**
     * Der Startdialog, der bei einem neuen Spiel aufgerufen wird.
     * Auswahl von Spieleranzahl und deren KI-stärke.
     *
     * @return Optional<StartGameInfo> falls das Dialogfenster mit dem Confirm-Butten bestätigt wurde.
     */
    private Optional<StartGameInfo> startGameDialog() {
        //3,4,5,6 Spieler sollen möglich sein
        int[] playerCountOptions = new int[MAX_PLAYER_COUNT - MIN_PLAYER_COUNT + 1];
        for (int i = 0; i < playerCountOptions.length; i++) {
            playerCountOptions[i] = MIN_PLAYER_COUNT + i;
        }

        Dialog<StartGameInfo> dialog = new Dialog<>();
        dialog.setTitle("Spielerwahl Cluedo");
        dialog.setHeaderText("Gib die Anzahl und Schwierigkeitsgrad der Mitspieler an!");
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setOnCloseRequest(Event::consume);

        ButtonType confirmButton = new ButtonType("Los Gehts!", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Integer> playerCountBox = new ComboBox<>();
        //Setze Auswahlmöglichkeiten
        for (int i : playerCountOptions) {
            playerCountBox.getItems().add(i);
        }
        //Setzte Defaultauswahl
        playerCountBox.setValue(playerCountOptions[0]);

        StringConverter<AIDifficulty> aiDifficultyBoxconverter = new StringConverter<>() {
            @Override
            public String toString(AIDifficulty aiDifficulty) {
                return JavaFXGUI.aIDifficultyToString(aiDifficulty);
            }

            @Override
            public AIDifficulty fromString(String s) {
                // Nicht notw. da keine Freitextauswahl
                throw new RuntimeException();
            }
        };

        List<ComboBox<AIDifficulty>> playerDifficultyBoxes = new ArrayList<>();
        for (int i = 0; i < MAX_PLAYER_COUNT - 1; i++) {
            ComboBox<AIDifficulty> playerDifficultyBox = new ComboBox<>();
            playerDifficultyBox.setConverter(aiDifficultyBoxconverter);
            playerDifficultyBox.getItems().addAll(Arrays.asList(AIDifficulty.values()));
            //Defaultauswahl setzen
            playerDifficultyBox.setValue(AIDifficulty.STUPID);
            playerDifficultyBoxes.add(playerDifficultyBox);
        }

        grid.add(new Label("Anzahl Spieler:"), 0, 0);
        grid.add(playerCountBox, 1, 0);
        for (int i = 0; i < playerDifficultyBoxes.size(); i++) {
            grid.add(new Label("KI-Spieler " + (i + 2) + ":"), 0, i + 1);
            grid.add(playerDifficultyBoxes.get(i), 1, i + 1);
        }

        //Initiales Ausschalten der Möglichkeiten
        for (int i = playerCountOptions[0] - 1; i < playerDifficultyBoxes.size(); i++) {
            playerDifficultyBoxes.get(i).setDisable(true);
        }

        playerCountBox.addEventHandler(ActionEvent.ACTION, actionEvent -> {
            int currentPlayerCountSelected = playerCountBox.getValue(); //Die ausgewählte Anzahl der Spieler
            //Alle resetten
            for (ComboBox<AIDifficulty> box : playerDifficultyBoxes) {
                box.setDisable(false);
            }
            //Wieder ausgrauen
            for (int i = currentPlayerCountSelected - 1; i < playerDifficultyBoxes.size(); i++) {
                playerDifficultyBoxes.get(i).setDisable(true);
            }
        });

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButton) {
                final int playerCount = playerCountBox.getValue();

                AIDifficulty[] aiDifficulties = new AIDifficulty[playerCount];
                //Der Menschliche Spieler 0 hat keine KI
                aiDifficulties[0] = null;
                for (int i = 0; i < playerCount - 1; i++) {
                    //Über alle ComboBoxen<AIDifficulty> laufen und werte holen
                    aiDifficulties[i + 1] = playerDifficultyBoxes.get(i).getValue();
                }
                return new StartGameInfo(playerCount, aiDifficulties);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * Hilfsmethode, die die GUI für Userinteraktionen sperrt.
     */
    public void disableClicks() {
        btnAccusation.setDisable(true);
        menuItemLoadGame.setDisable(true);
        menuItemNewGame.setDisable(true);
        menuItemSaveGame.setDisable(true);
        this.guiClickable = false;
    }

    /**
     * Hilfsmethode, die die GUI für Userinteraktionen entsperrt.
     */
    public void enableClicks() {
        btnAccusation.setDisable(false);
        menuItemLoadGame.setDisable(false);
        menuItemNewGame.setDisable(false);
        menuItemSaveGame.setDisable(false);
        this.guiClickable = true;
    }

    /**
     * Kümmert sich um den Dialog, falls der User das Laufende Spiel schließen möchte.
     *
     * @return ob der User das Spiel tatsächlich beendet hat.
     */
    private boolean handleExitGame() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Du beendest das gerade laufende Spiel");
        alert.setHeaderText("Möchtest du speichern?");

        ButtonType buttonTypeSave = new ButtonType("Speichern");
        ButtonType buttonTypeNoSave = new ButtonType("Nicht Speichern");
        ButtonType buttonTypeCancel = new ButtonType("Abbrechen", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeSave, buttonTypeNoSave, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() == buttonTypeCancel) {
            return false;
        }
        if (result.get() == buttonTypeSave) {
            return handleSaveGame();
        }
        return true;
    }

    /**
     * Kümmert sich um eine Anklage und um die darauf folgenden Dialoge, je nachdem, ob die Lösung
     * richtig oder falsch war.
     */
    public void handleAccusation() {
        //Null, da es sich nicht um eine Verdächtigung handelt und dafür kein Raum vorasugewählt sein soll
        Optional<CardTriple> accusation = this.chooseCardTripleDialog(null, "Anklage",
                "Gib deine Anklage ein!", true);
        if (accusation.isPresent()) {
            if (logic.checkGameWon(accusation.get())) {
                handleGameWon(accusation.get(), logic.getPlayers()[0]);
            } else {
                handleGameLost(accusation.get(), logic.getEnvelope(), logic.getPlayers()[0]);
            }
        }
    }

    /**
     * Kümmert sich um den Dialog, der den menschl. Spieler eine Verdächtigung aussprechen lässt.
     *
     * @param enteredRoom der betretene Raum (vorauswahl)
     * @return Die gewählte Verdächtigung.
     */
    public CardTriple handleExpressSuspicion(Card enteredRoom) { //Immer wenn Raum betreten wird
        Optional<CardTriple> suspicion = this.chooseCardTripleDialog(enteredRoom, "Verdächtigung",
                "Gib deine Verdächtigung ein!", false);
        if (suspicion.isPresent()) {
            return suspicion.get();
        } else {
            throw new RuntimeException();//Sollte nicht mögl. sein
        }
    }

    /**
     * Hilfsmethode, die ein neues Spiel startet.
     */
    private void startNewGame() {
        try {
            this.startGame();
        } catch (CluedoException e) {
            handleException(e);
        }
    }

    /**
     * Kümmert sich um das starten eines neuen Spiels, falls der User den jew. Menüeintrag auswählt.
     */
    public void handleNewGame() {
        boolean success = handleExitGame();
        if (success) {
            startNewGame();
        }
    }

    /**
     * Kümmert sich um den Dialog, der den User das aktuelle Spiel speichern lässt.
     * Verfahren siehe:
     * https://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file
     *
     * @return ob das Speichern erfolgreich abgeschlossen wurde.
     */
    public boolean handleSaveGame() {
        boolean saveSuccess = false;

        File currDir = null;
        try {
            currDir = new File(FXMLDocumentController.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ignored) {
        }
        FileChooser fileChooser = new FileChooser();
        if (currDir != null) {
            //korrektes Verzeichniss
            fileChooser.setInitialDirectory(currDir.getParentFile());
        }
        fileChooser.setTitle("Speichere dein Spiel");
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("JSON Datei (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);

        //Dialog öffen und Fenster nicht interagierbar machen
        File selectedFile = fileChooser.showSaveDialog(imgViewGameField.getScene().getWindow());
        if (selectedFile != null) {
            String selectedFilePath = selectedFile.getAbsolutePath();
            if (!selectedFilePath.endsWith(".json")) {
                selectedFile = new File(selectedFilePath + ".json");
            }
            asyncLogic.saveGame(selectedFile);
            saveSuccess = true;
        }
        return saveSuccess;

    }

    /**
     * Kümmert sich um den Dialog, der den User ein Spielstand laden lässt.
     * Verfahren siehe:
     * https://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file
     */
    public void handleLoadGame() {
        boolean success = handleExitGame();
        if (!success) {
            return;
        }

        File currDir = null;
        try {
            currDir = new File(FXMLDocumentController.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ignored) {

        }
        FileChooser fileChooser = new FileChooser();
        if (currDir != null) {
            //korrektes Verzeichniss
            fileChooser.setInitialDirectory(currDir.getParentFile());
        }
        fileChooser.setTitle("Lade einen Spielstand");

        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("JSON Datei (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);

        //Dialog öffen und Fenster nicht interagierbar machen
        File selectedFile = fileChooser.showOpenDialog(imgViewGameField.getScene().getWindow());
        if (selectedFile != null) {
            asyncLogic.loadGame(selectedFile, initialGameDataJSON);
        }

    }


    /**
     * Kümmert sich um das aufräumen/enfernen der veränderbaren Elemente der GUI im Spielverlauf.
     */
    private void clearGUI() {
        this.paneGameFieldWrapper.getChildren().clear();
        this.paneGameFieldWrapper.getChildren().add(imgViewGameField);
        this.listViewRooms.getItems().clear();
        this.listViewWeapons.getItems().clear();
        this.listViewCharacters.getItems().clear();
        this.weaponsGrp.getChildren().clear();
        clearNotes();
        enableClicks();
    }

    /**
     * Entfernt den Inhalt der Notizen des menschl. Spielers.
     */
    private void clearNotes() {
        for (int currCol = NOTES_START_COL_INDEX; currCol <= NOTES_END_COL_INDEX; currCol++) {
            for (int currRow = NOTES_START_ROW_INDEX; currRow <= NOTES_END_ROW_INDEX; currRow++) {
                if (currRow != NOTES_CHARACTER_AND_WEAPON_DIVIDER_ROW_INDEX
                        && currRow != NOTES_WEAPON_AND_ROOM_DIVIDER_ROW_INDEX) {
                    //Ist okay
                    ComboBox<String> currNode = (ComboBox<String>) getNodeByRowAndCol(currRow, currCol);
                    currNode.getItems().clear(); //Alles löschen um wieder die init Methode nutzen zu können
                    //clear() feuert ein Action event, daher muss im eventhandler
                    // nach != null geprüft werden da sich zu dem Zeitpunkt nichts
                    // in der Combobox befindet
                }
            }
        }
    }


    /**
     * Konvertiert die eigene Notizen in Strings.
     * (In GUI, da hier die Sprache festgelegt werden soll)
     *
     * @param noteSelf die eigenen Notizen.
     * @return die Notiz als String.
     */
    private static String noteSelfToString(NoteSelf noteSelf) {
        String ret = null;
        switch (noteSelf) {
            case OWN:
                ret = "eigene Karte";
                break;
            case NOTHING:
                ret = "----------";
                break;
            case SHOWN_ONCE:
                ret = "1X gezeigt";
                break;
            case SHOWN_TWICE:
                ret = "2X gezeigt";
                break;
            case SHOWN_THRICE:
                ret = "3X gezeigt";
                break;
            case SHOWN_OVER_TRICE:
                ret = ">3X gezeigt";
                break;
        }
        return ret;
    }

    /**
     * Konvertiert die die Notiz über andere Spieler in Strings.
     * (In GUI, da hier die Sprache festgelegt werden soll)
     *
     * @param noteOthers die Notiz über andere Spieler.
     * @return die Notiz als String.
     */
    private static String noteOthersToString(NoteOthers noteOthers) {
        String ret;
        switch (noteOthers) {
            case NOTHING:
                ret = "----------";
                break;
            case SEEN:
                ret = "gesehen";
                break;
            case HAS_NOT:
                ret = "hat nicht";
                break;
            case SUSPITION_A:
                ret = "Verdacht A";
                break;
            case SUSPITION_B:
                ret = "Verdacht B";
                break;
            case SUSPITION_C:
                ret = "Verdacht C";
                break;
            case SUSPITION_D:
                ret = "Verdacht D";
                break;
            default:
                throw new RuntimeException();//Sollte nicht passieren
        }
        return ret;
    }

    /**
     * Konvertiert die Strings der eigenen Notizen wieder in entsprechende Enumwerte.
     * (In GUI, da hier die Sprache festgelegt werden soll).
     *
     * @param str der zu konvertierende Notizeneintrag.
     * @return der Enumwert des Notizeneintrags
     */
    private static NoteSelf StringToNoteSelf(String str) {
        switch (str) {
            case "eigene Karte":
                return NoteSelf.OWN;
            case "----------":
                return NoteSelf.NOTHING;
            case "1X gezeigt":
                return NoteSelf.SHOWN_ONCE;
            case "2X gezeigt":
                return NoteSelf.SHOWN_TWICE;
            case "3X gezeigt":
                return NoteSelf.SHOWN_THRICE;
            case ">3X gezeigt":
                return NoteSelf.SHOWN_OVER_TRICE;
            default:
                throw new IllegalArgumentException("So ein String gibt es bei den eigenen Notizen nicht");
        }
    }

    /**
     * Konvertiert die Strings der Notizen über andere wieder in entsprechende Enumwerte.
     * (In GUI, da hier die Sprache festgelegt werden soll).
     *
     * @param str der zu konvertierende Notizeneintrag.
     * @return der Enumwert des Notizeneintrags.
     */
    private static NoteOthers stringToNoteOthers(String str) {
        switch (str) {
            case "----------":
                return NoteOthers.NOTHING;
            case "gesehen":
                return NoteOthers.SEEN;
            case "hat nicht":
                return NoteOthers.HAS_NOT;
            case "Verdacht A":
                return NoteOthers.SUSPITION_A;
            case "Verdacht B":
                return NoteOthers.SUSPITION_B;
            case "Verdacht C":
                return NoteOthers.SUSPITION_C;
            case "Verdacht D":
                return NoteOthers.SUSPITION_D;
            default:
                throw new IllegalArgumentException("Kommt nicht vor");
        }
    }


    /**
     * Konvertiert die Schwierigkeitsstufen der KIs in Strings.
     *
     * @param difficulty die KI Schwierigkeitsstufe.
     * @return die Stringrepräsentation.
     */
    private static String aIDifficultyToString(AIDifficulty difficulty) {
        String res;
        switch (difficulty) {
            case STUPID:
                res = "dumm";
                break;
            case NORMAL:
                res = "normal";
                break;
            case SMART:
                res = "schlau";
                break;
            default:
                throw new RuntimeException();
        }
        return res;
    }

    /**
     * Legt die Farben der Spielfiguren fest und liefert die jew. Farbe nach deren Index im
     * Spielfigurenarray.
     *
     * @param currentPlayerIndex der Index im Spielfigurenarray.
     * @return die jew. Farbe.
     */
    private static Color getColorFromCurrentPlayerIndex(int currentPlayerIndex) {
        Color res = null;
        switch (currentPlayerIndex) {
            case 0: //Ming
                res = Color.RED;
                break;
            case 1://Gatow
                res = Color.YELLOW;
                break;
            case 2://Weiß
                res = Color.WHITE;
                break;
            case 3://Grün
                res = Color.GREEN;
                break;
            case 4://Porz
                res = Color.BLUE;
                break;
            case 5://Bloom
                res = Color.PURPLE;
                break;
        }
        return res;
    }

    /**
     * Lädt die Initialdaten aus der JSON Datei.
     * Diese legt alle Spielfiguren, Waffen, Räume (deren Türen und Mittelpunkte), sowie
     * das aktuelle Spielfeld fest.
     * Diese könnte theoretisch angepasst werden und das Spiel ließe sich erweitern, da keine
     * Enums für die Spielelemente benutzt wurden, sondern Klassen. Diese lassen sich über deren Namen
     * identifizieren. Der Name wird aus eben dieser Initialdatei geladen und als Referenz
     * genutzt.
     *
     * @param path der Pfad zu der Initialisierungsdatei.
     * @return die geladene Initialisierungsdatei.
     * @throws CluedoException falls beim laden Fehler auftreten.
     */
    public static InitialGameDataJSON loadInitialGameData(String path) throws CluedoException {
        InputStream data = GameLogic.class.getResourceAsStream(path);
        if (data == null) {
            throw new CluedoException(ExceptionType.InitialGameDataNotFound, path);
        }
        Reader dataReader = new InputStreamReader(data, StandardCharsets.UTF_8);
        Gson gson = new Gson();
        InitialGameDataJSON initialGameDataJSON;
        try {
            initialGameDataJSON = gson.fromJson(dataReader, InitialGameDataJSON.class);
        } catch (JsonIOException e) {
            throw new CluedoException(ExceptionType.InitialGameDataIOException);
        } catch (JsonSyntaxException e) {
            throw new CluedoException(ExceptionType.InitialGameDataSyntaxException);
        }
        return initialGameDataJSON;
    }

    /**
     * Schaltet die Anzeige der mögl. Schritte an und aus.
     *
     * @param toggle der Status der Checkbox auf der GUI.
     */
    public void handleTogglePossibleMoves(boolean toggle) {
        this.possibleMovesGrp.setVisible(toggle);
    }
}
