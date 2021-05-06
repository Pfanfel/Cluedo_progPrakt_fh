package logic;

/**
 * Diese Klasse stellt die Implementierung der dummen KI-Stärke dar.
 * <p>
 * Zitat aus der Aufgabenstellung:
 * <p>
 * "Die "dumme" KI geht von der aktuellen Position aus immer zum nähesten Raum, der noch offen ist
 * (also bisher nicht als Tatraum ausgeschlossen werden konnte).
 * Falls der KI-Spieler sich aktuell in einem Raum befindet, wird dieser nicht mit in die Betrachtung einbezogen,
 * welcher Raum der "näheste" ist. Ansonsten ist hier ein Wegfindealgorithmus umzusetzen,
 * der über den Flur (aber ohne Geheimgänge) die Entfernungen zu anderen Räumen berechnet.
 * Andere Spieler, die auf dem Flur stehen, müssen dabei berücksichtigt werden.
 * Sie verlängern also ggf. den Weg bzw. machen ihn sogar unmöglich, wenn sie direkt vor einer Tür stehen.
 * Sollte nur noch ein einziger Raum "offen" sein, der Spieler befindet sich darin und muß ihn verlassen,
 * dann bewegt er sich zu einem zufälligen anderen Raum.
 * Wenn die "dumme" KI einen Verdacht äußert, werden die Personen dabei von unten nach oben
 * (angefangen mit Prof. Bloom) und die Waffen von oben nach unten durchgegangen (angefangen mit der Pistole).
 * Es werden allerdings nur "offene" Karten verdächtigt, also solche, die die KI weder selbst besitzt, noch bisher schon gesehen hat.
 * Gleiches gilt, wenn die KI selbst Karten zeigen muß:
 * Falls mehrere möglich sind, wird auch hier von oben nach unten die erste genommen
 * (also eher Personen als Waffen und eher Waffen als Räume)."
 */
public class AIStupid extends AI {

    @Override
    public AIDifficulty getDifficulty() {
        return AIDifficulty.STUPID;
    }

    @Override
    public Position computeNextMove(GameLogic logic, Player player, int steps) {
        return computeNextMoveStupid(logic, player, steps);
    }

    @Override
    public CardTriple expressSuspicion(GameLogic logic, Player currentPlayer, Card enteredRoomCard) {
        CardTriple suspicion = new CardTriple();

        suspicion.setRoom(enteredRoomCard);
        suspicion.setWeapon(suspectFirstWeapon(logic, currentPlayer));
        suspicion.setCharacter(suspectLastCharacter(logic, currentPlayer));

        return suspicion;
    }

    @Override
    public Card showCard(Player[] players, Card[] cards, Player currentPlayer, Player self, CardTriple suspicionResult) {
        //Die mögl. finden die zeigbar sind
        CardTriple possibleCardsToShow = self.possibleCardsToShow(suspicionResult);
        if (!possibleCardsToShow.isEmpty()) {
            //Falls mehrere möglich sind, wird auch hier von oben nach unten die erste genommen
            //(also eher Personen als Waffen und eher Waffen als Räume).
            for (Card card : cards) {
                if (possibleCardsToShow.contains(card)) {
                    return card;
                }
            }
        }
        return null;
    }


    @Override
    public void getCardsShown(Player[] players, Card[] cards, Player currentPlayer, CardTriple suspicion, Card[] shownCards) {
        takeNotesAboutShownCards(players, cards, currentPlayer, suspicion, shownCards);
    }

    @Override
    public void watchCardsGetShown(Player[] allPlayers, Card[] allCards, Player myself, Player currentPlayer, CardTriple suspicion, Card[] shownCards) {
        //Macht sich keine Notizen, wenn andere Karten zeigen
    }
}
