package logic;

/**
 * Klasse die die aus einem Spielstand geladenen Daten zwischenhält, um diese danach in die
 * Spiellogik zu schreiben.
 *
 * @author Michael Smirnov
 */
public class LoadedGameLogic {
    //Die zuordnung der Waffen in die Räume
    private final Room[] weaponInRooms;
    //Die Spieler im Spiel
    private final Player[] players;
    //Die Anzahl der Spieler im Spiel.
    private final int playerCount;
    //Die Lösung des Spiels.
    private final CardTriple envelope;

    /**
     * Konstruktor mit allen nötigen Daten.
     *
     * @param weaponInRooms die zuordnung der Waffen in die Räume
     * @param players       die Spieler im Spiel
     * @param playerCount   die Anzahl der Spieler im Spiel.
     * @param envelope      die Lösung des Spiels.
     */
    public LoadedGameLogic(Room[] weaponInRooms, Player[] players, int playerCount, CardTriple envelope) {
        this.weaponInRooms = weaponInRooms;
        this.players = players;
        this.playerCount = playerCount;
        this.envelope = envelope;
    }

    /**
     * Liefert die Anzahl der Spieler im Spiel.
     *
     * @return die Anzahl der Spieler im Spiel.
     */
    public int getPlayerCount() {
        return playerCount;
    }

    /**
     * Schreibt die Daten aus dem Spielstand in die übergebene GameLogic und verändert diese somit.
     *
     * @param logic die zu verändernde Spiellogik.
     */
    public void commit(GameLogic logic) {
        logic.setPlayers(this.players);
        logic.setPlayerCount(this.playerCount);
        logic.setWeaponInRooms(this.weaponInRooms);
        logic.setEnvelope(this.envelope);
        logic.setCurrentPlayerIndex(0); //Es ist wieder der erste (menschl.) Spieler dran
    }
}
