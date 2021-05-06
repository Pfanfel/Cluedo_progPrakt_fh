package logic;

/**
 * Diese Klasse stellt die Implementierung der normalen KI-Stärke dar.
 * <p>
 * Zitat aus der Aufgabenstellung:
 * <p>
 * "Die "normale" KI bewegt sich wie die "dumme" KI, berücksichtigt dabei allerdings auch Geheimgänge.
 * Bei einer Verdächtigung werden die Personen diesmal von oben nach unten verdächtigt,
 * um somit möglichst andere Spieler von ihrer aktuellen Position "wegzuwünschen".
 * Die Waffen werden weiterhin ebenfalls von oben nach unten verdächtigt.
 * Auch hier soll wieder nur nach noch "offenen" Karten gefragt werden.
 * Beim Zeigen von Karten sollen solche Karten bevorzugt gezeigt werden,
 * die man vorher bereits gezeigt hat - je häufiger, desto besser.
 * Es muß sich hier also pro Karte gemerkt werden,
 * wie oft diese schon gezeigt wurde. Stehen dann mehrere Karten zur Auswahl,
 * nimmt man die bisher schon am häufigsten gezeigte.
 * Bei mehreren gleich häufigen gilt wieder die Reihenfolge von oben nach unten."
 *
 * @author Michael Smirnov
 */
public class AINormal extends AI {

    @Override
    public AIDifficulty getDifficulty() {
        return AIDifficulty.NORMAL;
    }

    @Override
    public Position computeNextMove(GameLogic logic, Player player, int steps) {
        return computeNextMoveNormalAndSmart(logic, player, steps);
    }

    @Override
    public CardTriple expressSuspicion(GameLogic logic, Player currentPlayer, Card enteredRoomCard) {
        return expressSuspicionNormal(logic, currentPlayer, enteredRoomCard);
    }

    @Override
    public Card showCard(Player[] players, Card[] cards, Player currentPlayer, Player self, CardTriple suspicionResult) {
        return showCardNormal(self, cards, suspicionResult);
    }

    @Override
    public void getCardsShown(Player[] players, Card[] cards, Player currentPlayer, CardTriple suspicion, Card[] shownCards) {
        takeNotesAboutShownCards(players, cards, currentPlayer, suspicion, shownCards);
    }

    @Override
    public void watchCardsGetShown(Player[] allPlayers, Card[] allCards, Player myself, Player currentPlayer, CardTriple suspicion, Card[] shownCards) {
        //Macht sich keine Notizen, wenn andere Spieler Karten zeigen
    }

}
