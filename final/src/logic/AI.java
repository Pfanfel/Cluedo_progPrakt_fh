package logic;

import logic.exceptions.CluedoException;

import java.util.*;
import java.util.function.Function;

/**
 * Abstakte Klasse, welche die allgemeinen Methoden, welche für die KIs notwendig sind implementiert.
 * Enthält die Abstrakten Methoden, welche je nach erbender KI-Stärkte, unterschiedlich sind.
 *
 * @author Michael Smirnov
 */
public abstract class AI {
    /**
     * Liefert die KI Stärke der jew. Klasse.
     *
     * @return die KI Stärke.
     */
    public abstract AIDifficulty getDifficulty();

    /**
     * Berechnet das Ziel des Zuges eines KI-Spielers.
     * Je nachdem um welche KI-Stärke es sich handelt, ist diese Berechung unterschiedlich.
     *
     * @param logic  die Hauptspiellogik.
     * @param player der Spieler welcher gerade an der Reihe ist.
     * @param steps  die zur verfügung stehenden Schritte.
     * @return die Zielposition, falls keine berechenbar null.
     */
    public abstract Position computeNextMove(GameLogic logic, Player player, int steps);

    /**
     * Äußert eine Verdächtigung.
     * Je nachdem um welche KI-Stärke es sich handelt, ist diese Entscheidungsfindung unterschiedlich.
     *
     * @param logic           die Hauptspiellogik.
     * @param currentPlayer   der Spieler welcher gerade an der Reihe ist.
     * @param enteredRoomCard die Karte des Raumes, in dem sich die KI gerade befindet.
     * @return Drei Karten unterschiedlichen Kartentyps, welche die Verdächtigung darstellen.
     */
    public abstract CardTriple expressSuspicion(GameLogic logic, Player currentPlayer, Card enteredRoomCard);

    /**
     * Zeigt als Antwort/Konter auf eine Verdächtigung eine der in der Verdächtigung genannten Karten.
     * Falls keine der Karten auf der Hand sind, wird null zurückgegeben.
     * Je nachdem um welche KI-Stärke es sich handelt, ist diese Entscheidungsfindung unterschiedlich.
     *
     * @param players         alle Spieler im Spiel.
     * @param cards           alle Karten im Spiel.
     * @param currentPlayer   der aktuelle Spieler der die Verdächtigung geäußert hat.
     * @param self            der KI-Spieler selbst.
     * @param suspicionResult die vom aktuellen Spieler geäußerte Verdächtigung.
     * @return Die Karte, welche die Antwort/den Konter auf eine Verdächtigung darstellt.
     * Falls keine vorhanden, null.
     */
    public abstract Card showCard(Player[] players, Card[] cards, Player currentPlayer, Player self, CardTriple suspicionResult);

    /**
     * Auf eine Verdächtigung des aktuellen KI-Spielers hin, werden ihm die Antworten/Konter der anderen
     * mitspieler gezeigt. Die KI notiert sich die gezeigten Karten in ihre Notizen.
     * Je nachdem um welche KI-Stärke es sich handelt, werden zusätzlich Rückschlüsse daraus gezogen.
     *
     * @param players       alle Spieler im Spiel.
     * @param cards         alle Karten im Spiel.
     * @param currentPlayer der KI-Spieler selbst.
     * @param suspicion     die Verdächtigung auf die die Antworten gezeigt werden.
     * @param shownCards    die gezeigten Karten.
     */
    public abstract void getCardsShown(Player[] players, Card[] cards, Player currentPlayer, CardTriple suspicion, Card[] shownCards);

    //Schlauer KI-Spieler macht sich zu dazu Notizen ob jemand Karten gezeigt hat und schließt dementsprechend Karten aus

    /**
     * Die KI bekommt mit, wer alles bei einer Verdächtigung, welche nicht von der KI-Selbst, sondern
     * von einem anderen Spieler geäußert wurde Karten gezeigt hat.
     * Nur die schlaue KI zieht daraus Rückschlüsse, und merkt sich wer, auf welche Verdächtigung hin,
     * etwas gezeigt hat oder nicht. Dementsprechend zieht die schlaue KI daraus Rückschlüsse.
     *
     * @param allPlayers    alle Spieler im Spiel.
     * @param allCards      alle Karten im Spiel.
     * @param myself        der KI-Spieler selbst.
     * @param currentPlayer der aktuelle Spieler, der die aktuelle Verdächtigung geäußert hat.
     * @param suspicion     die vom aktuellen Spieler geäußerte Verdächtigung.
     * @param shownCards    die von den Spielern gezeigten karten. Die KI darf jedoch nur auswerten ob
     *                      eine Karte vorhanden ist oder nicht.
     */
    public abstract void watchCardsGetShown(Player[] allPlayers, Card[] allCards, Player myself, Player currentPlayer, CardTriple suspicion, Card[] shownCards);

    /**
     * Äußert eine Anklage, aber nur, wenn nur noch eine Waffe, Raum und Person offen sind.
     *
     * @param logic  die Hauptspiellogik.
     * @param player der KI-Spieler selbst.
     * @return falls mögl. eine Anklage die das Spiel beendet. Sollte nur die korrekte Lösung liefern.
     * @throws CluedoException falls die Karte nicht gefunden wurde.
     */
    public CardTriple expressAccusation(GameLogic logic, Player player) throws CluedoException {
        Set<Room> openRooms = getOpenRooms(logic, player);
        Set<Card> openWeaponCards = getOpenWeaponCards(logic, player);
        Set<Card> openCharacterCards = getOpenCharacterCards(logic, player);
        CardTriple accusation = null;
        if (openRooms.size() == 1 && openWeaponCards.size() == 1 && openCharacterCards.size() == 1) {
            Card roomCardSolution = logic.getCardByName(openRooms.iterator().next().getName());
            Card characterCardSolution = openCharacterCards.iterator().next();
            Card weaponCardSolution = openWeaponCards.iterator().next();
            accusation = new CardTriple(roomCardSolution, characterCardSolution, weaponCardSolution);
        }
        return accusation;
    }


    /**
     * Liefert die noch offenen/nicht ausgeschlossenen Räume in dem der Mord passiert ist.
     * Offen sind die, die weder auf der Hand noch bereits gezeigt wurden.
     *
     * @param logic         die Hauptspiellogik.
     * @param currentPlayer der KI-Spieler selbst.
     * @return liefert eine Menge von noch offenen Räumen.
     */
    private Set<Room> getOpenRooms(GameLogic logic, Player currentPlayer) {
        //Alle Räume im Spiel holen
        Set<Room> allRooms = new HashSet<>(Arrays.asList(logic.getRooms()));
        //Die Räume welche auf der Hand holen
        Set<Room> roomsFromCardsOnHand = Card.getRoomsFromCards(logic, currentPlayer.getCards());
        //Die Räume auf der Hand ausschliessen
        allRooms.removeAll(roomsFromCardsOnHand);
        //Die Räume die bereits gezeigt wurden ermitteln
        Set<Card> seenCards = getSeenCards(logic, currentPlayer.getNoteOthers());
        Set<Room> seenRooms = Card.getRoomsFromCards(logic, seenCards);
        //Die Räume die Bereits gezeigt wurden ausschließen
        allRooms.removeAll(seenRooms);
        //Übrigen sind die offenen
        return allRooms;
    }

    /**
     * Liefert die noch offenen Waffenkarten.
     * Offen sind die, die weder auf der Hand noch bereits gezeigt wurden.
     *
     * @param logic         die Hauptspiellogik.
     * @param currentPlayer der KI-Spieler selbst.
     * @return liefert eine Menge von noch offenen Waffenkarten.
     */
    protected Set<Card> getOpenWeaponCards(GameLogic logic, Player currentPlayer) {
        return this.getOpenCards(logic, currentPlayer, Card::getWeaponCardsFromCards);
    }

    /**
     * Liefert die noch offenen Personenkarten.
     * Offen sind die, die weder auf der Hand noch bereits gezeigt wurden.
     *
     * @param logic         die Hauptspiellogik.
     * @param currentPlayer der KI-Spieler selbst.
     * @return liefert eine Menge von noch offenen Personenkarten.
     */
    protected Set<Card> getOpenCharacterCards(GameLogic logic, Player currentPlayer) {
        return this.getOpenCards(logic, currentPlayer, Card::getCharacterCardsFromCards);
    }

    /**
     * Liefert eine Menge an offenen Karten, welche nach dem übergebenen Filter aussortiert wurden.
     * Offen sind die, die weder auf der Hand noch bereits gezeigt wurden.
     *
     * @param logic         die Hauptspiellogik.
     * @param currentPlayer der KI-Spieler selbst.
     * @param filter        Eine Fuktion, die eine Collection von Karten entgegen nimmt,
     *                      filtert und ein Set von Karten zurückliefert.
     * @return eine Menge an noch offenen Karten.
     */
    private Set<Card> getOpenCards(GameLogic logic, Player currentPlayer, Function<? super Collection<Card>, ? extends Set<Card>> filter) {
        //Alle Karten die es gibt holen
        Set<Card> allCards = new HashSet<>(Arrays.asList(logic.getCards()));
        //Davon nur Character übrig lassen
        Set<Card> allCharacters = filter.apply(allCards);
        //Alle Karten auf der Hand
        List<Card> allCardsOnHand = currentPlayer.getCards();
        //Nur Character aus den Karten holen
        Set<Card> characterOnHand = filter.apply(allCardsOnHand);
        //Chararacter auf der Hand aussließen
        allCharacters.removeAll(characterOnHand);
        //Gesehene Karten Holen
        Set<Card> seenCards = getSeenCards(logic, currentPlayer.getNoteOthers());
        //Davon die gesehenen Character extrahieren
        Set<Card> seenCharacters = filter.apply(seenCards);
        //Diese aussließen
        allCharacters.removeAll(seenCharacters);
        //Die übrigen sind die offenen.
        return allCharacters;
    }

    /**
     * Liefert die erste Karte aus dem übergebenen Set nach der Reihenfolge der Notizen/Gesamtkarten.
     * Null, wenn keine der Karten im Set.
     *
     * @param allCards alle Karten im Spiel
     * @param cards    Karten von denen die erste gesucht wird.
     * @return die erste Karte nach der Reihenfolge der Notizen/Gesamtkarten.
     * Null, wenn keine der Karten im Set.
     */
    protected Card getFirstCardByNoteOrder(Card[] allCards, Set<Card> cards) {
        for (Card card : allCards) {
            if (cards.contains(card)) {
                return card;
            }
        }
        return null;
    }

    /**
     * Liefert die letzte Karte aus dem übergebenen Set nach der Reihenfolge der Notizen/Gesamtkarten.
     * Null, wenn keine der Karten im Set.
     *
     * @param allCards alle Karten im Spiel
     * @param cards    Karten von denen die letzte gesucht wird.
     * @return die letzte Karte nach der Reihenfolge der Notizen/Gesamtkarten.
     * Null, wenn keine der Karten im Set.
     */
    private Card getLastCardByNoteOrder(Card[] allCards, Set<Card> cards) {
        for (int i = allCards.length - 1; i >= 0; i--) {
            if (cards.contains(allCards[i])) {
                return allCards[i];
            }
        }
        return null;
    }


    /**
     * Liefert den Kürzesten Weg zu dem übergebenen Raum. Berücksichtigt dabei andere Spieler.
     *
     * @param logic         die Hauptspiellogik.
     * @param dest          der Zielraum
     * @param playerPostion die aktuelle Spielerposition.
     * @return der Pfad beginnend bei der Startposition und endend bei der Tür in den Raum.
     * Null, wenn keiner vorhanden.
     */
    private List<Position> getShortestPathToRoom(GameLogic logic, Room dest, Position playerPostion) {
        GameCell currentPlayerCell = logic.getGameCell(playerPostion);
        List<Position> shortestPath = null;
        if (currentPlayerCell.isRoom()) {
            int shortestPathLength = Integer.MAX_VALUE;
            for (Position start : logic.getOpenDoors(currentPlayerCell.getRoom())) {//Jede freie Tür kann als startpunkt gewählt werden
                List<Position> currentPath = logic.getShortestPath(start, logic.getOpenDoors(dest));
                if (currentPath != null && currentPath.size() < shortestPathLength) {
                    shortestPathLength = currentPath.size();
                    shortestPath = currentPath;
                }
            }
        } else {
            shortestPath = logic.getShortestPath(playerPostion, logic.getOpenDoors(dest));
            if (shortestPath != null) {
                shortestPath.remove(0); // Die eigene Position wieder entfernen#
            }
        }
        return shortestPath;
    }

    /**
     * Liefert den kürzesten Weg zu den im Set übergebenen Räumen.
     *
     * @param logic          die Hauptspiellogik.
     * @param rooms          die Räume zu denen der kürzeste Weg berechnet werden soll.
     * @param playerPosition die aktuelle Spielerposition.
     * @return der kürzeste Pfad aus der Menge an Räumen, beginnend bei der Startposition und
     * endend bei der Tür in den Raum. Null wenn keiner vorhanden.
     */
    private List<Position> getShortestPathToRooms(GameLogic logic, Set<Room> rooms, Position playerPosition) {
        List<Position> shortestPath = null;
        int shortestPathLenght = Integer.MAX_VALUE;
        for (Room room : rooms) {
            List<Position> shortestPathToCurrentRoom = getShortestPathToRoom(logic, room, playerPosition);
            if (shortestPathToCurrentRoom != null && shortestPathToCurrentRoom.size() < shortestPathLenght) {
                shortestPathLenght = shortestPathToCurrentRoom.size();
                shortestPath = shortestPathToCurrentRoom;
            }
        }
        return shortestPath;
    }


    /**
     * Liefert eine Menge an bereits gesehenen Karten.
     *
     * @param logic die Hauptspiellogik.
     * @param notes die Notizen über andere Spieler, aus denen die bereits gesehenen Karten extrahiert
     *              werden sollen.
     * @return ein Set an gesehenen Karten, leer wenn keine gestehen.
     */
    private Set<Card> getSeenCards(GameLogic logic, NoteOthers[][] notes) {
        Set<Card> seenCards = new HashSet<>();
        for (NoteOthers[] otherPlayers : notes) { //Notizen über die Anderen Spieler(Spalten)
            for (int noteIndex = 0; noteIndex < otherPlayers.length; noteIndex++) {
                if (otherPlayers[noteIndex].equals(NoteOthers.SEEN)) {
                    seenCards.add(logic.getCardByNoteIndex(noteIndex));
                }
            }
        }
        return seenCards;
    }

    //Methode, die gegeben einem Spieler eine Notiz in den Notizen über andere macht

    /**
     * Trägt eine übergebene Notiz in den Notizen eines Spielers über einen anderen Spieler ein.
     *
     * @param players         alle Spieler.
     * @param cards           alle Karten.
     * @param self            der KI-Spieler.
     * @param targetCharacter die Person, über die eine Notiz getätigt werden soll.
     * @param targetCard      die Karten, über die eine Notiz getätigt werden soll.
     * @param noteToTake      die Notiz, welche über den Spieler und Karte eingetragen werden soll.
     */
    protected void takeNote(Player[] players, Card[] cards, Player self, Character targetCharacter, Card targetCard, NoteOthers noteToTake) {
        int characterIndexInNotes = getCharacterIndexInNotes(players, self, targetCharacter);
        assert characterIndexInNotes != -1;
        int cardIndexInNotes = getCardIndexInNotes(cards, targetCard);
        assert cardIndexInNotes != -1;
        NoteOthers[][] notes = self.getNoteOthers();
        notes[characterIndexInNotes][cardIndexInNotes] = noteToTake;
    }

    /**
     * Liefert den Index einer Karte (Zeile) in den Notizen.
     *
     * @param cards      alle Karten. (Reihenfolge wie in Notizen)
     * @param targetCard die Karte zu der der Index gesucht wird.
     * @return der Index der gesuchten Karte. Liefert -1, falls Karte nicht gefunden.
     */
    protected int getCardIndexInNotes(Card[] cards, Card targetCard) {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i].equals(targetCard)) {
                return i;
            }
        }
        //Sollte nie Passieren
        return -1;
    }

    /**
     * Liefert den Index einer Person (Spalte) in den Notizen.
     *
     * @param players         alle Spieler.
     * @param self            der eigene Spieler.
     * @param targetCharacter die Person zu der der Index in den Notizen gesucht wird.
     * @return der Index der gesuchten Person. Liefert -1, falls Person nicht gefunden.
     */
    protected int getCharacterIndexInNotes(Player[] players, Player self, Character targetCharacter) {
        Character ownCharacter = self.getCharacter();
        int playerInNoteIndex = 0;
        for (Player player : players) {
            if (!player.getCharacter().equals(ownCharacter)) {
                if (player.getCharacter().equals(targetCharacter)) {
                    return playerInNoteIndex;
                }
                playerInNoteIndex++;
            }
        }
        //Sollte nie Passieren
        return -1;
    }


    /**
     * Liefert die erste offene Waffenkarte eines Spielers nach der Reihenfolge in den Notizen.
     *
     * @param logic         die Hauptspiellogik.
     * @param currentPlayer der Spieler, dessen erste offene Waffenkarte gesucht wird.
     * @return die erste offene Waffenkarte oder null.
     */
    protected Card suspectFirstWeapon(GameLogic logic, Player currentPlayer) {
        Set<Card> openWeaponCards = getOpenWeaponCards(logic, currentPlayer);
        return getFirstCardByNoteOrder(logic.getCards(), openWeaponCards);
    }

    /**
     * Liefert die erste offene Personenkarte eines Spielers nach der Reihenfolge in den Notizen.
     *
     * @param logic         die Hauptspiellogik.
     * @param currentPlayer der Spieler, dessen erste offene Personenkarte gesucht wird.
     * @return die erste offene Personenkarte oder null.
     */
    private Card suspectFirstCharacter(GameLogic logic, Player currentPlayer) {
        Set<Card> openCharacterCards = getOpenCharacterCards(logic, currentPlayer);
        return getFirstCardByNoteOrder(logic.getCards(), openCharacterCards);
    }


    /**
     * Liefert die letzte offene Personenkarte eines Spielers nach der Reihenfolge in den Notizen.
     *
     * @param logic         die Hauptspiellogik.
     * @param currentPlayer der Spieler, dessen letzte offene Personenkarte gesucht wird.
     * @return die letzte offene Personenkarte oder null.
     */
    protected Card suspectLastCharacter(GameLogic logic, Player currentPlayer) {
        Set<Card> openCharacterCards = getOpenCharacterCards(logic, currentPlayer);
        return getLastCardByNoteOrder(logic.getCards(), openCharacterCards);
    }

    /**
     * Äußert eine Verdächtigung gegeben der vorgaben der normalen KI-Stärke.
     * <p>
     * "Bei einer Verdächtigung werden die Personen diesmal von oben nach unten verdächtigt,
     * um somit möglichst andere Spieler von ihrer aktuellen Position "wegzuwünschen".
     * Die Waffen werden weiterhin ebenfalls von oben nach unten verdächtigt.
     * Auch hier soll wieder nur nach noch "offenen" Karten gefragt werden."
     *
     * @param logic           die Hauptspiellogik.
     * @param currentPlayer   der KI-Spieler selbst.
     * @param enteredRoomCard der für die Verdächtigung betretene Raum als Karte.
     * @return die Verdächtigung
     */
    protected CardTriple expressSuspicionNormal(GameLogic logic, Player currentPlayer, Card enteredRoomCard) {
        CardTriple suspicion = new CardTriple();
        suspicion.setRoom(enteredRoomCard);
        suspicion.setWeapon(suspectFirstWeapon(logic, currentPlayer));
        suspicion.setCharacter(suspectFirstCharacter(logic, currentPlayer));

        return suspicion;
    }

    /**
     * Berechnet das Ziel des Zuges eines dummen KI-Spielers nach Aufgabenstellung:
     * "Die "dumme" KI geht von der aktuellen Position aus immer zum nähesten Raum, der noch offen ist
     * (also bisher nicht als Tatraum ausgeschlossen werden konnte).
     * Falls der KI-Spieler sich aktuell in einem Raum befindet, wird dieser nicht mit in die Betrachtung einbezogen,
     * welcher Raum der "näheste" ist.
     * Ansonsten ist hier ein Wegfindealgorithmus umzusetzen, der über den Flur (aber ohne Geheimgänge) die Entfernungen zu anderen Räumen berechnet.
     * Andere Spieler, die auf dem Flur stehen, müssen dabei berücksichtigt werden.
     * Sie verlängern also ggf. den Weg bzw. machen ihn sogar unmöglich, wenn sie direkt vor einer Tür stehen.
     * Sollte nur noch ein einziger Raum "offen" sein, der Spieler befindet sich darin
     * und muß ihn verlassen, dann bewegt er sich zu einem zufälligen anderen Raum."
     *
     * @param logic  die Hauptspiellogik.
     * @param player der dumme KI-Spieler.
     * @param steps  die zur verfügung stehenden Schritte.
     * @return die Zielposition. Falls keine vorhanden, null.
     */
    //Liegt hier, da die normale und schlaue dieses Verfahren als fallback nutzen und dies
    //ebenfalls in dieser Klasse implementiert wurde.
    protected Position computeNextMoveStupid(GameLogic logic, Player player, int steps) {
        //Welche Räume sind noch offen?
        Set<Room> openRooms = getOpenRooms(logic, player);
        GameCell gameCell = logic.getGameCell(player.getPos());
        if (gameCell.isRoom()) { //Wenn aktuell in einem Raum, dann diesen aus der Betrachtung aussließen
            openRooms.remove(gameCell.getRoom());
        }
        Position nextMove;
        List<Position> shortestPathToRoom;

        //Welcher dieser Wege ist am Kürzesten?
        //Liefert den kürzesten Weg zum nähsten Raum in der Reihenfolge: Aktuelle position -> Ziel(Tür vor nähstem Raum)
        shortestPathToRoom = getShortestPathToRooms(logic, openRooms, player.getPos());
        if (shortestPathToRoom == null) {
            //Falls nur noch ein Raum offen ist UND der Spieler sich darin befindet, muss er ihn verlassen und sich zu einem zufälligen anderen Raum bewegen
            // Das passiert auch, wenn er sich zu keinem anderem offenem Raum bewegen kann
            Room randomDestination = logic.getRandomRoom(gameCell.getRoom());
            shortestPathToRoom = getShortestPathToRoom(logic, randomDestination, player.getPos());
        }

        if (shortestPathToRoom != null) {//Es gibt einen Weg
            Room destination = logic.getRoomFromDoor(shortestPathToRoom.get(shortestPathToRoom.size() - 1)); //Letzes Element ist das Ziel (Tür zum Raum)
            if (steps >= shortestPathToRoom.size()) { //Ein Schritt muss über bleiben um in den Raum zu gelangen. Im path ist die Startposition enthalten also reicht <= aus.
                nextMove = destination.getMidPoint();
            } else { //Wenn nicht genug schritte da sind, um den ausgewählten Raum zu erreichen
                nextMove = shortestPathToRoom.get(steps - 1); //Da das 0te Element der erste Schritt ist, kann mit der Anzahl der Schritte - 1 drauf zugegriffen werden.
            }
            GameLogic.debugln("Ich bin {" + player.getCharacter().getName() + "} Mein Ziel ist {" + destination.getName() + "} Benötigte Schritte: {" + shortestPathToRoom.size() + "} gewürfelt: {" + steps + "}");
        } else {  //Kein Weg wurde gefunden, da keiner mögl. ist (Türen im Raum versperrt oder eingekesselt)
            nextMove = null;
            GameLogic.debugln("Ich bin {" + player.getCharacter().getName() + "} und ich setze aus! gewürfelt: {" + steps + "}");
        }
        return nextMove;
    }

    /**
     * Berechnet das Ziel des Zuges eines normalen und schlauen KI-Spielers nach Aufgabenstellung:
     * "Die "normale" KI bewegt sich wie die "dumme" KI, berücksichtigt dabei allerdings auch Geheimgänge."
     *
     * @param logic  die Hauptspiellogik.
     * @param player der normale/schlaue KI-Spieler.
     * @param steps  die zur verfügung stehenden Schritte.
     * @return die Zielposition. Falls keine vorhanden, null.
     */
    protected Position computeNextMoveNormalAndSmart(GameLogic logic, Player player, int steps) {
        Position nextMove;
        Set<Room> openRooms = getOpenRooms(logic, player);
        GameCell playerGameCell = logic.getGameCell(player.getPos());
        if (playerGameCell.isRoom()) { //Wenn aktuell in einem Raum, dann diesen aus der Betrachtung aussließen
            openRooms.remove(playerGameCell.getRoom());
        }
        //Bin ich in einem Raum?
        if (playerGameCell.isRoom()
                //Ist in diesem Raum ein Geheimgang?
                && playerGameCell.getRoom().getSecretCorridor() != null
                //Führt der Geheimgang zu dem einem der Ziele in openRooms?
                && openRooms.contains(playerGameCell.getRoom().getSecretCorridor())) {
            Room secretCorridorDestination = playerGameCell.getRoom().getSecretCorridor();
            // Liefer den Mittelpunkt von dem über den Geheimgang erreichbaren Raum zurück
            GameLogic.debugln(player.getCharacter().getName() + " geht über den Geheimgang nach: " + secretCorridorDestination.getName());
            nextMove = secretCorridorDestination.getMidPoint();

        } else {
            nextMove = computeNextMoveStupid(logic, player, steps);
        }
        return nextMove;
    }

    /**
     * Macht Notizen für die übergebene KI zu den auf eine Verdächtigung hin gezeigten Karten.
     *
     * @param players       alle Spieler.
     * @param cards         alle Karten.
     * @param currentPlayer der KI-Spieler, welcher sich die Notizen machen soll.
     * @param suspicion     die von dem KI-Spieler ausgesprochene Verdächtigung.
     * @param shownCards    die dem KI-Spieler gezeigten Karten.
     */
    protected void takeNotesAboutShownCards(Player[] players, Card[] cards, Player currentPlayer, CardTriple suspicion, Card[] shownCards) {
        GameLogic.debugln("Ich bin { " + currentPlayer.getCharacter().getName() + " } und sollte mir notiert haben, dass ich { " + Arrays.toString(shownCards) + " } gezeigt bekommen habe");
        int shownCardsIndex = 0;
        for (Player playerWhoShown : players) {
            if (!playerWhoShown.equals(currentPlayer)) { //Nur über die anderen
                if (shownCards[shownCardsIndex] == null) {
                    //Hat keine der 3 die Verdächtigt wurden
                    takeNote(players, cards, currentPlayer, playerWhoShown.getCharacter(), suspicion.getCharacter(), NoteOthers.HAS_NOT);
                    GameLogic.debugln("Ich bin { " + currentPlayer.getCharacter().getName() + " } und notiere mir { " + playerWhoShown.getCharacter().getName() + " } hat keine { " + suspicion.getCharacter() + " }");
                    takeNote(players, cards, currentPlayer, playerWhoShown.getCharacter(), suspicion.getWeapon(), NoteOthers.HAS_NOT);
                    GameLogic.debugln("Ich bin { " + currentPlayer.getCharacter().getName() + " } und notiere mir { " + playerWhoShown.getCharacter().getName() + " } hat keine { " + suspicion.getWeapon() + " }");
                    takeNote(players, cards, currentPlayer, playerWhoShown.getCharacter(), suspicion.getRoom(), NoteOthers.HAS_NOT);
                    GameLogic.debugln("Ich bin { " + currentPlayer.getCharacter().getName() + " } und notiere mir { " + playerWhoShown.getCharacter().getName() + " } hat keine { " + suspicion.getRoom() + " }");
                } else {
                    //playerWhoShown hat mir die Karte shownCards[shownCardsIndex] gezeigt
                    takeNote(players, cards, currentPlayer, playerWhoShown.getCharacter(), shownCards[shownCardsIndex], NoteOthers.SEEN);
                    GameLogic.debugln("Ich bin { " + currentPlayer.getCharacter().getName() + " } und notiere mir { " + playerWhoShown.getCharacter().getName() + " } hat mir { " + currentPlayer.getCharacter().getName() + " } { " + shownCards[shownCardsIndex] + " } gezeigt");
                }
                shownCardsIndex++;
            }
        }
    }

    /**
     * Zeigt als Antwort/Konter auf eine Verdächtigung eine der in der Verdächtigung genannten Karten.
     * Nach den Vorgaben der normalen KI-Stärke:
     * <p>
     * "Beim Zeigen von Karten sollen solche Karten bevorzugt gezeigt werden,
     * die man vorher bereits gezeigt hat - je häufiger, desto besser.
     * ...
     * "Stehen dann mehrere Karten zur Auswahl, nimmt man die bisher schon am häufigsten gezeigte.
     * Bei mehreren gleich häufigen gilt wieder die Reihenfolge von oben nach unten."
     * <p>
     * Falls keine der Karten auf der Hand sind, wird null zurückgegeben.
     *
     * @param player          der normale KI-Spieler, der eine Karte zeigen soll.
     * @param allCards        alle Karten im Spiel.
     * @param suspicionResult die vom aktuellen Spieler geäußerte Verdächtigung.
     * @return Die Karte, welche die Antwort/den Konter auf eine Verdächtigung darstellt.
     * Falls keine vorhanden, null.
     */
    protected Card showCardNormal(Player player, Card[] allCards, CardTriple suspicionResult) {
        //Die mögl. finden die zeigbar sind
        CardTriple possibleCardsToShow = player.possibleCardsToShow(suspicionResult);
        Card cardToShow = null;
        //Frühzeigig abbrechen wenn nichts gezeigt werden kann
        if (!possibleCardsToShow.isEmpty()) {
            NoteSelf[] ownNotes = player.getNoteSelf();
            Set<Card> possibleCards = possibleCardsToShow.getCards();
            int maxShownFrequency = Integer.MIN_VALUE;
            Set<Card> mostShownCards = new HashSet<>();

            //Über alle Karten die man Zeigen könnte laufen
            for (Card card : possibleCards) {
                int currCardIndexInNotes = getCardIndexInNotes(allCards, card);
                NoteSelf currNote = ownNotes[currCardIndexInNotes];
                if (currNote != NoteSelf.NOTHING && currNote.ordinal() >= maxShownFrequency) {
                    if (currNote.ordinal() > maxShownFrequency) {
                        //Alle bisherigen löschen, da dijenigen die am häufigsten gezeigt wurden relevant
                        mostShownCards.clear();
                    }
                    mostShownCards.add(card);
                    maxShownFrequency = currNote.ordinal();
                }
            }
            cardToShow = getFirstCardByNoteOrder(allCards, mostShownCards);
            //Notiz machen, dass diese Karte gezeigt wurde
            GameLogic.debugln("Als normale KI { " + player.getCharacter().getName() + " } notiere ich mir, dass ich { " + cardToShow + " } einmal häufiger gezeigt habe");
            takeNoteCardShown(allCards, cardToShow, ownNotes);

        }
        return cardToShow;
    }

    /**
     * Trägt in den übergebenen eigenen Notizen ein, dass die übergebene
     * Karte nun einmal häufiger gezeigt wurde.
     *
     * @param cards      alle Karten.
     * @param cardToShow die gezeigte Karte.
     * @param ownNotes   die eigenen Notizen, in die die gezeigte Karte eingetragen werden soll.
     */
    protected void takeNoteCardShown(Card[] cards, Card cardToShow, NoteSelf[] ownNotes) {
        int cardToShowIndexInNotes = getCardIndexInNotes(cards, cardToShow);
        ownNotes[cardToShowIndexInNotes] = NoteSelf.increment(ownNotes[cardToShowIndexInNotes]);
    }


}
