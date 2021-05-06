package logic;

import java.util.*;

/**
 * Diese Klasse stellt die Implementierung der schlauen KI-Stärke dar.
 * Diese ist die einzige KI-Stärke, welche Instanzvariablen besitzt um sich Informationen, wie den Spielverlauf
 * und welchem Spieler sie welche Karte wie häufig gezeigt hat, speichert.
 * Diese Informationen gehen jedoch augrund der Vorgaben des Speicherformates beim Speichern verloren.
 * <p>
 * Zitat aus der Aufgabenstellung:
 * <p>
 * "Die "schlaue" KI bewegt sich genau wie die "normale" KI.
 * Bei einer Verdächtigung ist sie die einzige KI-Stärke,
 * die auch nach eigenen Karten fragen kann, um das Zeigen bestimmter anderer Karten zu erzwingen.
 * Konkret soll dies angewandt werden, wenn die Person und die Waffe schon festgelegt werden konnten
 * und nur noch der Raum fehlt: In diesem Fall soll die "schlaue" KI dann statt der tatsächlichen Tatperson
 * und -waffe soweit möglich nach eigenen Personen- und Waffenkarten fragen (wieder von oben nach unten),
 * um so die Mitspieler ggf. wegzuwünschen und auch nicht zu offensichtlich zu machen,
 * welches Wissen die KI schon hat. Ansonsten verhält die KI sich wie die "normale" KI bei Verdächtigungen.
 * Es soll sich aber zusätzlich gemerkt werden, welche Verdächtigungen die anderen Spieler ausgesprochen
 * und von welchen Spielern sie daraufhin Karten gesehen haben.
 * So läßt sich später ggf. auf einzelne Karten schließen, die dann "abgehakt" werden können.
 * Beim Zeigen von Karten soll die "schlaue" KI sich merken,
 * wem sie bereits welche Karten gezeigt hat und diese wenn möglich erneut zeigen.
 * Falls das nicht möglich ist, verhält sie sich wie die "normale" KI."
 *
 * @author Michael Smirnov
 */
public class AISmart extends AI {

    //Zählt wie häufig wem eine Karte gezeigt wurde, selbe indizierung wie bei NoteOthers
    //[Spielerindex][Kartenindex]
    private final int[][] shownCardsCount;
    //"bei mehreren eigenen Waffen- oder Personenkarten fragt die schlaue KI diese der Reihe nach ab."
    //Zähler der bei mehreren offenen Waffenkarten hochgezählt wird, um diese der Reihe nach zu zeigen.
    private int weaponOnHandCounter;
    //Zähler der bei mehreren offenen Personenkarten hochgezählt wird, um diese der Reihe nach zu zeigen.
    private int characterOnHandCounter;
    //Die Spielhistorie (enthält nich die Züge bei denen die KI selbst eine Verdächtigung geäußert hat)
    private final List<Turn> gameHistory;

    /**
     * Konstruktor der schlauen KI-Stärke.
     *
     * @param otherPlayerCount Anzahl der anderen Spieler im Spiel.
     * @param cardCount        die Anzahl an im Spiel befindlichen Karten.
     */
    public AISmart(int otherPlayerCount, int cardCount) {
        //-1 da man sich selbst keine Karten zeigen kann
        shownCardsCount = new int[otherPlayerCount - 1][cardCount];
        gameHistory = new ArrayList<>();
    }

    /**
     * Innere Statische Klasse welche einen Spielzug repräsentiert.
     */
    private static class Turn {
        //Die Person welche die Verdächtigung geäußert hat.
        private final Character suspector;
        //Die Verdächtigung die die Person geäußert hat.
        private final CardTriple suspicion;
        //Die daraufhin von den anderen teilnehmenden Spielern gezeigten Karten.
        //(Dürfen nur auf vorhandensein geprüft werden)
        //In derselben Reihefolge, wie die Notizen über andere Spieler
        private final Card[] cardsShown;

        /**
         * Konstruktor für einen Zug.
         *
         * @param suspector  die Person welche die Verdächtigung geäußert hat.
         * @param suspicion  die Verdächtigung die die Person geäußert hat.
         * @param cardsShown die daraufhin von den anderen teilnehmenden Spielern gezeigten Karten.
         */
        private Turn(Character suspector, CardTriple suspicion, Card[] cardsShown) {
            this.suspector = suspector;
            this.suspicion = suspicion;
            this.cardsShown = cardsShown;
        }

        /**
         * Liefert die Person welche die Verdächtigung geäußert hat.
         *
         * @return die Person welche die Verdächtigung geäußert hat.
         */
        private Character getSuspector() {
            return suspector;
        }

        /**
         * Liefert die Verdächtigung die die Person geäußert hat.
         *
         * @return die Verdächtigung die die Person geäußert hat.
         */
        private CardTriple getSuspicion() {
            return suspicion;
        }

        /**
         * Liefert die daraufhin von den anderen teilnehmenden Spielern gezeigten Karten.
         *
         * @return die daraufhin von den anderen teilnehmenden Spielern gezeigten Karten.
         */
        private Card[] getCardsShown() {
            return cardsShown;
        }
    }

    /**
     * Aufählung für aus der die temporären Notizen bestehen. Diese werden benutzt,
     * um Rückschlüsse auf die von anderen Spielern gezeigten Karten zu ziehen.
     */
    private enum TempNotes {
        HAS, HAS_NOT, UNKNOWN;

        @Override
        public String toString() {
            switch (this) {
                case HAS:
                    return "+";
                case HAS_NOT:
                    return "-";
                case UNKNOWN:
                    return "?";
                default:
                    throw new IllegalArgumentException("Existiert nicht");
            }
        }
    }

    @Override
    public AIDifficulty getDifficulty() {
        return AIDifficulty.SMART;
    }

    @Override
    public Position computeNextMove(GameLogic logic, Player player, int steps) {
        return computeNextMoveNormalAndSmart(logic, player, steps);
    }


    @Override
    public CardTriple expressSuspicion(GameLogic logic, Player currentPlayer, Card enteredRoomCard) {
        CardTriple suspicion = new CardTriple();
        suspicion.setRoom(enteredRoomCard);

        Set<Card> openWeaponCards = getOpenWeaponCards(logic, currentPlayer);
        Set<Card> openCharacterCards = getOpenCharacterCards(logic, currentPlayer);

        if (openWeaponCards.size() == 1 && openCharacterCards.size() == 1) { //Stehen Tatwaffe UND Person fest?
            GameLogic.debugln("Ich als Schlaue KI { " + currentPlayer.getCharacter().getName() + "} habe Tatwaffe und Person Festgelegt");
            //Kann nach eigenen Karten Fragen um Zeigen bestimmter Karten zu erzwingen, wenn Person und Waffe feststehen
            List<Card> allCardsOnHand = currentPlayer.getCards();
            List<Card> weaponsOnHand = new ArrayList<>(Card.getWeaponCardsFromCards(allCardsOnHand));
            List<Card> charactersOnHand = new ArrayList<>(Card.getCharacterCardsFromCards(allCardsOnHand));

            if (weaponsOnHand.isEmpty()) {//Keine Waffen auf der Hand
                //Nach Tatwaffe fragen
                suspicion.setWeapon(openWeaponCards.iterator().next());//Die Einzige die noch offen ist
            } else { //"bei mehreren eigenen Waffen- oder Personenkarten fragt die schlaue KI diese der Reihe nach ab."
                //3 Karten auf der hand
                //0%3 ->0 1%3->1 2%3->2 3%3->0 4%3->1 5%3->2 6%3->3 ...
                Card weaponToShow = weaponsOnHand.get(weaponOnHandCounter % weaponsOnHand.size());
                weaponOnHandCounter++;
                suspicion.setWeapon(weaponToShow);
            }
            if (charactersOnHand.isEmpty()) {//Keine Personen auf der Hand
                //Nach  Tatperson fragen
                suspicion.setCharacter(openCharacterCards.iterator().next());
            } else {
                //Diese der Reihe nach abfragen
                Card characterToShow = charactersOnHand.get(characterOnHandCounter % charactersOnHand.size());
                characterOnHandCounter++;
                suspicion.setCharacter(characterToShow);
            }
        } else { //Ansonsten wie die Normale KI, wenn Waffe UND Person noch nicht gefunden
            suspicion = expressSuspicionNormal(logic, currentPlayer, enteredRoomCard);
        }
        return suspicion;
    }

    /**
     * Liefert die Karten, welche dem übergebenen Spieler bisher am häufigsten gezeigt wurden.
     * Diese Methode nutzt nicht die eigenen Notizen, sondern die das interne int-Array.
     *
     * @param players         alle Spieler im Spiel.
     * @param cards           alle Karten im Spiel.
     * @param currentPlayer   der aktuelle Spieler, dem eine Karte gezeigt werden muss.
     * @param self            der Spieler der schlauen KI-Stärke.
     * @param suspicionResult die Verdächtigung die der aktuelle Spieler geäußert hat.
     * @return die Karten, welche dem aktullen Spieler am häufigsten gezeigt wurden.
     * Kann auch leer sein.
     */
    private Set<Card> getMostShownCardsToAskingPlayer(Player[] players, Card[] cards, Player currentPlayer, Player self, CardTriple suspicionResult) {
        //Die mögl. finden die zeigbar sind
        Set<Card> mostShownCards = new HashSet<>();
        Set<Card> possibleCardsToShow = self.possibleCardsToShow(suspicionResult).getCards();
        //Index des Fragen Spielers für das interne Array im dem die Anzahl gespeichert wir wie häufig wem was gezeigt wurde
        int askingPlayerIndex = getCharacterIndexInNotes(players, self, currentPlayer.getCharacter());
        int maxShownCount = Integer.MIN_VALUE;
        //Anzahl wie häufig eine Karte gezeigt wurde für jede mogl. Karte ermitteln(kann auch ein leeres Set sein)
        for (Card card : possibleCardsToShow) {
            int cardToShowIndex = getCardIndexInNotes(cards, card);
            int cardShownCount = shownCardsCount[askingPlayerIndex][cardToShowIndex];
            if (cardShownCount >= maxShownCount) {
                if (cardShownCount > maxShownCount) {
                    //Resetten, da vorherige nur nicht so häufig gezeigt wurden wie diese
                    mostShownCards.clear();
                }
                mostShownCards.add(card);
                maxShownCount = cardShownCount;

            }
        }
        return mostShownCards;
    }

    @Override
    public Card showCard(Player[] players, Card[] cards, Player currentPlayer, Player self, CardTriple suspicionResult) {
        Card cardToShow;
        //Interne Datenstruktur befragen WEM sie bereits wie HÄUFIG eine KARTE gezeigt hat und was die Häufigsten waren
        Set<Card> mostShownCards = getMostShownCardsToAskingPlayer(players, cards, currentPlayer, self, suspicionResult);
        GameLogic.debugln("Als schlaue KI { " + self.getCharacter().getName() + " } habe ich { " + currentPlayer.getCharacter().getName() + " } am häufigsten { " + mostShownCards + " } gezeigt");
        switch (mostShownCards.size()) {
            case 0:
                //Noch keine Karte Gezeigt->Wie Normale verhalten
                cardToShow = showCardNormal(self, cards, suspicionResult); //Liefert null, wenn nichts gez. werden kann
                //Die Methode merkt sich bereits in den eigenen Notizen die gezeigte Karte
                break;
            case 1:
                //Es wurde nur eine Karte dem Spieler am häufigsten gezeigt
                cardToShow = mostShownCards.iterator().next();
                //In den eigenen Notizen merken, dass diese nun häufiger gezeigt wurde
                GameLogic.debugln("Als schlaue KI { " + self.getCharacter().getName() + " } notiere ich mir, dass ich { " + cardToShow + " } einmal häufiger gezeigt habe");
                takeNoteCardShown(cards, cardToShow, self.getNoteSelf());
                break;
            default: //Die Es wurden 2 oder mehr(3) der mögl. Karten dem Anklagenden Spieler gleich häufig gezeigt
                //Es wird die erste Karte laut Notiz genommen
                cardToShow = getFirstCardByNoteOrder(cards, mostShownCards);
                //In den eigenen Notizen merken, dass diese nun häufiger gezeigt wurde
                GameLogic.debugln("Als schlaue KI { " + self.getCharacter().getName() + " } notiere ich mir, dass ich { " + cardToShow + " } einmal häufiger gezeigt habe");
                takeNoteCardShown(cards, cardToShow, self.getNoteSelf());

        }
        //Interne Datenstruktur beschreiben WEM sie bereits wie HÄUFIG eine KARTE gezeigt hat
        this.rememberShownCard(players, cards, currentPlayer, self, cardToShow);
        return cardToShow;
    }

    @Override
    public void getCardsShown(Player[] players, Card[] cards, Player currentPlayer, CardTriple suspicion, Card[] shownCards) {
        takeNotesAboutShownCards(players, cards, currentPlayer, suspicion, shownCards);
        analyzeHistory(players, cards, currentPlayer);
    }

    /**
     * Liefert den Index des übergebenen Spielers ausgehend von der Reihenfolge in dem Array,
     * in dem alle Spieler vorhanden sind. (Wie Notizen)
     *
     * @param players alle Spieler
     * @param target  der gesuchte Spieler.
     * @return der Index des gesuchten Spielers.
     */
    private int getIndexOfPlayer(Player[] players, Player target) {
        for (int i = 0; i < players.length; i++) {
            if (players[i].equals(target)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Hilfsmethode, um einen String mit padding zu versehen.
     * (Für Debugausgaben)
     *
     * @param s der String welche gepadded werden soll.
     * @param n die anzahl an Padding.
     * @return der gepaddete String.
     */
    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    /**
     * Schreibt die Notizen über andere Spieler auf die Konsole.
     * (Debugausgaben)
     *
     * @param players    alle Spieler.
     * @param cards      alle Karten.
     * @param self       der eigene KI-Spieler.
     * @param noteOthers die Notizen über andere des eigenen KI-Spielers.
     */
    private void printNoteOthers(Player[] players, Card[] cards, Player self, NoteOthers[][] noteOthers) {
        GameLogic.debugln("Ich Als Schlaue KI {" + self.getCharacter().getName() + "} habe in meinen NoteOthers : ");
        GameLogic.debug(padRight(" ", 20));
        for (Player player : players) {
            if (!player.equals(self)) {
                GameLogic.debug(padRight(player.getCharacter().getName(), 20));
            }
        }
        GameLogic.debugln();
        for (Card card : cards) {
            GameLogic.debug(padRight(card.getName() + ": ", 20));
            int cardIndex = getCardIndexInNotes(cards, card);
            for (Player player : players) {
                if (!player.equals(self)) {
                    int playerIndexInNoteOthers = getCharacterIndexInNotes(players, self, player.getCharacter());
                    GameLogic.debug(padRight(noteOthers[playerIndexInNoteOthers][cardIndex].toString(), 20));
                }
            }
            GameLogic.debugln();
        }
    }

    /**
     * Schreibt die temporären Notizen auf denen ausschlüsse von Karten getätigt werden
     * auf die Konsole.
     * (Debugausgaben)
     *
     * @param players   alle Spieler.
     * @param cards     alle Karten.
     * @param self      der eigene KI-Spieler.
     * @param tempNotes die temporären Notizen des eigenen KI-Spielers.
     */
    private void printTempNotes(Player[] players, Card[] cards, Player self, TempNotes[][] tempNotes) {
        GameLogic.debugln("Ich Als Schlaue KI {" + self.getCharacter().getName() + "} habe in meinen TempNotes : ");
        GameLogic.debug(padRight(" ", 20));
        for (Player player : players) {
            GameLogic.debug(padRight(player.getCharacter().getName(), 20));
        }
        GameLogic.debugln();
        for (Card card : cards) {
            GameLogic.debug(padRight(card.getName() + ": ", 20));
            int cardIndex = getCardIndexInNotes(cards, card);
            for (Player player : players) {
                int playerIndexInNoteOthers = getIndexOfPlayer(players, player);
                GameLogic.debug(padRight(tempNotes[playerIndexInNoteOthers][cardIndex].toString(), 20));

            }
            GameLogic.debugln();
        }
    }


    /**
     * Wertet die Spielhistorie aus um neue Erkenntnisse zu gewinnen, welcher Spieler, welche Karte
     * auf der Hand haben könnte.
     * Dafür wird eine temporäre Notiz erstellt, welche die eigenen, die bereits gesehenen
     * sowie die ausgeschlossenen Karten enthält.
     * Diese wird nun Zeilenweise abgelaufen und bei gesehenen Karten ausgeschlossen, dass diese jemand
     * anders haben könnte.
     * Falls ein Spieler einem anderen Spieler nun eine Karte gezeigt hat und zwei von den im Verdacht
     * vorkommenden Karten bei dem zeigenden Spieler auf jeden Fall vorhanden sind, so wird daraus geschlossen,
     * dass es sich bei der gezeigten Karte um die verbleibende Karte handelt.
     * Dies wird jedes mal für alle in der Historie enthaltenden Spielzüge gemacht, wenn die Notizen über andere Spieler
     * beschieben werden, da daraus eventuell neue Erkenntnisse gewonnen werden könnten.
     *
     * @param players alle Spieler im Spiel.
     * @param cards   alle Karten im Spiel.
     * @param self    der Spieler der schauen KI
     */
    private void analyzeHistory(Player[] players, Card[] cards, Player self) {

        //Zwischendatenstruktur aufbauen und mit Fragezeichen füllen
        TempNotes[][] tempNotes = new TempNotes[players.length][cards.length];
        //Mit Fragezeichen befüllen
        for (TempNotes[] tempNote : tempNotes) {
            Arrays.fill(tempNote, TempNotes.UNKNOWN);
        }

        //Eigene Spalte (Index für den Spaltenzugriff ausrechnen)
        int selfIndex = getIndexOfPlayer(players, self);

        // mit hat oder hat nicht befüllen ausgehend von der Hand
        List<Card> cardsOnHand = self.getCards();
        for (int cardInTempNotesIndex = 0; cardInTempNotesIndex < cards.length; cardInTempNotesIndex++) {
            if (cardsOnHand.contains(cards[cardInTempNotesIndex])) {
                tempNotes[selfIndex][cardInTempNotesIndex] = TempNotes.HAS;
            } else {
                tempNotes[selfIndex][cardInTempNotesIndex] = TempNotes.HAS_NOT;
            }
        }


        //Schreibe aus den Notizen Informationen über andere Spieler in die Zwischendatenstruktur ob diese Karten haben oder nicht
        NoteOthers[][] noteOthers = self.getNoteOthers();

        printNoteOthers(players, cards, self, noteOthers);

        int tempNotesPlayerIndex = 0;
        for (Player player : players) {
            //getCharacterIndexInNotes gibt bei dem eigenen Character -1 zurück
            int playerInNoteOthersIndex = getCharacterIndexInNotes(players, self, player.getCharacter());
            if (playerInNoteOthersIndex != -1) {
                for (Card card : cards) {
                    int currentCardIndex = getCardIndexInNotes(cards, card);
                    if (noteOthers[playerInNoteOthersIndex][currentCardIndex] == NoteOthers.HAS_NOT) {
                        tempNotes[tempNotesPlayerIndex][currentCardIndex] = TempNotes.HAS_NOT;
                    } else if (noteOthers[playerInNoteOthersIndex][currentCardIndex] == NoteOthers.SEEN) {
                        tempNotes[tempNotesPlayerIndex][currentCardIndex] = TempNotes.HAS;
                    }
                }
            }
            tempNotesPlayerIndex++;
        }

        //Zeilenweise ablaufen und versuchen dinge auszuschließen
        analizeRowInTempNotes(tempNotes);

        //Historie Eintrag für Eintrag ablaufen
        for (Turn turn : gameHistory) {
            Character currentSuspector = turn.getSuspector();
            CardTriple currentSuspicion = turn.getSuspicion();
            Card[] currentShownCards = turn.getCardsShown();
            //Über alle Spieler die auf eine Verdächtigung reagiert haben laufen (außer einem selber)
            int shownCardsIndex = 0;
            for (Player player : players) {
                if (!player.getCharacter().equals(currentSuspector)) {
                    if (currentShownCards[shownCardsIndex] != null) { //Es wurde etwas gezeigt
                        if (!player.equals(self)) { //Wenn man selber etwas gezeigt hat,dann kann man daraus keine Rückschlüsse ziehen
                            int playerIndexInTempNotes = getIndexOfPlayer(players, player);
                            //In die Spalte des Spielers schauen und prüfen ob bereits 2 der 3 Karten der Verdächtigung bei Ihm ausgeschlossen werden konnten
                            int indexOfRemainingCardToExclude = getIndexOfCardToExclude(cards, tempNotes[playerIndexInTempNotes], currentSuspicion);
                            if (indexOfRemainingCardToExclude != -1) { //Es wurde tatsächlich solch eine Karte gefunden
                                //Durch den Ausschluss kann die Karte nun als gesehen betrachtet werden
                                tempNotes[playerIndexInTempNotes][indexOfRemainingCardToExclude] = TempNotes.HAS;
                                GameLogic.debugln("ES WURDE EIN AUSSCHLUSS GETÄTIGT");
                                //Es könnten eventuell nun weitere Karten ausgeschlossen werden
                                analizeRowInTempNotes(tempNotes);
                            }
                        }
                    }
                    shownCardsIndex++;
                }
            }
        }

        //Daten aus der Zwischendatenstruktur wieder in die Notizen über die anderen Spieler überführen
        for (int tempNoteCharacterIndex = 0; tempNoteCharacterIndex < tempNotes.length; tempNoteCharacterIndex++) {
            Player player = players[tempNoteCharacterIndex];
            if (!player.equals(self)) {
                //Der Index des Spielers in den Notizen über andere
                int playerInNotesIndex = getCharacterIndexInNotes(players, self, player.getCharacter());
                for (Card card : cards) { //Die Spalte ablaufen und neue Erkenntnisse in die Notizen über andere Spieler eintragen
                    int currentCardIndex = getCardIndexInNotes(cards, card);
                    if (tempNotes[tempNoteCharacterIndex][currentCardIndex] == TempNotes.HAS) {
                        noteOthers[playerInNotesIndex][currentCardIndex] = NoteOthers.SEEN;
                    } else if (tempNotes[tempNoteCharacterIndex][currentCardIndex] == TempNotes.HAS_NOT) {
                        noteOthers[playerInNotesIndex][currentCardIndex] = NoteOthers.HAS_NOT;
                    }
                }
            }
        }
        printNoteOthers(players, cards, self, noteOthers);
        //Nun sind die Notizen über andere Spieler mit neuen Infos angereichert
    }

    /**
     * Liefert den Index der Karte in den temporären Notizen, welche aufgrund eines Auschlusses,
     * nun als gesehen angenommen werden kann.
     *
     * @param cards            alle Karten im Spiel.
     * @param tempNote         die temporären Notizen auf denen die Ausschlüsse getätigt werden.
     * @param currentSuspicion die Verdächtigung in dem aktuell betrachtetem Fall.
     * @return der Index der Karte die als gesehen angenommen werden kann.
     * Falls nicht mögl., dann -1.
     */
    private int getIndexOfCardToExclude(Card[] cards, TempNotes[] tempNote, CardTriple currentSuspicion) {
        List<Card> cardsThePlayerDontHave = new ArrayList<>();
        Set<Card> suspicion = currentSuspicion.getCards();
        for (int cardInTempNotesIndex = 0; cardInTempNotesIndex < cards.length; cardInTempNotesIndex++) {
            if (suspicion.contains(cards[cardInTempNotesIndex])) {
                if (tempNote[cardInTempNotesIndex] == TempNotes.HAS_NOT) {
                    cardsThePlayerDontHave.add(cards[cardInTempNotesIndex]);
                }
            }
        }
        if (cardsThePlayerDontHave.size() == 2) { //Wenn schon 2 Karten der damaligen Verdächtigung ausgeschlossen werden konnten
            //Dieser Spieler aber eine Karte gezeigt hat, dann muss der Spieler die verbleibende Karte definitiv auf der Hand haben
            //Aus der damaligen Verdächtigung die Karten die der Spieler definitiv nicht hat abziehen
            suspicion.removeAll(cardsThePlayerDontHave);
            Card playersCard = suspicion.iterator().next();
            return getCardIndexInNotes(cards, playersCard);
        }
        return -1;
    }


    /**
     * Prüft, ob eine der Karten bereits gesehen wurde
     * Wenn der Fall, dann in gesamter Zeile "hat nicht" eintragen außer bei der gesehenen Karte.
     *
     * @param tempNotes die temporären Notizen.
     */
    private void analizeRowInTempNotes(TempNotes[][] tempNotes) {
        //Zeilenweise durch das Array laufen
        for (int noteIndex = 0; noteIndex < tempNotes[0].length; noteIndex++) { //Alle Zeilen sind gleich lang
            int hasIndex = -1;
            for (int characterIndex = 0; characterIndex < tempNotes.length; characterIndex++) {
                if (tempNotes[characterIndex][noteIndex] == TempNotes.HAS) {
                    hasIndex = characterIndex;
                }
            }
            if (hasIndex != -1) { //Es wurde ein HAS Eintrag gefunden
                //Nochmal durch die Zeile und alles außer dem hasIndex auf Has_Not setzen
                for (int characterIndex = 0; characterIndex < tempNotes.length; characterIndex++) {
                    if (characterIndex != hasIndex) {
                        tempNotes[characterIndex][noteIndex] = TempNotes.HAS_NOT;
                    }
                }
            }
        }


    }

    /**
     * Prüft ob zwei genau Karten aus dem Verdacht in dem übergebenen Set vorhanden sind.
     *
     * @param ownCards  ein Set mit den eigenen Karten.
     * @param suspicion die Verdächtigung aus der zwei Elemente im Set vorhanden sein sollen.
     * @return ob genau zwei Elemente vorhanden sind oder nicht.
     */
    private boolean containsTwo(Set<Card> ownCards, CardTriple suspicion) {
        return (ownCards.contains(suspicion.getRoom()) && ownCards.contains(suspicion.getCharacter()))
                || (ownCards.contains(suspicion.getRoom()) && ownCards.contains(suspicion.getWeapon()))
                || (ownCards.contains(suspicion.getCharacter()) && ownCards.contains(suspicion.getWeapon()));
    }

    @Override
    public void watchCardsGetShown(Player[] allPlayers, Card[] allCards, Player myself, Player
            currentPlayer, CardTriple suspicion, Card[] shownCards) {
        gameHistory.add(new Turn(currentPlayer.getCharacter(), suspicion, shownCards));

        //Bei denjenigen die nichts gezeigt haben notieren, dass diese definitiv keine dieser 3 Karten besitzen
        int shownCardsIndexDEBUG = 0;
        GameLogic.debugln("Ich Als Schlaue KI {" + myself.getCharacter().getName() + "} bekomme mit dass: ");
        for (Player player : allPlayers) {
            if (!player.equals(currentPlayer)) {
                String print;
                if (shownCards[shownCardsIndexDEBUG] == null) {
                    print = "nichts";
                    //Notiz machen, dass dieser Spieler definitiv keine der 3 Karten hat
                    if (!player.equals(myself)) { //Keine Notizen in Note Others über meine Eigene Karten machen
                        takeNote(allPlayers, allCards, myself, player.getCharacter(), suspicion.getCharacter(), NoteOthers.HAS_NOT);
                        takeNote(allPlayers, allCards, myself, player.getCharacter(), suspicion.getWeapon(), NoteOthers.HAS_NOT);
                        takeNote(allPlayers, allCards, myself, player.getCharacter(), suspicion.getRoom(), NoteOthers.HAS_NOT);
                        GameLogic.debugln("Ich Als Schlaue KI { " + myself.getCharacter().getName() + " } notiere mir dass, { " + player.getCharacter().getName() + " } definitiv keine { " + suspicion.getCharacter() + " } hat");
                        GameLogic.debugln("Ich Als Schlaue KI { " + myself.getCharacter().getName() + " } notiere mir dass, { " + player.getCharacter().getName() + " } definitiv keine { " + suspicion.getWeapon() + " } hat");
                        GameLogic.debugln("Ich Als Schlaue KI { " + myself.getCharacter().getName() + " } notiere mir dass, { " + player.getCharacter().getName() + " } definitiv keine { " + suspicion.getRoom() + " } hat");
                    }
                } else {
                    print = "etwas";
                }
                GameLogic.debugln("{" + player.getCharacter().getName() + "} { " + print + " } gezeigt hat");
                shownCardsIndexDEBUG++;
            }
        }

        //Über die gesamte Historie laufen und schauen ob man etwas herleiten kann und falls ja dies in die Notizen eintragen

        //Falls Die KI 2 Karten des Verdachtes auf der Hand hat und jemand eine Karte zeigt, dann hat derjenige diese Karte
        Set<Card> ownCards = new HashSet<>(myself.getCards());
        if (containsTwo(ownCards, suspicion)) {
            concludeTwoCardsOfSuspicionOnHand(allPlayers, allCards, myself, currentPlayer, suspicion, shownCards, ownCards);
        }

        analyzeHistory(allPlayers, allCards, myself);
    }


    /**
     * Schließt auf die Karte eines anderen Spielers, falls zwei der im Verdacht genannten
     * Karten der KI-Spieler selber auf der Hand hat und er mitbekommt, dass jemand bei einer Verdächtigung
     * eine Karte gezeigt hat.
     *
     * @param allPlayers    alle Spieler im Spiel.
     * @param allCards      alle Karten im Spiel.
     * @param myself        der Spieler der KI.
     * @param currentPlayer der Spieler der aktuell eine Verdächtigung geäußert hat.
     * @param suspicion     die Verdächtigung die der aktuelle Spieler geäußert hat.
     * @param shownCards    die von den Spielern gezeigten Karten.(prüfung nur == null)
     * @param ownCards      die Karten auf der Hand des KI-Spielers.
     */
    private void concludeTwoCardsOfSuspicionOnHand(Player[] allPlayers, Card[] allCards, Player
            myself, Player currentPlayer, CardTriple suspicion, Card[] shownCards, Set<Card> ownCards) {
        int shownCardsIndex = 0;
        for (Player player : allPlayers) {
            if (!player.equals(currentPlayer)) { //Der aktuelle Spieler ist nicht in den gezeigten Karten enthalten
                //Wenn eine Karte gezeigt wurde und es jemand anderes als die KI war, dann
                //kann ich draus schließen, dass die Karte welche ich nicht habe die Karte ist die der Andere spieler gezeigt hat
                if (shownCards[shownCardsIndex] != null && !player.equals(myself)) {
                    //Ich könnte die Karte die
                    Set<Card> shownCardSet = suspicion.getCards();
                    shownCardSet.removeAll(ownCards);
                    assert shownCardSet.size() == 1;
                    Card shownCard = shownCardSet.iterator().next();
                    assert shownCard.equals(shownCards[shownCardsIndex]);
                    takeNote(allPlayers, allCards, myself, player.getCharacter(), shownCard, NoteOthers.SEEN);
                    GameLogic.debugln("Ich Als Schlaue KI { " + myself.getCharacter().getName() + " } notiere mir dass, { " + player.getCharacter().getName() + " } definitiv ein { " + shownCard + " } hat da ich 2 Karten des Verdachtes auf der Hand habe");
                }
                shownCardsIndex++;
            }

        }
    }

    /**
     * Merkt sich, dass die übergebene Karte dem übergebenen Spieler nun einmal häufiger gezeigt wurde.
     *
     * @param allPlayers    alle Spieler im Spiel.
     * @param allCards      alle Karten im Spiel.
     * @param currentPlayer der Spieler dem die Karte gezeigt wird.
     * @param self          der eigene Spieler der KI.
     * @param cardToShow    die Karte welche gezeigt werden soll.
     */
    private void rememberShownCard(Player[] allPlayers, Card[] allCards, Player
            currentPlayer, Player self, Card cardToShow) {
        if (cardToShow != null) {
            int characterShownToIndex = getCharacterIndexInNotes(allPlayers, self, currentPlayer.getCharacter());
            int cardToShowIndex = getCardIndexInNotes(allCards, cardToShow);
            //Intern merken, dass diese Karte diesem Spieler nun einmal mehr gezeigt wurde
            this.shownCardsCount[characterShownToIndex][cardToShowIndex]++;
            GameLogic.debugln("Als schlaue KI { " + self.getCharacter().getName() + " } merke ich mir, dass ich { " + currentPlayer.getCharacter().getName() + " } die Karte { " + cardToShow + " } nun { " + shownCardsCount[characterShownToIndex][cardToShowIndex] + " } mal gezeigt habe");
        }
    }
}
