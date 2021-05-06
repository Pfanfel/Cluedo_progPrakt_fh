package logic.json;

/**
 * Repräsenitert die Spielstandsdaten in einer Spielstandsdatei. (JSON-Format)
 *
 * @author Michael Smirnov
 */
public class GameDataJSON {
    //Die teilnehmenden Spieler mit Spielername,KI,Aufenthaltsort,...
    private final PlayerJSON[] players;
    //Die Waffen im Spiel und deren Aufenthaltsort.
    private final WeaponJSON[] weapons;
    //Die Notizen jedes Spielers
    private final NoteJSON[][] notes;

    /**
     * Konstruktor.
     *
     * @param players Die teilnehmenden Spieler
     * @param weapons Die Waffen im Spiel
     * @param notes   Die Notizen jedes Spielers
     */
    public GameDataJSON(PlayerJSON[] players, WeaponJSON[] weapons, NoteJSON[][] notes) {
        this.players = players;
        this.weapons = weapons;
        this.notes = notes;
    }

    /**
     * Liefert die teilnehmenden Spieler.
     *
     * @return die teilnehmenden Spieler.
     */
    public PlayerJSON[] getPlayers() {
        return players;
    }

    /**
     * Liefert die Waffen im Spiel.
     *
     * @return die Waffen im Spiel.
     */
    public WeaponJSON[] getWeapons() {
        return weapons;
    }

    /**
     * Liefert die Notizen jedes Spielers.
     *
     * @return die Notizen jedes Spielers.
     */
    public NoteJSON[][] getNotes() {
        return notes;
    }

}
