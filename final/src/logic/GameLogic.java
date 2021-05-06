package logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import logic.exceptions.CluedoException;
import logic.exceptions.ExceptionType;
import logic.json.GameDataJSON;
import logic.json.InitialCharacterJSON;
import logic.json.InitialGameDataJSON;
import logic.json.InitialRoomJSON;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Diese Klasse repräsentiert die Spiellogik von Cluedo.
 * Sie beinhaltet alle für das Spiel notwendigen Informationen, sowie die Spiellogik welche aus den
 * Originalspielregeln und den anpassungen der Aufgabenstellung abgeleitet wurde.
 *
 * @author Michael Smirnov
 */
public class GameLogic {

    //Debugmodus schalter.
    private static final boolean DEBUG_MODE = false;

    //Das Spielfeld
    private final GameCell[][] gameField;

    //Alle im Spiel befindl. Charaktere
    private final Character[] characters;

    //Alle im Spiel befindichen Räume
    private final Room[] rooms;

    //Alle im Spiel befindichen Waffem
    private final Weapon[] weapons;

    //Alle im Spiel befindichen. Karten
    private final Card[] cards;

    //Generator für zufällige Zahlen
    private final Random random = new Random();

    //Der Mord/Die Lösung
    private CardTriple envelope = new CardTriple();

    //Anzahl der Spieler im Spiel.
    private int playerCount;

    //Die Positionen der Waffen
    //Indiziert über Weapon[] um Raum zu erhalten in welchem die Waffe liegt
    private Room[] weaponInRooms;

    //Aktuell gewürfelte Augenzahl
    private int dice;

    //Die GUI
    private GUIConnector gui;

    //Indice des akuellen Spielers
    private int currentPlayerIndex;

    //Alle im Spiel befindichen Spieler
    private Player[] players;


    /**
     * Testkonstruktor für Spielfeld welches vom Standart abweicht.
     *
     * @param gameField    das Spielfeld.
     * @param characters   die Spielfiguren im Spiel.
     * @param difficulties die stärken der KI-Spieler.
     */
    public GameLogic(GameCell[][] gameField, Character[] characters, AIDifficulty[] difficulties) {
        this(new Room[0], characters, null, gameField, new Card[0], characters.length, difficulties);
    }

    /**
     * Testkonstruktor um Spieler zu übergeben die bereits an einer bestimmten Position stehen.
     *
     * @param gameField    das Spielfeld.
     * @param players      die Spieler im Spiel
     * @param characters   die Spielfiguren im Spiel
     * @param difficulties die stärken der KI-Spieler.
     */
    public GameLogic(GameCell[][] gameField, Player[] players, Character[] characters, AIDifficulty[] difficulties) {
        this(new Room[0], characters, null, gameField, new Card[0], players.length, difficulties);
        this.players = players;
    }

    /**
     * Konstruktor der Spiellogik, welcher im Spielverlauf tatsächlich genutzt wird.
     *
     * @param rooms        alle Räume im Spiel.
     * @param characters   alle Personen/Spielfiguren im Spiel.
     * @param weapons      alle Waffen im Spiel.
     * @param gameField    das Spielfeld.
     * @param cards        alle Karten im Spiel.
     * @param playerCount  die Anzahl der Spieler im Spiel
     * @param difficulties die KI-Stärken der mitspieler (bei dem menschl. Spieler an Index 0 steht null.)
     */
    public GameLogic(Room[] rooms, Character[] characters, Weapon[] weapons, GameCell[][] gameField, Card[] cards, int playerCount, AIDifficulty[] difficulties) {
        this.currentPlayerIndex = 0;
        this.rooms = rooms;
        this.characters = characters;
        this.weapons = weapons;
        this.gameField = gameField;
        this.cards = cards;
        this.playerCount = playerCount;
        this.players = new Player[playerCount];
        //Initialisieren der Spieler
        for (int i = 0; i < playerCount; i++) {
            this.players[i] = new Player(characters[i], difficulties[i], characters.length, cards.length);
        }
        prepareGameLogic();
    }

    /**
     * Bereitet die Spiellogik nach dem Laden der notwendigen Informationen aus der
     * Initialisierungsdatei vor.
     * Mord ermitteln, Karten verteilen usw.
     */
    private void prepareGameLogic() {
        List<Card> cardDeck = new ArrayList<>(Arrays.asList(cards));
        Collections.shuffle(cardDeck);
        boolean weaponFound = false;
        boolean characterFound = false;
        boolean roomFound = false;
        for (Card card : cardDeck) {
            if (!weaponFound && card.getType() == CardType.WEAPON) {
                this.envelope.setWeapon(card);
                weaponFound = true;
            }
            if (!characterFound && card.getType() == CardType.CHARACTER) {
                this.envelope.setCharacter(card);
                characterFound = true;
            }
            if (!roomFound && card.getType() == CardType.ROOM) {
                this.envelope.setRoom(card);
                roomFound = true;
            }
        }
        cardDeck.removeAll(envelope.getCards());
        GameLogic.debugln("Die Lösung lautet: " + envelope);

        //Verteilen der Karten reihum an Spieler bis keine mehr vorhanden sind
        Iterator<Card> iterator = cardDeck.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            players[i % playerCount].addCard(iterator.next());
        }

        //Initiales setzen der eigenen Notitzen der Spieler
        for (Player player : players) {
            player.initNoteSelf(this.cards);
        }

        //Vereilt die Waffen auf die Räume
        List<Room> weaponsForRooms = new ArrayList<>(Arrays.asList(rooms));
        Collections.shuffle(weaponsForRooms);
        this.weaponInRooms = weaponsForRooms.toArray(new Room[0]);

        for (Player player : players) {
            GameLogic.debugln("Ich bin {" + player.getCharacter().getName() + "} ich habe {" + player.getCards() + "}");
        }
    }

    /**
     * Initialisiert die Logik mit einer GUI und startet den ersten Zug.
     *
     * @param gui die GUI für das Spiel.
     */
    public void init(GUIConnector gui) {
        this.gui = gui;
        this.startTurn();
    }

    /**
     * Liefert die Spielfeldposition des aktuellen Spielers.
     *
     * @return die Spielfeldposition.
     */
    private Position getCurrentPlayerPosition() {
        return getCurrentPlayer().getPos();
    }

    /**
     * Liefert den aktuellen Spieler am Zug.
     *
     * @return der Spieler am Zug.
     */
    private Player getCurrentPlayer() {
        return this.players[currentPlayerIndex];
    }


    /**
     * Liefert die aktuelle Würfelaugenzahl.
     *
     * @return die aktuelle Würfelaugenzahl.
     */
    private int getDice() {
        return dice;
    }

    /**
     * Würfelt den Würfel.
     */
    private void rollDice() {
        //(max + 1 - min) + min
        this.dice = random.nextInt(6 + 1 - 1) + 1;
    }

    /**
     * Setzt den aktuellen Spieler an eine neue Position.
     *
     * @param newPlayerPos die neue Position.
     */
    public void setCurrentPlayerPosition(Position newPlayerPos) {
        getCurrentPlayer().setPos(newPlayerPos);
    }

    /**
     * Setzt den Index des aktuellen Spielers.
     * (Derjenige der an der Reihe ist.)
     *
     * @param index der index des aktuellen Spielers.
     */
    public void setCurrentPlayerIndex(int index) {
        this.currentPlayerIndex = index;
    }

    /**
     * Setzt den Spieler welcher dran ist weiter.
     */
    private void setNextPlayer() {
        setCurrentPlayerIndex((this.currentPlayerIndex += 1) % this.playerCount);
    }


    /**
     * Lädt ein Spiel aus dem Dateisystem.
     * <p>
     * Anmerkung: Bei dem Laden eine Spielstandes werden alle in einem Raum befindlichen Spieler in
     * den Mittelpunkt des Raumes auf der GUI gezeichnet. Erst nach einem Schritt
     * werden die Figuren wieder korrekt auf der GUI angezeigt.
     *
     * @param file                die Datei aus der der Spielstand geladen werden soll.
     * @param initialGameDataJSON die Initialspieldaten.
     * @throws CluedoException falls beim laden ein Fehler auftritt.
     */
    public void loadGame(File file, InitialGameDataJSON initialGameDataJSON) throws CluedoException {
        Reader r;
        try {
            r = new FileReader(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CluedoException(ExceptionType.FileNotFound, file.getPath());
        }
        Gson gson = new Gson();
        GameDataJSON gameDataJSON;
        try {
            gameDataJSON = gson.fromJson(r, GameDataJSON.class);
        } catch (JsonSyntaxException e) {
            throw new CluedoException(ExceptionType.InvalidJSON, file.getPath());
        }
        //Wenn die Spielstandsdatei nicht korrekt ist soll das vorherige spiel fortgeführt werden

        LoadedGameLogic loadedGameLogic;
        try {
            loadedGameLogic = GameDataConverter.convertToLoadedGameLogic(gameDataJSON, this);
        } catch (CluedoException e) {
            e.setPath(file.getPath());
            gui.handleException(e);
            return;
        }
        //Ausgangspositionen der Charaktere neu laden.
        int playerCount = loadedGameLogic.getPlayerCount();
        Character[] initPositions = initCharacters(initialGameDataJSON);
        //Die Nicht-Spieler-Charactere werden an die Initialpositionen gesetzt
        System.arraycopy(initPositions, playerCount, this.characters, playerCount, initPositions.length - playerCount);
        //Hier muss die logik komplett valide sein
        loadedGameLogic.commit(this);
        this.gui.redrawGUI();
        startTurn(); //Damit nach dem Laden eine neue Würfelzahl erscheint
    }


    /**
     * Kümmert sich um das Speichern des aktuellen Spielstandes
     *
     * @param file die Datei in die der Spielstand geschieben werden soll.
     */
    public void saveGame(File file) throws CluedoException {
        Writer w;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            w = new FileWriter(file, StandardCharsets.UTF_8);
            gson.toJson(GameDataConverter.convertToGameDataJSON(this), w);
            w.close();
        } catch (IOException e) {
            throw new CluedoException(ExceptionType.WritingError, file.getPath());
        }
    }


    /**
     * Entfernt alle Nachbarn, welche nicht innerhalb des Spielfeldes liegen
     *
     * @param positions die Positionen welche überprüft werden sollen.
     * @return die Positionen, welche innerhalb des Spielfeldes liegen.
     */
    private Set<Position> validateNeighbours(Set<Position> positions) {
        int gameFieldHeight = gameField.length;
        int gameFieldWidth = gameField[0].length;
        //Keine Außerhalb des Spielfeldes
        positions.removeIf(p -> p.getX() < 0 || p.getX() >= gameFieldWidth || p.getY() < 0 || p.getY() >= gameFieldHeight);
        //Keine versprerrten
        positions.removeIf(p -> getGameCell(p) != GameCell.CORRIDOR);
        return positions;
    }

    /**
     * Prüft ob der ausgewählte Schritt zulässig/erreichbar ist
     *
     * @param pos   Zielposition
     * @param steps zur Verfügung stehende Schritte
     * @param exact ob die Ziel genau mit den gegebenen Schritten erreichbar sein soll,
     *              oder ob auch weniger Schritte erlaubt sind.
     * @return ob der aktuelle Spieler innerhalb der angegebenen Schrittzahl das übergebene Feld erreichen kann.
     */
    public boolean isCorridorReachable(Position pos, int steps, boolean exact) {
        return generateValidCorridorMovesForCurrentPlayer(steps, exact).contains(pos);
    }

    /**
     * Liefert die erreichbaren Positionen des aktuellen Spielers über den Flur.
     *
     * @param steps die zur Verfügung stehenden Schritte.
     * @param exact ob alle Schritte innerhalb des Zuges verbraucht werden müssen oder nicht.
     * @return die erreichbaren Positionen des aktuellen Spielers.
     */
    private Set<Position> generateValidCorridorMovesForCurrentPlayer(int steps, boolean exact) {
        Set<Position> startPosition = new HashSet<>();
        Position currentPlayerPosition = getCurrentPlayer().getPos();
        //Bei einem Raum muss nicht die Position des Spielers als Startpunkt, sondern
        //die Türen des Raumes genommen werden.
        if (isRoom(currentPlayerPosition)) {
            //Wenn ich in einem Raum bin muss das herausgehen aus der Tür bereits einen Schritt verbrauchen
            //Da die Türen der Räume als VOR den Räumen definiert sind muss ein schritt abgezogen werden
            steps--;
            Position[] currRoomDors = getGameCell(currentPlayerPosition).getRoom().getDoors();
            startPosition.addAll(Arrays.asList(currRoomDors));
            Set<Position> otherPlayers = getOtherPlayerPositions();
            startPosition.removeIf(otherPlayers::contains);
        } else {
            startPosition.add(currentPlayerPosition);
        }
        return generateValidMoves(startPosition, steps, exact);
    }

    /**
     * Liefert gegeben einer Menge an Positionen alle von dort aus erreichbaren Positionen.
     *
     * @param startPositions die gegebenen Startpositionen.
     * @param steps          die zur Verfügung stehenden Schritte
     * @param exact          ob die Ziel genau mit den gegebenen Schritten erreichbar sein soll,
     *                       oder ob auch weniger Schritte erlaubt sind.
     * @return die erreichbaren Positionen.
     */
    private Set<Position> generateValidMoves(Set<Position> startPositions, int steps, boolean exact) {
        Set<Position> working = new HashSet<>(startPositions);
        Set<Position> lastStep = new HashSet<>(working);
        for (int i = 0; i < steps; i++) {
            working.addAll(nextMove(working));
            if (exact) {
                working.removeAll(lastStep);
                lastStep.clear();
                lastStep.addAll(working);
            }
        }
        return working;
    }

    /**
     * Liefert alle Positonen von den anderen Spielern
     *
     * @return eine Menge von den Positonen aller anderen Spieler
     */
    private Set<Position> getOtherPlayerPositions() {
        //Von anderen Spielern belegte Felder
        Set<Position> otherPlayers = new HashSet<>();
        for (Player player : players) {
            if (player != players[this.currentPlayerIndex]) {
                otherPlayers.add(player.getPos());
            }
        }
        return otherPlayers;
    }

    /**
     * Gibt ausgehened von einem Set mit Positionen, alle Positionen an,
     * welche mit einem Schritt erreicht werden können.
     *
     * @param positions das Set mit Ausgangspositionen.
     * @return das Set mit den errreichbaren Positionen
     */
    private Set<Position> nextMove(Set<Position> positions) {
        Set<Position> res = new HashSet<>();
        //Von anderen Spielern belegte Felder
        Set<Position> otherPlayers = getOtherPlayerPositions();
        for (Position p : positions) {
            Set<Position> freePositons = new HashSet<>(this.validateNeighbours(p.getNeighbours(1)));
            freePositons.removeIf(otherPlayers::contains); //Von Spielerpos bereinigt
            res.addAll(freePositons);
        }
        return res;
    }

    /**
     * Gibt ausgehened von einer Position, alle Positionen an,
     * welche mit einem Schritt erreicht werden können.
     *
     * @param position die Ausgangsposition.
     * @return das Set mit den errreichbaren Positionen.
     */
    private Set<Position> nextMove(Position position) {
        Set<Position> positions = new HashSet<>();
        positions.add(position);
        return nextMove(positions);
    }

    /**
     * Liefert den Index der des Waffennamens aus dem Array mit allen Waffen im Spiel.
     *
     * @param weaponName der Name der gesuchten Waffe.
     * @return der Index aus dem Array mit allen Waffen im Spiel.
     * @throws CluedoException falls der Waffenname nicht gefunden wurde.
     */
    public int getWeaponIndexByName(String weaponName) throws CluedoException {
        for (int i = 0; i < this.weapons.length; i++) {
            if (this.weapons[i].getName().equals(weaponName)) {
                return i;
            }
        }
        throw new CluedoException(ExceptionType.WeaponNameNotFound);
    }


    /**
     * Liefert die Zuordnung der Waffen zu den Räumen, in denen diese sich aktuell befinden.
     *
     * @return die Zuordnung der Waffen zu den Räumen, in denen diese sich aktuell befinden.
     */
    public Room[] getWeaponInRooms() {
        return weaponInRooms;
    }

    /**
     * Liefert alle Waffen im Spiel.
     *
     * @return alle Waffen im Spiel.
     */
    public Weapon[] getWeapons() {
        return weapons;
    }

    /**
     * Liefert alle Personen/Spielfiguren im Spiel.
     *
     * @return alle Personen/Spielfiguren im Spiel.
     */
    public Character[] getCharacters() {
        return characters;
    }

    /**
     * Setzt die Anzahl der Spieler im Spiel.
     * (Bei neuem Spiel)
     *
     * @param playerCount die Anzahl der Spieler im Spiel.
     */
    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    /**
     * Setzt die Zuordnung der Waffen zu den Räumen, in denen diese sich aktuell befinden.
     *
     * @param weaponInRooms die Zuordnung der Waffen zu den Räumen, in denen diese sich aktuell befinden sollen.
     */
    public void setWeaponInRooms(Room[] weaponInRooms) {
        this.weaponInRooms = weaponInRooms;
    }

    /**
     * Setzt alle Spieler im Spiel.
     *
     * @param players alle Spieler die sich im Spiel befinden sollen.
     */
    public void setPlayers(Player[] players) {
        this.players = players;
    }

    /**
     * Setzt den Umschlag/Lösung des Mordes.
     *
     * @param envelope die lösung des Mordes.
     */
    public void setEnvelope(CardTriple envelope) {
        this.envelope = envelope;
    }

    /**
     * Liefert den Umschlag/die Lösung des Mordes.
     *
     * @return den Umschlag/die Lösung des Mordes.
     */
    public CardTriple getEnvelope() {
        return envelope;
    }

    /**
     * Bestimmt das Spielfeldelement anhand der übergebenen Position
     *
     * @param pos die zu bestimmende Postition
     * @return das Spielfeldelement an dieser Position
     */
    public GameCell getGameCell(Position pos) {
        int gameFieldHeight = gameField.length;
        int gameFieldWidth = gameField[0].length;
        assert pos.getX() >= 0 && pos.getX() < gameFieldWidth;
        assert pos.getY() >= 0 && pos.getY() < gameFieldHeight;
        if (pos.getX() < 0) {
            throw new IllegalArgumentException("X koord. kleiner 0");
        }
        if (pos.getY() < 0) {
            throw new IllegalArgumentException("Y koord. kleiner 0");
        }
        if (pos.getX() >= gameFieldWidth) {
            throw new IllegalArgumentException("X koord. größer " + gameFieldWidth);
        }
        if (pos.getY() >= gameFieldHeight) {
            throw new IllegalArgumentException("Y koord. größer " + gameFieldHeight);
        }
        return this.gameField[pos.getY()][pos.getX()];
    }

    /**
     * Prüft ob die übergebene Position ein Raum ist
     *
     * @param pos die zu prüfende Position
     * @return ob es ein Raum ist oder nicht
     */
    public boolean isRoom(Position pos) {
        return getGameCell(pos).getRoom() != null;
    }


    /**
     * Die Hauptmethode der Logik, welche sich um den kompletten Ablauf des Spiels kümmert.
     * Diese wird vom User über einen Klick auf das Spielfeld angestoßen und läuft bis alle
     * KI-Spieler ihre Züge gemacht haben.
     *
     * @param gameCellPosition Position des Klicks des Users
     * @throws CluedoException falls inkonsistente Zustände auftreten, oder während des Zuges etwas
     *                         fehlschlägt.
     */
    public void makeMove(Position gameCellPosition) throws CluedoException {
        //Wurde auf einen Raum geklickt?
        if (isRoom(gameCellPosition)) { //Klick auf Raum
            Room clickedRoom = getGameCell(gameCellPosition).getRoom();
            if (!canMove() && clickedRoom.getMidPoint().equals(getCurrentPlayerPosition())) {
                handleOwnSuspicion(clickedRoom);
                nextTurn();
                handleAILoop();
            } else if ((this.roomIsReachable(clickedRoom, this.getDice()) && !getCurrentPlayer().isInside(clickedRoom))
                    || (getCurrentPlayer().getRequested() && getCurrentPlayer().isInside(clickedRoom))) {
                //Raum erreichbar und man selbst ist noch nicht drinne, oder man wurde in einen Raum gewünscht und Klickt auf diesen
                //Spieler auf mittelpunkt des Raumes setzen
                this.setCurrentPlayerPosition(clickedRoom.getMidPoint());
                this.gui.drawCharacterInRoom(getCurrentPlayer().getCharacter(), currentPlayerIndex);
                handleOwnSuspicion(clickedRoom);
                nextTurn();
                handleAILoop();
            } else { //Der Raum ist nicht erreichbar
                this.gui.showIllegalStepMessage();
            }
        } else { //Klick Auf Flur
            if (!canMove() && gameCellPosition.equals(getCurrentPlayerPosition())) {
                nextTurn();
                handleAILoop();
            } else if (this.isCorridorReachable(gameCellPosition, this.getDice(), true)) {
                //Ist geklickte Pos ueber den Flur erreichbar?
                this.setCurrentPlayerPosition(gameCellPosition);
                this.gui.drawCharacterOnCorridor(getCurrentPlayer().getCharacter(), currentPlayerIndex);
                nextTurn();
                handleAILoop();
            } else { //Die geklickte Flurkachel ist nicht erreichbar
                this.gui.showIllegalStepMessage();
            }
        }
    }

    /**
     * Hilfsmethode, welche prüft ob der aktuelle Spieler sich bewegen kann.
     *
     * @return ob der aktuelle Spieler sich bewegen kann.
     */
    private boolean canMove() {
        Set<Position> validMoves = generateValidMovesForCurrentPlayer();
        return !validMoves.isEmpty();
    }

    /**
     * Kümmert sich darum, dass der verdächtige Spieler und die verdächtigte Tatwaffe
     * in den Raum gezogen wird, in dem die Verdächtigung ausgesprochen wurde.
     *
     * @param currentPlayer der aktuelle Spieler am Zug.
     * @param suspicion     die geäußerte Verdächtigung.
     * @param enteredRoom   der betretene Raum
     * @throws CluedoException falls inkonsistente Zustände auftreten, oder während des Zuges etwas
     *                         fehlschlägt.
     */
    private void getSuspectedItemsIntoRoom(Player currentPlayer, CardTriple suspicion, Room enteredRoom) throws CluedoException {
        Character suspectedCharacter = getCharacterByName(suspicion.getCharacter().getName());
        //Nur wenn ein anderer Spieler verdächtigt wurde, wird dieser in den Raum gezogen und requested auf true gesetzt
        if (!currentPlayer.getCharacter().equals(suspectedCharacter)) {
            suspectedCharacter.setPosition(enteredRoom.getMidPoint());
            gui.drawCharacterInRoom(suspectedCharacter, getCharacterIndex(suspectedCharacter));
            if (isPlayer(suspectedCharacter)) { //Es handelt sich bei dem verdächtigtem um einen Spieler
                Player suspectedPlayer = getPlayerByName(suspectedCharacter.getName());
                suspectedPlayer.setRequested(true);
            }
        }
        //Die verdächtigte Waffe wird ebenfalls in den Raum gezogen und gezeichnet
        Weapon suspectedWeapon = getWeaponByName(suspicion.getWeapon().getName());
        setWeaponIntoRoom(enteredRoom, suspectedWeapon);
        gui.setWeapon(enteredRoom, suspectedWeapon);
    }

    /**
     * Kümmert sich darum die Karten einzusammeln, welche von den KIs und dem menschlichen Spieler
     * auf eine Verdächtigung hin gezeigt wurden.
     *
     * @param currentPlayer der aktuelle Spieler am Zug.
     * @param suspicion     die vom dem Spieler geäußerte Verdächtigung.
     * @return die von den Mitspielern gezeigten Karten.
     */
    private Card[] handleShownCards(Player currentPlayer, CardTriple suspicion) {
        Card[] shownCards = new Card[players.length - 1]; // - 1 da derjenige der den verdacht ausgesprochen sich nichts zeigt.
        int shownCardsIndex = 0;
        for (Player currentPlayerToShowCard : players) {
            if (!currentPlayerToShowCard.equals(currentPlayer)) { //Nur über die anderen
                if (currentPlayerToShowCard.isAI()) {
                    shownCards[shownCardsIndex] = currentPlayerToShowCard.getAi().showCard(players, cards, currentPlayer, currentPlayerToShowCard, suspicion);
                } else {
                    shownCards[shownCardsIndex] = gui.handleShowCard(suspicion, currentPlayerToShowCard.possibleCardsToShow(suspicion));
                }
                GameLogic.debugln("{" + currentPlayerToShowCard.getCharacter().getName() + "} zeigt daraufhin: {" + shownCards[shownCardsIndex] + "}");
                shownCardsIndex++;
            }
        }
        return shownCards;
    }


    /**
     * Kümmert sich darum den KI-Spielern mitzuteilen, ob ein bestimmter Spieler als Antwort auf die Verdächtigung
     * demjenigen der die Verdächtigung ausgesprochen eine Karte gezeigt hat oder nicht.
     *
     * @param currentPlayer der aktuelle Spieler am Zug.
     * @param suspicion     die vom dem Spieler geäußerte Verdächtigung.
     */
    private void showShownCardsToAIs(Player currentPlayer, CardTriple suspicion, Card[] shownCards) {
        //Alle KIs-Bekommen ebenfalls die Information ob ein Bestimmter Spieler der KI eine Karte gezeigt hat oder nicht
        //Nur die schlaue KI verarbeitet die Information
        for (Player player : players) {
            if (!player.equals(currentPlayer) && player.isAI()) {
                player.getAi().watchCardsGetShown(players, cards, player, currentPlayer, suspicion, shownCards); //Jede KI muss wissen wie sie shown cards Interpertiert
            }
        }
    }

    /**
     * Kümmert sich um die Verdächtigung einer KI und allen Aktionen die damit verbunden sind.
     *
     * @param enteredRoom der von der KI betretene Raum.
     * @throws CluedoException falls inkonsistente Zustände auftreten, oder während des Zuges etwas
     *                         fehlschlägt.
     */
    private void handleAISuspicion(Room enteredRoom) throws CluedoException {
        Card enteredRoomCard = getCardByName(enteredRoom.getName());
        Player currentPlayer = getCurrentPlayer();
        CardTriple suspicion = currentPlayer.getAi().expressSuspicion(this, currentPlayer, enteredRoomCard);
        GameLogic.debugln("Ich bin { " + currentPlayer.getCharacter().getName() + " } ich verdächtige { " + suspicion + " }");
        getSuspectedItemsIntoRoom(currentPlayer, suspicion, enteredRoom);
        //Dialog der angezeigt, wer welchen Verdacht ausgesprochen hat
        gui.handleAISuspicion(currentPlayer.getCharacter().getName(), suspicion);
        //Dialog mit vorausgefüllten Feldern aus dem Verdacht. Nur die Karten auf der Hand hat sind wählbar
        Card[] shownCards = handleShownCards(currentPlayer, suspicion);
        //Die gezeigten Karten müssen an die KI übergeben werden (Karte muss zu Spieler zuordbar sein)
        currentPlayer.getAi().getCardsShown(players, cards, currentPlayer, suspicion, shownCards);
        // ->Der KI-Spieler Notiert sich von wem was konkret gezeigt wurde
        //Karten bekommen mit welche Karten gezeigt wurden
        showShownCardsToAIs(currentPlayer, suspicion, shownCards);
        //Der spieler bekommt angezeigt ob die KI-Spieler eine Karte gezeigt haben oder nicht
        gui.handleOthersSuspicionResult(players, currentPlayer, shownCards, suspicion);
    }

    /**
     * Kümmert sich um die Verdächtigung eines menschlichen Spielers und allen Aktionen die damit verbunden sind.
     *
     * @param enteredRoom der von der KI betretene Raum.
     * @throws CluedoException falls inkonsistente Zustände auftreten, oder während des Zuges etwas
     *                         fehlschlägt.
     */
    private void handleOwnSuspicion(Room enteredRoom) throws CluedoException {
        Card enteredRoomCard = getCardByName(enteredRoom.getName());
        Player currentPlayer = getCurrentPlayer();
        //Dialog mit der vorauswahl des Raumes
        CardTriple suspicion = gui.handleExpressSuspicion(enteredRoomCard);
        GameLogic.debugln("Ich bin {" + getCurrentPlayer().getCharacter().getName() + "} Ich verdächtige {" + suspicion + "}");
        //Waffe und Character in den Raum
        getSuspectedItemsIntoRoom(currentPlayer, suspicion, enteredRoom);
        //Die Karten welche von den anderen Spielern gezeigt werden bekommen
        Card[] shownCards = handleShownCards(currentPlayer, suspicion);
        //Alle Schlauen KIs-Bekommen ebenfalls die Information ob ein Bestimmter Spieler der KI eine Karte gezeigt hat oder nicht
        showShownCardsToAIs(currentPlayer, suspicion, shownCards);
        gui.handleOwnSuspicionResult(players, shownCards, suspicion);
    }


    /**
     * Kümmert sich um alle Aktionen die während die KIs am Zug sind passieren.
     *
     * @throws CluedoException falls inkonsistente Zustände auftreten, oder während des Zuges etwas
     *                         fehlschlägt.
     */
    private void handleAILoop() throws CluedoException {
        boolean gameEnded = false;
        while (!gameEnded && getCurrentPlayer().isAI()) {
            if (handleAIAccusation()) {
                gameEnded = true;
            } else {
                //Wenn der Spieler in den Raum gewünscht wurde dann muss kein neuer Zug berechnet werden sondern es wird in diesem Raum eine Verdächtigung ausgesprochen
                if (getCurrentPlayer().getRequested()) {
                    GameCell currentPlayerLocation = getGameCell(getCurrentPlayer().getCharacter().getPosition());
                    if (currentPlayerLocation.isRoom()) {
                        handleAISuspicion(currentPlayerLocation.getRoom());
                    } else {//Spieler wurde in einen Raum gewünscht, befindet sich aber nicht in einem Raum
                        throw new CluedoException(ExceptionType.RequestedButNotInRoom);
                    }
                } else {
                    //KI Zug Position Berechnen
                    Position dest = getCurrentPlayer().getAi().computeNextMove(this, getCurrentPlayer(), getDice());
                    if (dest != null) { //Ein zug wurde berechnet
                        //Ist der Move der KI valide?
                        assert generateValidMovesForCurrentPlayer().contains(dest);
                        //Der KI-Spieler am Zug wird auf die Position gesezt
                        this.setCurrentPlayerPosition(dest);
                        //Hat der KI-Spieler einen Raum betreten?
                        if (isRoom(dest)) {
                            Room enteredRoom = getGameCell(dest).getRoom();
                            //Der KI-Spieler wird in den betretenden Raum gezeichnet
                            gui.drawCharacterInRoom(getCurrentPlayer().getCharacter(), getCurrentPlayerIndex());
                            handleAISuspicion(enteredRoom);
                            if (handleAIAccusation()) {
                                gameEnded = true;
                            }
                        } else { //KI-Spieler ist weiterhin auf dem Flur
                            this.gui.drawCharacterOnCorridor(getCurrentPlayer().getCharacter(), currentPlayerIndex);
                        }
                    } else {
                        //der weg ist versperrt: stehen bleiben und, falls in Raum, eine verdächtigung aussprechen.
                        if (isRoom(getCurrentPlayerPosition())) {
                            handleAISuspicion(getGameCell(getCurrentPlayerPosition()).getRoom());
                            if (handleAIAccusation()) {
                                gameEnded = true;
                            }
                        }
                    }
                }
            }
            if (!gameEnded) {
                nextTurn();
            }
        }
    }

    /**
     * Kümmert sich um die Anklage der KI, welche aktuell am Zug ist.
     *
     * @return ob die KI eine Erfolgreiche anklage geäußert hat und das Spiel somit beendet ist.
     * @throws CluedoException falls inkonsistente Zustände auftreten, oder während des Zuges etwas
     *                         fehlschlägt.
     */
    private boolean handleAIAccusation() throws CluedoException {
        CardTriple solution = getCurrentPlayer().getAi().expressAccusation(this, getCurrentPlayer());
        if (solution != null) {
            if (checkGameWon(solution)) {
                gui.handleGameWon(solution, getCurrentPlayer());
            } else {
                gui.handleGameLost(envelope, solution, getCurrentPlayer());
            }
            return true;
        }
        return false;
    }

    /**
     * Überprüft ob die Lösung des Spiels gefunden wurde.
     *
     * @param solution die angebliche Lösung des Spiels.
     * @return ob die Lösung des Spiels gefunden wurde.
     */
    public boolean checkGameWon(CardTriple solution) {
        return solution.equals(envelope);
    }

    /**
     * Liefert einen Raum zu einer Türposition.
     *
     * @param door die Position einer Tür.
     * @return Liefert einen Raum zu einer Türposition oder null.
     */
    public Room getRoomFromDoor(Position door) {
        for (Room currRoom : getRooms()) {
            for (Position currDoor : currRoom.getDoors()) {
                if (currDoor.equals(door)) {
                    return currRoom;
                }
            }
        }
        return null;
    }

    /**
     * Setzt die übergebene Waffe in den übergebene Raum.
     *
     * @param room   der Raum in den die Waffe gesetzt werden soll.
     * @param weapon die Waffe, welche in den Raum gesetzt werden soll.
     * @throws CluedoException falls die Waffe nicht vorhanden.
     */
    public void setWeaponIntoRoom(Room room, Weapon weapon) throws CluedoException {
        int weaponIndex = getWeaponIndex(weapon);
        weaponInRooms[weaponIndex] = room;
    }

    /**
     * Liefert den Index der übergebenen Waffe im Waffenarray.
     *
     * @param weapon die Waffe.
     * @return der Index der Waffe.
     * @throws CluedoException Waffe ist nicht im Weapons Array.
     */
    public int getWeaponIndex(Weapon weapon) throws CluedoException {
        for (int i = 0; i < weapons.length; i++) {
            if (weapons[i].equals(weapon)) {
                return i;
            }
        }
        throw new CluedoException(ExceptionType.WeaponNotFound); //Waffe nicht im weapons Array
    }

    /**
     * Prüft, ob die Person/Spielfigur ein aktiver Spieler ist.
     *
     * @param character die Person welche überprüft werden soll.
     * @return ob die Person/Spielfigur ein aktiver Spieler ist oder nicht.
     */
    private boolean isPlayer(Character character) {
        for (Player player : players) {
            if (player.getCharacter().equals(character)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Liefert den Index der Person/Spielfigur im Spielfigurenarray.
     *
     * @param character die Spielfigur zu der der Index gesucht wird.
     * @return den Index der Spielfigur im Spielfigurenarray.
     * @throws CluedoException falls diese nicht gefunden.
     */
    private int getCharacterIndex(Character character) throws CluedoException {
        for (int i = 0; i < characters.length; i++) {
            if (characters[i].equals(character)) {
                return i;
            }
        }
        throw new CluedoException(ExceptionType.CharacterNotFound);
    }

    /**
     * Kümmert sich um die Dinge, die beim Start eines Zuges geschehen sollen.
     */
    private void startTurn() {
        this.rollDice();
        this.gui.updateDice(this.getDice());
        this.gui.clearPossibleMoves();
        if (!getCurrentPlayer().isAI()) {
            Set<Position> validMoves = generateValidMovesForCurrentPlayer();
            if (validMoves.isEmpty()) {
                validMoves.add(getCurrentPlayerPosition());
            }
            gui.drawPossibleMoves(validMoves);
        }
    }

    /**
     * Generiert valide Positionen für den aktuellen Spieler.
     *
     * @return valide Positionen für den aktuellen Spieler.
     */
    private Set<Position> generateValidMovesForCurrentPlayer() {
        Set<Position> validMovesExact = generateValidCorridorMovesForCurrentPlayer(this.getDice(), true);
        for (Room room : this.rooms) {
            if (!room.equals(getGameCell(getCurrentPlayerPosition()).getRoom())) {
                if (this.roomIsReachable(room, this.getDice())) {
                    validMovesExact.add(room.getMidPoint());
                }
            }

        }
        return validMovesExact;
    }

    /**
     * Kümmert sich um den Start eines neuen Zuges.
     */
    private void nextTurn() {
        endTurn();
        this.setNextPlayer();
        this.startTurn();
    }

    /**
     * Kümmert sich um das Ende eines neuen Zuges.
     */
    private void endTurn() {
        getCurrentPlayer().setRequested(false);
    }

    /**
     * Konstruiert ein Spielfeld aus einer Stringrepräsentation.
     *
     * @param height die höhe des Spielfeldes.
     * @param width  die breite des Spielfeldes.
     * @param str    das Spielfeld als String.
     * @return das Spielfeld für die Logik.
     */
    public static GameCell[][] gameFieldFromString(int height, int width, String[] str, GameCell[] avalibleGameCells) {
        GameCell[][] res = new GameCell[height][width];
        for (int y = 0; y < height; y++) {
            String currRow = str[y];
            for (int x = 0; x < width; x++) {
                char currChar = currRow.charAt(x);
                GameCell currGameCell;
                if (currChar == ' ') {
                    currGameCell = GameCell.CORRIDOR;
                } else if (currChar == '#') {
                    currGameCell = GameCell.WALL;
                } else {
                    currGameCell = avalibleGameCells[java.lang.Character.getNumericValue(currChar)];
                }
                res[y][x] = currGameCell;
            }
        }
        return res;
    }

    /**
     * Liefert einen zufälligen Raum aus den gesamten Räumen im Spiel, außer dem übergebenen.
     *
     * @param room der Raum, welcher ausgeschlossen werden soll.
     * @return der zufällige Raum aus den gesamten Räumen im Spiel, außer dem übergebenen.
     */
    public Room getRandomRoom(Room room) {
        List<Room> rooms = new ArrayList<>(Arrays.asList(this.rooms));
        if (room != null) { //Wenn aktuell in einem Raum, dann diesen aus der Betrachtung aussließen
            rooms.remove(room);
        }
        Collections.shuffle(rooms);
        return rooms.get(0);
    }

    /**
     * Prüft ob ein Raum, über den Flur mit den gegeben Schitten erreichbar wäre.
     *
     * @param steps die notwendigen Schritte um den Raum zu erreichen.
     * @return ob ein Raum, über den Flur mit den gegeben Schitten erreichbar ist.
     */
    public boolean roomIsInReach(int steps) {
        for (Room room : rooms) {
            if (roomIsReachable(room, steps)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prüft, ob der übergebene Raum von der aktuellen Position des aktuellen Spielers erreichbar ist.
     * Unter berücksichtigung der Geheimräume.
     *
     * @param dest  der Zielraum
     * @param steps schritte mit denen der Raum erreich werden sollte.
     * @return ob der übergebene Raum von der aktuellen Position des aktuellen Spielers erreichbar ist oder nicht.
     */
    public boolean roomIsReachable(Room dest, int steps) {
        boolean destFound = false;
        GameCell currPlayerPos = getGameCell(getCurrentPlayerPosition());
        if (currPlayerPos.isRoom()) {
            Room secretCorridorDest = currPlayerPos.getRoom().getSecretCorridor();
            if (secretCorridorDest != null && secretCorridorDest == dest) {
                //Unabhängig der Schrittzahl erreichabr
                destFound = true;
            }
        }
        if (!destFound && dest.getDoors() != null) {
            Position[] doors = dest.getDoors();
            for (Position door : doors) {
                if (isCorridorReachable(door, steps - 1, false)) { //Ein schritt noch für in den Raum notw.
                    destFound = true;
                }
            }
        }
        return destFound;
    }

    /**
     * Liefert den kürzesten Weg von einer Startposition aus zu einer Menge an Zielpositionen.
     *
     * @param startPosition die Ausgangsposition.
     * @param destinations  die Zielpositionen zu denen der kürzeste Pfad berechnent werden soll.
     * @return der kürzeste von der Startposition bis zu der Endposition.
     * Gibt null zurück, falls kein Pfad gefunden wurde.
     */
    public List<Position> getShortestPath(Position startPosition, Set<Position> destinations) {
        if (destinations != null && destinations.size() > 0) { //Muss mind. ein Ziel haben
            List<Position> open = new LinkedList<>();
            open.add(startPosition);
            Map<Position, Position> cameFrom = new HashMap<>();
            cameFrom.put(startPosition, null);
            while (!open.isEmpty()) {
                Position current = open.remove(0); //nimmt das erste Element
                if (destinations.contains(current)) {
                    List<Position> path = new LinkedList<>();
                    while (current != null) { //Beim Abbruch ist Start schon enthalten
                        path.add(0, current); //Vorne einfügen um nicht umkehren zu müssen
                        current = cameFrom.get(current); //Vorgänger vom Aktuellen
                    }
                    return path;
                }
                for (Position next : nextMove(current)) {
                    if (!cameFrom.containsKey(next)) { //Noch nicht besucht
                        open.add(next);
                        cameFrom.put(next, current); //Von current nach next
                    }
                }
            }
        }
        return null; //Keinen Weg vorhanden extraverhalten z.b. wenn letze Tür versperrt oder eingestellt
    }

    /**
     * Liefert den Index des aktuellen Spielers.
     *
     * @return der Index des aktuellen Spielers.
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * Liefert alle Räume im Spiel.
     *
     * @return alle Räume im Spiel.
     */
    public Room[] getRooms() {
        return rooms;
    }

    /**
     * Liefert alle Spieler im Spiel.
     *
     * @return alle Spieler im Spiel.
     */
    public Player[] getPlayers() {
        return players;
    }

    /**
     * Liefert alle Karten im Spiel.
     *
     * @return alle Karten im Spiel.
     */
    public Card[] getCards() {
        return this.cards;
    }


    /**
     * Methode die ein Spiel ausgehend von der Initialisierungsdatei der Anzahl der Spieler und deren Schwierigkeitsgrade erstellt.
     * Hat diselbe Funktionalität wie ein Konstruktor, wird jedoch nur einmal verwendet und auf den allgemeinen Konstruktor zurückgefuehrt.
     * Aufruf eines Konstruktors innerhalb eines anderen am Ende nicht möglich.
     *
     * @param initGameDataJSON Die initialdaten für das Starten eines Spieles.
     *                         Enthält Namen und Anzhal der Räume, Waffen und Charaktere, sowie das Spielfeld.
     * @param playerAmount     Die Anzahl der Spieler, die an dem Spiel teilnehmen.
     * @param difficulties     Die Stärken der KI Stärken (inkl. Menschl. [0] == null)
     * @return Die Initialisierte Spiellogik.
     */
    public static GameLogic createInitialGameLogicFromJSON(InitialGameDataJSON initGameDataJSON, int playerAmount, AIDifficulty[] difficulties) {
        //Es wird davon ausgegangen, dass die Initialisierungstatei korrekt aufgebaut ist!
        //Daten liegen jetzt vor
        //Initialisierung der Räume
        InitialRoomJSON[] initialRooms = initGameDataJSON.getRooms();
        Room[] rooms = new Room[initialRooms.length];
        for (int i = 0; i < initialRooms.length; i++) {
            rooms[i] = Room.fromJSON(initialRooms[i]);
        }
        //Geheimgang der Rooms setzen
        for (int i = 0; i < initialRooms.length; i++) {
            Room secretDorridorDest = null;
            String secretCorridorDestStr = initialRooms[i].getSecretCorridor();
            if (secretCorridorDestStr != null) {
                for (Room room : rooms) {
                    if (room.getName().equals(secretCorridorDestStr)) {
                        secretDorridorDest = room;
                    }
                }
                //initialRooms und rooms sind in derselben Reihenfolge
                rooms[i].setSecretCorridor(secretDorridorDest);

            }
        }

        Character[] characters = initCharacters(initGameDataJSON);
        //Initialisierung der verfügbaren Waffen
        String[] Initialweapons = initGameDataJSON.getWeapons();
        Weapon[] weapons = new Weapon[Initialweapons.length];
        for (int i = 0; i < weapons.length; i++) {
            weapons[i] = Weapon.fromJSON(Initialweapons[i]);
        }

        GameCell[] avalibleGameCells = new GameCell[rooms.length];
        for (int i = 0; i < rooms.length; i++) {
            avalibleGameCells[i] = new GameCell(rooms[i]);
        }
        int gameFieldHeight = initGameDataJSON.getGameField().getGameFieldHeight();
        int gameFieldWidth = initGameDataJSON.getGameField().getGameFieldWidth();
        String[] gameFieldStr = initGameDataJSON.getGameField().getGameField();
        GameCell[][] gameField = gameFieldFromString(gameFieldHeight, gameFieldWidth, gameFieldStr, avalibleGameCells);

        Card[] cards = new Card[rooms.length + characters.length + weapons.length];
        int cardIndexCounter = 0;
        //cards wird in der selben Reihenfolge befüllt, wie die Karten in der init. Datei stehen und
        //somit auch in derselben Reihenfolge, wie in der GUI die Zeilen der Notitzen
        for (Character character : characters) {
            cards[cardIndexCounter] = new Card(character.getName(), CardType.CHARACTER);
            cardIndexCounter++;
        }
        for (Weapon weapon : weapons) {
            cards[cardIndexCounter] = new Card(weapon.getName(), CardType.WEAPON);
            cardIndexCounter++;
        }
        for (Room room : rooms) {
            cards[cardIndexCounter] = new Card(room.getName(), CardType.ROOM);
            cardIndexCounter++;
        }
        return new GameLogic(rooms, characters, weapons, gameField, cards, playerAmount, difficulties);
    }

    /**
     * Liefert aus der initialen Spieldatei die Personen/Spielfiguren.
     *
     * @param initGameDataJSON die initiale Spieldatei.
     * @return die Personen/Spielfiguren im gesamten Spiel.
     */
    private static Character[] initCharacters(InitialGameDataJSON initGameDataJSON) {
        //Initialisierung der Character
        InitialCharacterJSON[] initialCharacters = initGameDataJSON.getPlayers();
        Character[] characters = new Character[initialCharacters.length];
        for (int i = 0; i < initialCharacters.length; i++) {
            characters[i] = Character.fromJSON(initialCharacters[i]);
        }
        return characters;
    }

    /**
     * Liefert eine Karte ausgehend von dem Zeilenindex in den Notizen.
     *
     * @param index der Index der gesuchten Karte.
     * @return die gesuchte Karte
     */
    public Card getCardByNoteIndex(int index) {
        //Reihenfoge der Notizen muss mit der im cards-array übereinstimmen.
        return this.cards[index];
    }

    /**
     * Liefert die offenen/nicht besetzten Türen des übergebenen Raumes.
     *
     * @param room der Raum.
     * @return die offenen Türen des Raumes.
     */
    public Set<Position> getOpenDoors(Room room) {
        Set<Position> openDoors = new HashSet<>();
        for (Position door : room.getDoors()) {
            if (!isOccupied(door)) {//Tür frei -> betretbar
                openDoors.add(door);
            }
        }
        return openDoors;
    }

    /**
     * Prüft, ob Position von einer anderen Spielfigur belegt ist.
     *
     * @param position die zu prüfende Position.
     * @return ob Position von einer anderen Spielfigur belegt ist.
     */
    private boolean isOccupied(Position position) {
        for (Character character : characters) {
            if (!getCurrentPlayer().getCharacter().equals(character) && character.getPosition().equals(position)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Liefert die Waffe im Spiel ausgehend von ihrem Namen.
     *
     * @param weaponName der Name der gesuchten Waffe.
     * @return die gesuchte Waffe.
     * @throws CluedoException falls eine Waffe mit dem Namen nicht vorhanden ist.
     */
    public Weapon getWeaponByName(String weaponName) throws CluedoException {
        for (Weapon weapon : this.weapons) {
            if (weapon.getName().equals(weaponName)) {
                return weapon;
            }
        }
        throw new CluedoException(ExceptionType.WeaponNameNotFound);
    }


    /**
     * Liefert den Raum im Spiel ausgehend von ihrem Namen.
     *
     * @param roomName der Name des gesuchten Raumes.
     * @return der gesuchte Raum
     * @throws CluedoException falls der Raum mit dem Namen nicht vorhanden ist.
     */
    public Room getRoomByName(String roomName) throws CluedoException {
        for (Room room : this.rooms) {
            if (room.getName().equals(roomName)) {
                return room;
            }
        }
        throw new CluedoException(ExceptionType.RoomNameNotFound);

    }

    /**
     * Liefert die Spielfigur im Spiel ausgehend von ihrem Namen.
     *
     * @param characterName der Name der gesuchten Spielfigur.
     * @return die Spielfigur.
     * @throws CluedoException Falls die Spielfigur nicht gefunden wurde.
     */
    public Character getCharacterByName(String characterName) throws CluedoException {
        for (Character current : this.characters) {
            if (current.getName().equals(characterName)) {
                return current;
            }
        }
        throw new CluedoException(ExceptionType.CharacterNameNotFound);
    }

    /**
     * Liefert den Spieler der mit der übergebenen Spielfigur spielt.
     *
     * @param characterName der Name der Spielfigur des Spielers.
     * @return der Spieler, der mit der Spielfigur spielt.
     * @throws CluedoException falls kein Spieler im Spiel mit der Spielfigur spielt.
     */
    public Player getPlayerByName(String characterName) throws CluedoException {
        for (Player player : players) {
            if (player.getCharacter().getName().equals(characterName)) {
                return player;
            }
        }
        throw new CluedoException(ExceptionType.PlayerNameNotFound);
    }

    /**
     * Liefert eine Karte ausgehend von ihrem Namen.
     *
     * @param cardName der Name der gesuchten Karte.
     * @return die gesuchte Karte.
     * @throws CluedoException falls die Karte nicht im Spiel vorhanden ist.
     */
    public Card getCardByName(String cardName) throws CluedoException {
        for (Card currCard : this.cards) {
            if (currCard.getName().equals(cardName)) {
                return currCard;
            }
        }
        throw new CluedoException(ExceptionType.CardNameNotFound);
    }


    /**
     * Hilfsmethode zum debuggen.
     *
     * @param message die auszugebende Nachricht mit zeilenumbruch.
     */
    public static void debugln(String message) {
        if (DEBUG_MODE) {
            System.out.println(message);
        }
    }

    /**
     * Hilfsmethode zum debuggen.
     *
     * @param message die auszugebende Nachricht ohne zeilenumbruch.
     */
    public static void debug(String message) {
        if (DEBUG_MODE) {
            System.out.print(message);
        }
    }

    /**
     * Hilfsmethode zum debuggen.
     */
    public static void debugln() {
        if (DEBUG_MODE) {
            System.out.println();
        }
    }

    /**
     * Prüft, ob die übergebene Position innerhalb des Spielfeldes liegt.
     *
     * @param pos die zu überprüfende Position.
     * @return ob die übergebene Position innerhalb des Spielfeldes liegt.
     */
    public boolean isValidPosition(Position pos) {
        int gameFieldHeight = gameField.length;
        int gameFieldWidth = gameField[0].length;

        if (pos.getX() < 0) {
            return false;
        }
        if (pos.getY() < 0) {
            return false;
        }
        if (pos.getX() >= gameFieldWidth) {
            return false;
        }
        if (pos.getY() >= gameFieldHeight) {
            return false;
        }
        return true;
    }
}
