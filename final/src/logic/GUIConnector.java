package logic;

import logic.exceptions.CluedoException;

import java.util.Set;

/**
 * Enthält die Methoden welche für die Logik sichtbar sein sollen.
 *
 * @author Michael Smirnov
 */

public interface GUIConnector {

    /**
     * Aktualisiert die Würfelaugen auf die übergebene Zahl.
     *
     * @param dice die Augentahl des Würfels.
     */
    void updateDice(int dice);

    /**
     * Zeichnet die Spielfigur an die Position im Flur, welche in ihr enthalten ist.
     *
     * @param character      die Spielfigur welche gesetzt werden soll.
     * @param characterIndex der Index der zu zeichnenden Figur im Spielfigurenarray.
     */
    void drawCharacterOnCorridor(Character character, int characterIndex);

    /**
     * Zeichnet die Spielfigur an die Position im Raum, welche in ihr enthalten ist.
     * (Muss ein Raummittelpunkt sein)
     *
     * @param character      die Spielfigur welche gesetzt werden soll.
     * @param characterIndex der Index der zu zeichnenden Figur im Spielfigurenarray.
     */
    void drawCharacterInRoom(Character character, int characterIndex);

    /**
     * Setzt die Waffe in den Raum.
     *
     * @param room   der Raum in den die Waffe zu setzen ist.
     * @param weapon die zu setzende Waffe.
     * @throws CluedoException falls die Waffe nicht gesetzt werden kann
     */
    void setWeapon(Room room, Weapon weapon) throws CluedoException;

    /**
     * Kümmert sich um das behandeln des Fehlers für den User.
     *
     * @param e die Aufgetretene Fehlermeldung.
     */
    void handleException(CluedoException e);

    /**
     * Setzt die GUI auf den Ausgangszustand zurück.
     */
    void redrawGUI();

    /**
     * Zeigt dem User eine Nachricht, dass er einen ungültigen Schritt machen wollte.
     */
    void showIllegalStepMessage();

    /**
     * Zeichnet die für den Spieler möglichen Schritte auf dem Spielfeld.
     *
     * @param possibleMoves die Menge an für den Spieler möglichen Schritte.
     */
    void drawPossibleMoves(Set<Position> possibleMoves);

    /**
     * Löscht die für den Spieler möglichen Schritte auf dem Spielfeld.
     */
    void clearPossibleMoves();

    /**
     * Kümmert sich darum, dass der User in einem Dialog seine Verdächtigung aussprechen kann.
     *
     * @param enteredRoom der von dem User betretene Raum als Karte.
     * @return die vom User ausgesprochene Verdächtigung.
     */
    CardTriple handleExpressSuspicion(Card enteredRoom);

    /**
     * Kümmert sich um die Verdächtigung eines KI Spielers und zeigt diese an.
     *
     * @param playerName der Spieler der die Verdächtigung geäußert hat.
     * @param suspicion  die von dem Spieler geäußerte verdächtigung.
     */
    void handleAISuspicion(String playerName, CardTriple suspicion);

    /**
     * Kümmert sich um das anzeigen der Reaktion aller anderen Spieler auf die vom User
     * geäußerte Verdächtigung.
     *
     * @param allPlayers alle Spieler im Spiel.
     * @param shownByAI  die von den KIs gezeigten Karten.
     * @param suspicion  die von dem Spieler ausgesprochene Verdächtigung.
     * @throws CluedoException falls dabei ein Fehler auftritt.
     */
    void handleOwnSuspicionResult(Player[] allPlayers, Card[] shownByAI, CardTriple suspicion) throws CluedoException;

    /**
     * Kümmert sich um die Anzeige der Reaktionen der anderen Spieler, falls jemand anderes als der
     * User eine Verdächtigung geäußert hat.
     *
     * @param allPlayers    alle Spieler im Spiel.
     * @param currentPlayer der aktuelle Spieler, der die Verdächtigung geäußert hat.
     * @param shownCards    die von den Spielern gezeigten Karten.
     * @param suspicion     die vem dem aktuellen Spieler geäußerte Verdächtigung.
     * @throws CluedoException falls dabei ein Fehler auftritt.
     */
    void handleOthersSuspicionResult(Player[] allPlayers, Player currentPlayer, Card[] shownCards, CardTriple suspicion) throws CluedoException;

    /**
     * Kümmert sich um den Dialog, bei dem der User auf eine Verdächtigung hin eine Karte zeigen muss.
     *
     * @param suspicion           die Verdächtigung auf die der User reagieren muss.
     * @param possibleCardsToShow die Karten die der Spieler zeigen kann.
     * @return die von dem User gezeigte Karte.
     */
    Card handleShowCard(CardTriple suspicion, CardTriple possibleCardsToShow);

    /**
     * Kümmert sich um den Dialog bei einem gewonnenen Spiel.
     *
     * @param solution die Lösung des Spiels.
     * @param winner   der Sieger.
     */
    void handleGameWon(CardTriple solution, Player winner);

    /**
     * Kümmert sich um den Dialog bei einem verlorenem Spiel.
     *
     * @param wrongSolution die Falsche Anklage.
     * @param solution      die Lösung des Spiels.
     * @param loser         der Verlierer.
     */
    void handleGameLost(CardTriple wrongSolution, CardTriple solution, Player loser);
}
