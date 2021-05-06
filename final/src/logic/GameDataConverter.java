package logic;

import logic.exceptions.CluedoException;
import logic.exceptions.ExceptionType;
import logic.json.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Diese Klasse enthält ist für die Serialisierung und Deserialisierung des Spielstandes verantwortlich.
 * In anderen Worten, sie lädt und validiert eine Spielstandsdatei und schreibt aus einem Spiel eine
 * Spielstandsdatei.
 * Es wird erwartert, dass die Reihenfolge der Spieler mit der Reihenfolge der Notizen
 * der Spieler übereinstimmt.
 *
 * @author Michael Smirnov
 */
public class GameDataConverter {

    /**
     * Konvertiert die Hauptspiellogik in ein Spielstandsdatei, welche mit
     * GSON geschrieben werden kann.
     *
     * @param logic die Hauptspiellogik.
     * @return ein Objekt, welches als JSON geschieben werden kann.
     */
    public static GameDataJSON convertToGameDataJSON(GameLogic logic) {
        int playersInGame = logic.getPlayers().length;
        int charactersInGame = logic.getCharacters().length;
        //Infos zu den Spielern im Spiel
        PlayerJSON[] playersJSON = new PlayerJSON[playersInGame];
        //Notizen für jeden Spieler im Spiel, die Notizen über ALLE Spieler enthalten
        NoteJSON[][] playerNotesJSON = new NoteJSON[playersInGame][charactersInGame];
        for (int i = 0; i < playersJSON.length; i++) {
            playersJSON[i] = GameDataConverter.convertToPlayerJSON(logic, i);
            playerNotesJSON[i] = GameDataConverter.convertToNoteJSON(logic, i);
        }
        WeaponJSON[] weaponJSON = GameDataConverter.convertToWeaponJSON(logic.getWeapons(), logic.getWeaponInRooms());
        return new GameDataJSON(playersJSON, weaponJSON, playerNotesJSON);
    }


    /**
     * Konvertiert einen Spieler im Spiel zu einem Spieler der gespeichert werden kann.
     *
     * @param logic       die Hauptspiellogik.
     * @param playerIndex der Index des zu konvertierenden Spielers im Hauptspielarray
     * @return der Spieler, der nun in eine Spielstandsdatei geschrieben werden kann.
     */
    private static PlayerJSON convertToPlayerJSON(GameLogic logic, int playerIndex) {
        Player player = logic.getPlayers()[playerIndex];
        String name = player.getCharacter().getName();
        String iq = fromAiToString(player.getAi());
        String room;
        if (logic.getGameCell(player.getPos()).isRoom()) {
            room = logic.getGameCell(player.getPos()).getRoom().getName();
        } else if (logic.getGameCell(player.getPos()).isCorridor()) {  //Ist Flur
            room = "Flur";
        } else { //Ist Wand (kann nicht passieren)
            throw new IllegalStateException("Spieler steckt in der Wand");
        }
        Position position = player.getPos();
        boolean requested = player.getRequested();
        CardsJSON cards = GameDataConverter.convertToCardsJSON(player.getCards());
        return new PlayerJSON(name, iq, room, position, requested, cards);
    }

    /**
     * Konvertiert die Karten eines Spielers in die Kartenrepräsentation der Spielstandsdatei.
     *
     * @param cards die Karten welche konvertiert werden sollen.
     * @return die Karten in der Spielstandsdatei.
     */
    private static CardsJSON convertToCardsJSON(List<Card> cards) {
        List<String> weapons = new ArrayList<>();
        List<String> rooms = new ArrayList<>();
        List<String> characters = new ArrayList<>();
        for (Card card : cards) {
            switch (card.getType()) {
                case WEAPON:
                    weapons.add(card.getName());
                    break;
                case ROOM:
                    rooms.add(card.getName());
                    break;
                case CHARACTER:
                    characters.add(card.getName());
                    break;
                default:
                    throw new IllegalStateException("Kann nicht passieren");
            }
        }
        return new CardsJSON(characters.toArray(new String[0]), rooms.toArray(new String[0]), weapons.toArray(new String[0]));
    }

    /**
     * Wandelt eine KI instanz in die Stringrepräsentation in der Spielstandsdatei um.
     *
     * @param ai die KI eines Spielers.
     * @return der String der die KI-Stärke repräsentiert.
     */
    private static String fromAiToString(AI ai) {
        //Im Spielsand wird nicht zwischen menschl. und "normaler" KI unterschieden
        if (ai == null || ai.getDifficulty().equals(AIDifficulty.NORMAL)) {
            return "normal";
        } else if (ai.getDifficulty() == AIDifficulty.SMART) {
            return "schlau";
        } else {
            return "dumm";
        }
    }

    /**
     * Konvertiert einen String in die jeweilige KI-Stärke.
     *
     * @param aiString der zu konvertierende String.
     * @return die jew. KI-Stärke.
     * @throws CluedoException Falls der String null oder eine unbekannte Spielstärke steht.
     */
    private static AIDifficulty fromStringToAIDifficulty(String aiString) throws CluedoException {
        if (aiString == null) {
            throw new CluedoException(ExceptionType.AIDifficultyNotFound);
        }
        switch (aiString) {
            case "dumm":
                return AIDifficulty.STUPID;
            case "normal":
                return AIDifficulty.NORMAL;
            case "schlau":
                return AIDifficulty.SMART;
            default:
                throw new CluedoException(ExceptionType.AIDifficultyNotFound);
        }
    }

    /**
     * Konvertiert die Waffen im Raum in deren Spielstandsräpresentation.
     *
     * @param weapons        alle Waffen im Spiel.
     * @param weaponsInRooms Die Waffen, welche zu Räumen zugeordnet sind.
     * @return die Waffen mit den Räumen wie in der Spielstandsdatei.
     */
    private static WeaponJSON[] convertToWeaponJSON(Weapon[] weapons, Room[] weaponsInRooms) {
        WeaponJSON[] result = new WeaponJSON[weapons.length];
        for (int i = 0; i < weapons.length; i++) {
            result[i] = new WeaponJSON(weapons[i].getName(), weaponsInRooms[i].getName());
        }
        return result;
    }

    /**
     * Konvertiert die eigenen Notizen eines Spielers in das JSON Speicherformat.
     *
     * @param logic       die Hauptspiellogik.
     * @param playerIndex Index des zu konvertierenden Spielers im Hauptspiel-Spielerarray.
     * @return die eigenen Notizen eines Spielers im Speicherformat.
     */
    private static NoteJSON[] convertToNoteJSON(GameLogic logic, int playerIndex) {
        Player[] players = logic.getPlayers();
        Player player = players[playerIndex];
        Character[] allCharacters = logic.getCharacters();
        Card[] allCards = logic.getCards();
        NoteJSON[] notes = new NoteJSON[logic.getCharacters().length]; //Die character im Spiel
        //Über welchen Spieler
        for (int destinationIndex = 0; destinationIndex < notes.length; destinationIndex++) {
            if (destinationIndex == playerIndex) { //Eigene Notizen konvertieren
                notes[destinationIndex] = convertOwnNotesOfPlayerToNoteJSON(allCards, player);
            } else if (destinationIndex < playerIndex) { //Notizen alle verbleibenden Spieler
                notes[destinationIndex] = convertNoteOthersOfPlayerToNoteJSON(allCards, allCharacters, player, destinationIndex, destinationIndex);
            } else { //characterIndex > playerIndex
                notes[destinationIndex] = convertNoteOthersOfPlayerToNoteJSON(allCards, allCharacters, player, destinationIndex, destinationIndex - 1);
            }
        }
        return notes;
    }


    /**
     * Konvertiert die Notizen eines Spielers über andere Spieler in das Speicherformat.
     *
     * @param allCards         alle Karten im Spiel.
     * @param allCharacters    alle Personen im Spiel.
     * @param player           der Spieler, dessen Notizen zu konvertieren sind.
     * @param destinationIndex der Index des Spielers den wir konvertieren wollen.
     * @param sourceIndex      der Index des Spielers über den wir die Notizen haben und konvertieren wollen.
     * @return die Konvertierten Notizen eines Spielers über andere Spieler im Speicherformat.
     */
    private static NoteJSON convertNoteOthersOfPlayerToNoteJSON(Card[] allCards, Character[] allCharacters, Player player, int destinationIndex, int sourceIndex) {
        //den Index ausgehend von dem aktuellem player für die Notiz über den Spieler an characterIndex
        //Die indece berechnug muss pro spieler anders sein, da die eigenen notizen in den others ja fehlen
        NoteOthers[] noteOthers = player.getNoteOthers()[sourceIndex];
        String name = allCharacters[destinationIndex].getName(); //Name des Spielers dessen Notizen wir generieren
        List<String> ownPersonas = new ArrayList<>();
        List<String> ownRooms = new ArrayList<>();
        List<String> ownWeapons = new ArrayList<>();
        splitNotes(allCards, noteOthers, ownPersonas, ownRooms, ownWeapons);

        return new NoteJSON(name,
                new MemoJSON(ownPersonas.toArray(new String[0]),
                        ownRooms.toArray(new String[0]),
                        ownWeapons.toArray(new String[0])
                ));

    }

    /**
     * Konvertiert die eigenen Notizen eines Spielers in das Speicherformat.
     *
     * @param allCards alle Karten im Spiel.
     * @param player   der Spieler, dessen eigene Notizen konvertiert werden sollen.
     * @return die Konvertierten eigenen Notizen des Spielers in Speicherformat.
     */
    private static NoteJSON convertOwnNotesOfPlayerToNoteJSON(Card[] allCards, Player player) {
        NoteSelf[] ownNotes = player.getNoteSelf();
        String name = player.getCharacter().getName();
        List<String> ownPersonas = new ArrayList<>();
        List<String> ownRooms = new ArrayList<>();
        List<String> ownWeapons = new ArrayList<>();
        splitNotes(allCards, ownNotes, ownPersonas, ownRooms, ownWeapons);
        return new NoteJSON(name,
                new MemoJSON(ownPersonas.toArray(new String[0]),
                        ownRooms.toArray(new String[0]),
                        ownWeapons.toArray(new String[0])
                ));
    }

    /**
     * Hilfsmethode, welche Notizern nach ihrem Kartentyp aufteilt.
     *
     * @param allCards    alle Karten im Spiel.
     * @param notes       die Notizen welche aufgeteilt werden sollen.
     * @param ownPersonas die Personennotizen
     * @param ownRooms    die Raumnotizen
     * @param ownWeapons  die Waffennotizen
     */
    private static void splitNotes(Card[] allCards, Object[] notes, List<String> ownPersonas, List<String> ownRooms, List<String> ownWeapons) {
        for (int i = 0; i < notes.length; i++) {
            Object currentNote = notes[i];
            Card currentCard = allCards[i];
            if (currentCard.isCharacter()) {
                ownPersonas.add(currentNote.toString());
            } else if (currentCard.isRoom()) {
                ownRooms.add(currentNote.toString());
            } else if (currentCard.isWeapon()) {
                ownWeapons.add(currentNote.toString());
            } else {
                throw new IllegalStateException("Kein bekannter Kartentyp");
            }
        }
    }


    /**
     * Konvertiert die geladenen Spielstandsdaten in ein Objekt, welches die geladene Spielstandsdatei repräsentiert.
     *
     * @param loadedGame der geladene Spielstand.
     * @param logic      die Hauptspiellogik.
     * @return die gerade geladenen Daten welche in ein zur Hauptspiellogik passendes Format konvertiert wurden.
     * @throws CluedoException falls Fehler bei der Validierung auftreten.
     */
    public static LoadedGameLogic convertToLoadedGameLogic(GameDataJSON loadedGame, GameLogic logic) throws CluedoException {
        List<Card> allCardsOfPlayers = new ArrayList<>();
        //Aktuelle Logic mit den Werten aus dem json befüllen
        Room[] weaponInRoomsFromJSON = convertToRoomArray(loadedGame, logic);
        Player[] playersFromJSON = convertToPlayerArray(loadedGame, logic, allCardsOfPlayers);
        int playerAmountFromJSON = playersFromJSON.length;
        CardTriple envelopeFromJSON = getEnvelope(logic.getCards(), allCardsOfPlayers);
        return new LoadedGameLogic(weaponInRoomsFromJSON, playersFromJSON, playerAmountFromJSON, envelopeFromJSON);

    }

    /**
     * Liefert den Umschlag/die Lösung des Mordes.
     *
     * @param allCards          alle im Spiel vorhandenen Karten.
     * @param allCardsOfPlayers alle Karten welche die Spieler auf der Hand haben.
     * @return der Umschlag mit der Lösung.
     */
    private static CardTriple getEnvelope(Card[] allCards, List<Card> allCardsOfPlayers) {
        boolean weaponFound = false;
        boolean roomFound = false;
        boolean characterFound = false;
        CardTriple envelope = new CardTriple();
        assert (allCardsOfPlayers.size() + CardTriple.TRIPLE_SIZE == allCards.length);
        for (Card currCard : allCards) {
            if (!allCardsOfPlayers.contains(currCard)) {
                switch (currCard.getType()) {
                    case CHARACTER:
                        if (!characterFound) {
                            envelope.setCharacter(currCard);
                            characterFound = true;
                        }
                        break;
                    case ROOM:
                        if (!roomFound) {
                            envelope.setRoom(currCard);
                            roomFound = true;
                        }
                        break;
                    case WEAPON:
                        if (!weaponFound) {
                            envelope.setWeapon(currCard);
                            weaponFound = true;
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Karte von ungültigem Typen in logic.cards[]");
                }
            }
        }
        return envelope;
    }

    /**
     * Validiert die Position der geladenen Spieler.
     *
     * @param logic          die Hauptspiellogik.
     * @param playerPosition die Position des geladenen Spielers.
     * @param supposedRoom   der Raum in dem er sich befinden soll.
     * @throws CluedoException falls die Position des Spielers nicht mit dem Raum übereinstimmt,
     *                         oder garnicht vorhanden ist.
     */
    private static void validatePlayerPosition(GameLogic logic, Position playerPosition, String supposedRoom) throws CluedoException {
        if (playerPosition == null) {
            throw new CluedoException(ExceptionType.NullInField);
        }
        GameCell playerGameCell = logic.getGameCell(playerPosition);
        if (playerGameCell.isRoom()) {
            if (!playerGameCell.getRoom().getName().equals(supposedRoom)) {
                throw new CluedoException(ExceptionType.PlayerToLoadInWrongRoom);
            }
            if (!playerPosition.equals(logic.getRoomByName(supposedRoom).getMidPoint())) {
                throw new CluedoException(ExceptionType.PlayerToLoadNotInRoomCenter);
            }
        } else if (playerGameCell.isCorridor()) {  //Korridor
            if (supposedRoom == null || !supposedRoom.equals("Flur")) {
                throw new CluedoException(ExceptionType.PlayerToLoadInWrongRoom);
            }
        } else { //Wand
            throw new CluedoException(ExceptionType.PlayerToLoadInWall);
        }
    }

    /**
     * Konvertiert die geladenen Daten in das für die Spiellogik notwendige Format.
     *
     * @param loadedGame        das geladene Spiel.
     * @param logic             die Hauptspiellogik.
     * @param allCardsOfPlayers alle Karten aller im Spiel vorhandener Spieler.
     * @return die Spieler in "Logikformat"
     * @throws CluedoException falls Spieler nicht vorhanden oder etwas mit ihren Daten nicht stimmt.
     */
    private static Player[] convertToPlayerArray(GameDataJSON loadedGame, GameLogic logic, List<Card> allCardsOfPlayers) throws CluedoException {
        PlayerJSON[] playerFromJSON = loadedGame.getPlayers();
        if (playerFromJSON == null) {
            throw new CluedoException(ExceptionType.NullInField);
        }
        NoteJSON[][] notesFromJSON = loadedGame.getNotes();
        if (notesFromJSON == null) {
            throw new CluedoException(ExceptionType.NullInField);
        }
        Player[] playersInGameResult = new Player[playerFromJSON.length];
        for (int playerToLoadIndex = 0; playerToLoadIndex < playerFromJSON.length; playerToLoadIndex++) {
            PlayerJSON playerToLoad = playerFromJSON[playerToLoadIndex];
            if (playerToLoad == null) {
                throw new CluedoException(ExceptionType.NullInField);
            }
            //Anhand vom Namen den character holen
            Character playerCharacter = logic.getCharacterByName(playerToLoad.getName());
            //Anhand vom iq die richtige AI holen
            AIDifficulty playerAIDifficulty = null;
            if (playerToLoadIndex != 0) { //Sonderfall, da Ming laut Aufgabenstellung der menschl. Spieler an erster Stelle gespeichert wird
                playerAIDifficulty = fromStringToAIDifficulty(playerToLoad.getIq());
            } else {
                fromStringToAIDifficulty(playerToLoad.getIq()); // nur zur Überprüfung
            }
            //Die Position holen
            Position playerPosition = playerToLoad.getPosition();
            validatePlayerPosition(logic, playerPosition, playerToLoad.getRoom());
            //Ob gewünscht schreiben
            boolean playerRequested = playerToLoad.getRequested();
            if (playerRequested && !logic.getGameCell(playerPosition).isRoom()) {
                throw new CluedoException(ExceptionType.PlayerToLoadRequestedButNotInRoom);
            }
            //Über alle Karten in JSON Spieler und anhand der Namen die Karten holen
            if (playerToLoad.getCards() == null
                    || playerToLoad.getCards().getWeapons() == null
                    || playerToLoad.getCards().getRooms() == null
                    || playerToLoad.getCards().getPersons() == null
            ) {
                throw new CluedoException(ExceptionType.NullInField);
            }
            List<Card> playerCards = convertToListOfCards(logic, playerToLoad.getCards());
            allCardsOfPlayers.addAll(playerCards);
            NoteJSON[] playerOwnNotesToLoad = getOwnNotesByPlayerIndex(notesFromJSON, playerToLoadIndex);
            //Die eigenen Notizen aus dem notesFromJSON holen und in ein neues NoteSelf[] schreiben
            NoteJSON playerOwnNoteToLoad = getOwnNoteByName(playerOwnNotesToLoad, playerCharacter.getName());
            NoteSelf[] playerOwnNotes = convertToNoteSelf(logic, playerOwnNoteToLoad);

            NoteJSON[] playerNoteOthersToLoad = notesFromJSON[playerToLoadIndex];
            NoteOthers[][] playerNoteOthers = convertToNoteOthers(logic, playerNoteOthersToLoad, playerCharacter.getName());

            //Spieler konstruieren und in Array
            //List<Card> cards, Position pos, AIDifficulty aiDifficulty, Character character, NoteSelf[] noteSelf, boolean requested, NoteOthers[][] noteOthers
            playersInGameResult[playerToLoadIndex] = new Player(playerCards, playerPosition, playerAIDifficulty, playerCharacter, playerOwnNotes, playerRequested, playerNoteOthers);
        }
        return playersInGameResult;
    }

    /**
     * Konvertiert und validiert die geladenen Daten in die Notizen über andere Spieler.
     *
     * @param logic                  die Hauptpiellogik.
     * @param playerNoteOthersToLoad die Notizen welche zu konvertieren sind.
     * @param playerToLoadName       der Name des Spielers dem die Notizen gehören sind.
     * @return die Notizen in "Logikformat"
     * @throws CluedoException falls bei der konvertierung Validierungsfehler auftreten.
     */
    private static NoteOthers[][] convertToNoteOthers(GameLogic logic, NoteJSON[] playerNoteOthersToLoad, String playerToLoadName) throws CluedoException {
        NoteOthers[][] noteOthers = new NoteOthers[playerNoteOthersToLoad.length - 1][]; // - 1 Da die eigenen nicht zu Others Zählen
        int noteOthersIndex = 0;
        for (int currNoteToLoadIndex = 0; currNoteToLoadIndex < playerNoteOthersToLoad.length; currNoteToLoadIndex++) {
            NoteJSON currNoteToLoad = playerNoteOthersToLoad[currNoteToLoadIndex];
            if (!currNoteToLoad.getName().equals(playerToLoadName)) { //Nur über die anderen
                noteOthers[noteOthersIndex] = convertToNoteOthers(logic, currNoteToLoad);
                noteOthersIndex++;
            }
        }
        return noteOthers;
    }

    /**
     * Konvertiert die Notizen über einen Bestimmten Spieler.
     *
     * @param logic               die Hauptspiellogik.
     * @param playerOwnNoteToLoad Notiz im Speicherformat welche konvertiert werden soll.
     * @return die Notiz über diesen Spieler in "Spiellogikformat"
     * @throws CluedoException falls bei der validierung Fehler auftreten.
     */
    private static NoteOthers[] convertToNoteOthers(GameLogic logic, NoteJSON playerOwnNoteToLoad) throws CluedoException {
        String[] charactersToLoad = playerOwnNoteToLoad.getMemo().getPersons();
        String[] weaponsToLoad = playerOwnNoteToLoad.getMemo().getWeapons();
        String[] roomsToLoad = playerOwnNoteToLoad.getMemo().getRooms();
        int noteLength = charactersToLoad.length + weaponsToLoad.length + roomsToLoad.length;
        //Notizenlänge muss übereinstimmen
        if (logic.getPlayers()[0].getNoteOthers()[0].length != noteLength) {
            throw new CluedoException(ExceptionType.NoteOthersLength);
        }
        NoteOthers[] result = new NoteOthers[noteLength];
        mergeNoteOthers(result, charactersToLoad, weaponsToLoad, roomsToLoad);
        return result;
    }

    /**
     * Konvertiert eine gespeicherte Notiz zu den eigenen Notizen eines Spielers
     *
     * @param logic               die Hauptspiellogik.
     * @param playerOwnNoteToLoad die Notiz des Spieler über sich selber.
     * @return die Notiz in "Spiellogikformat".
     * @throws CluedoException falls bei der Validierung Fehler auftreten.
     */
    private static NoteSelf[] convertToNoteSelf(GameLogic logic, NoteJSON playerOwnNoteToLoad) throws CluedoException {
        if (playerOwnNoteToLoad.getMemo() == null) {
            throw new CluedoException(ExceptionType.NullInField);
        }
        String[] charactersToLoad = playerOwnNoteToLoad.getMemo().getPersons();
        String[] weaponsToLoad = playerOwnNoteToLoad.getMemo().getWeapons();
        String[] roomsToLoad = playerOwnNoteToLoad.getMemo().getRooms();
        if (charactersToLoad == null
                || weaponsToLoad == null
                || roomsToLoad == null) {
            throw new CluedoException(ExceptionType.NullInField);
        }
        int noteLength = charactersToLoad.length + weaponsToLoad.length + roomsToLoad.length;
        //Notizenlänge muss übereinstimmen
        if (logic.getPlayers()[0].getNoteSelf().length != noteLength) {
            throw new CluedoException(ExceptionType.NoteSelfLength);
        }
        NoteSelf[] result = new NoteSelf[noteLength];
        mergeNoteSelf(result, charactersToLoad, weaponsToLoad, roomsToLoad);
        return result;
    }

    /**
     * Konvertiert, validiert und bringt die die geladenen Notizen in die Reihenfolge,
     * wie diese in der Logik benötigt werden.
     *
     * @param result           die zu beschreibenden Notizen.
     * @param charactersToLoad die Personen, welche zu laden sind.
     * @param weaponsToLoad    die Waffen, welche zu laden sind.
     * @param roomsToLoad      die Räume welche zu laden sind.
     * @throws CluedoException falls bei der validierung Fehler auftreten.
     */
    private static void mergeNoteSelf(NoteSelf[] result, String[] charactersToLoad, String[] weaponsToLoad, String[] roomsToLoad) throws CluedoException {
        NoteSelf[] characters = new NoteSelf[charactersToLoad.length];
        NoteSelf[] weapons = new NoteSelf[weaponsToLoad.length];
        NoteSelf[] rooms = new NoteSelf[roomsToLoad.length];
        for (int i = 0; i < characters.length; i++) {
            characters[i] = NoteSelf.fromString(charactersToLoad[i]);
        }
        for (int i = 0; i < weapons.length; i++) {
            weapons[i] = NoteSelf.fromString(weaponsToLoad[i]);
        }
        for (int i = 0; i < rooms.length; i++) {
            rooms[i] = NoteSelf.fromString(roomsToLoad[i]);
        }
        System.arraycopy(characters, 0, result, 0, characters.length);
        System.arraycopy(weapons, 0, result, characters.length, weapons.length);
        System.arraycopy(rooms, 0, result, characters.length + weapons.length, rooms.length);
    }

    /**
     * Konvertiert, validiert und bringt die die geladenen Notizen über die anderen Spieler in die Reihenfolge,
     * wie diese in der Logik benötigt werden.
     *
     * @param result           die zu beschreibenden Notizen über andere Spieler.
     * @param charactersToLoad die Personen, welche zu laden sind.
     * @param weaponsToLoad    die Waffen, welche zu laden sind.
     * @param roomsToLoad      die Räume welche zu laden sind.
     * @throws CluedoException falls bei der validierung Fehler auftreten.
     */
    private static void mergeNoteOthers(NoteOthers[] result, String[] charactersToLoad, String[] weaponsToLoad, String[] roomsToLoad) throws CluedoException {
        NoteOthers[] characters = new NoteOthers[charactersToLoad.length];
        NoteOthers[] weapons = new NoteOthers[weaponsToLoad.length];
        NoteOthers[] rooms = new NoteOthers[roomsToLoad.length];
        for (int i = 0; i < characters.length; i++) {
            characters[i] = NoteOthers.fromString(charactersToLoad[i]);
        }
        for (int i = 0; i < weapons.length; i++) {
            weapons[i] = NoteOthers.fromString(weaponsToLoad[i]);
        }
        for (int i = 0; i < rooms.length; i++) {
            rooms[i] = NoteOthers.fromString(roomsToLoad[i]);
        }
        System.arraycopy(characters, 0, result, 0, characters.length);
        System.arraycopy(weapons, 0, result, characters.length, weapons.length);
        System.arraycopy(rooms, 0, result, characters.length + weapons.length, rooms.length);
    }


    /**
     * Liefert die eigenen Notizen über den übergebenen Namen.
     *
     * @param playerOwnNotesToLoad die Notizen, welche für diesen Spieler zu laden sind.
     * @param playerToLoadName     der Name des Spielers, dessen eigene Notizen in dem übergebeben Array
     *                             an geladenen Notizen gefunden werden soll.
     * @return die Notizen des Spielers in Speicherformat.
     * @throws CluedoException falls die Notiz in falschem Format
     */
    private static NoteJSON getOwnNoteByName(NoteJSON[] playerOwnNotesToLoad, String playerToLoadName) throws CluedoException {
        if (playerOwnNotesToLoad == null) {
            throw new CluedoException(ExceptionType.NullInField);
        }
        for (NoteJSON currNote : playerOwnNotesToLoad) {
            if (currNote != null && currNote.getName().equals(playerToLoadName)) {
                return currNote;
            }
        }
        throw new CluedoException(ExceptionType.NoteOthersWrongFormat);
    }

    /**
     * Liefert die eigenen Notizen des Spielers ausgehen von dem Index des Spielers im Spielerarray
     * der Spiellogik.
     *
     * @param notesFromJSON     die geladenen Notizen aller Spieler.
     * @param playerToLoadIndex der Index des zu ladenen Spielers.
     * @return die Notizen des Spielers.
     */
    private static NoteJSON[] getOwnNotesByPlayerIndex(NoteJSON[][] notesFromJSON, int playerToLoadIndex) {
        return notesFromJSON[playerToLoadIndex];
    }

    /**
     * @param logic die Hauptspiellogik.
     * @param cards die geladenen Karten eines Spielers.
     * @return die Karten des Spielers als Liste für die Logik.
     * @throws CluedoException falls beim laden Validierungsfehler auftreten.
     */
    private static List<Card> convertToListOfCards(GameLogic logic, CardsJSON cards) throws CluedoException {
        List<Card> result = new ArrayList<>();
        for (String person : cards.getPersons()) {
            result.add(logic.getCardByName(person));
        }
        for (String room : cards.getRooms()) {
            result.add(logic.getCardByName(room));
        }
        for (String weapon : cards.getWeapons()) {
            result.add(logic.getCardByName(weapon));
        }
        return result;
    }

    /**
     * Konvertiert die Räume aus der Spielstandsdatei in die Räume für die Spiellogik.
     *
     * @param loadedGame die geladene Spielstandsdatei.
     * @param logic      die Haupspiellogik.
     * @return die Räume, wie diese in der Logik liegen sollen.
     * @throws CluedoException falls bei der konvertierung Validierungsfehler auftreten.
     */
    private static Room[] convertToRoomArray(GameDataJSON loadedGame, GameLogic logic) throws CluedoException {
        if (loadedGame == null) {
            throw new CluedoException(ExceptionType.NullInField);
        }
        WeaponJSON[] weaponInRoomJSON = loadedGame.getWeapons();
        if (weaponInRoomJSON == null) {
            throw new CluedoException(ExceptionType.NullInField);
        }
        if (weaponInRoomJSON.length != logic.getWeapons().length) {
            throw new CluedoException(ExceptionType.WeaponsInRoomsLength);
        } else {
            Room[] weaponToRoomResult = new Room[logic.getWeapons().length];
            for (WeaponJSON currJSON : weaponInRoomJSON) {
                if (currJSON == null) {
                    throw new CluedoException(ExceptionType.NullInField);
                }
                int weaponIndex = logic.getWeaponIndexByName(currJSON.getName());
                Room room = logic.getRoomByName(currJSON.getRoom());
                weaponToRoomResult[weaponIndex] = room;
            }
            return weaponToRoomResult;
        }
    }


}
