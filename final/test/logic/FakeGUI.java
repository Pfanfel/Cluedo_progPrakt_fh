package logic;

import logic.exceptions.CluedoException;

import java.util.Set;

public class FakeGUI implements GUIConnector {
    @Override
    public void updateDice(int dice) {

    }

    @Override
    public void drawCharacterOnCorridor(Character character, int characterIndex) {

    }

    @Override
    public void drawCharacterInRoom(Character character, int characterIndex) {

    }

    @Override
    public void setWeapon(Room room, Weapon weapon) {

    }

    @Override
    public void handleException(CluedoException e) {

    }

    @Override
    public void redrawGUI() {

    }


    @Override
    public void showIllegalStepMessage() {

    }

    @Override
    public void drawPossibleMoves(Set<Position> possibleSteps) {

    }

    @Override
    public void clearPossibleMoves() {

    }

    @Override
    public CardTriple handleExpressSuspicion(Card enteredRoom) {
        return null;
    }

    @Override
    public void handleAISuspicion(String playerName, CardTriple suspicion) {

    }

    @Override
    public void handleOwnSuspicionResult(Player[] allPlayers, Card[] shownByAI, CardTriple suspicion) {

    }

    @Override
    public void handleOthersSuspicionResult(Player[] allPlayers, Player currentPlayer, Card[] shownCards, CardTriple suspicion) {

    }

    @Override
    public Card handleShowCard(CardTriple suspicion, CardTriple possibleCardsToShow) {
        return null;
    }

    @Override
    public void handleGameWon(CardTriple solution, Player winner) {

    }

    @Override
    public void handleGameLost(CardTriple wrongSolution, CardTriple solution, Player loser) {

    }
}
