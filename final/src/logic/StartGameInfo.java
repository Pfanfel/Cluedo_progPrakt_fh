package logic;

import java.util.Arrays;
import java.util.Objects;

/**
 * Klasse die, die Spielinformation, welche beim Start eines neues Spiels
 * abgefragt wird, repräsentiert.
 *
 * @author Michael Smirnov
 */
public class StartGameInfo {
    //Die Anzahl an Spielern im Spiel.
    private final int playerCount;
    //Die KI-Stärken der Mitspieler (die des menschl. Spielers == null)
    private final AIDifficulty[] playerDifficulties;

    /**
     * Konstruktor.
     *
     * @param playerCount        die Anzahl an Spielern im Spiel.
     * @param playerDifficulties Die KI-Stärken der Mitspieler (die des menschl. Spielers == null)
     */
    public StartGameInfo(int playerCount, AIDifficulty[] playerDifficulties) {
        this.playerCount = playerCount;
        this.playerDifficulties = playerDifficulties;
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
     * Liefert die KI-Stärken der Mitspieler.
     *
     * @return die KI-Stärken der Mitspieler.
     */
    public AIDifficulty[] getPlayerDifficulties() {
        return playerDifficulties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StartGameInfo)) return false;
        StartGameInfo that = (StartGameInfo) o;
        return playerCount == that.playerCount &&
                Arrays.equals(playerDifficulties, that.playerDifficulties);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(playerCount);
        result = 31 * result + Arrays.hashCode(playerDifficulties);
        return result;
    }

    @Override
    public String toString() {
        return "StartGameInfo{" +
                " playerCount=" + playerCount +
                ", playerDifficulties=" + Arrays.toString(playerDifficulties) +
                '}';
    }
}
