package logic.json;

/**
 * Repräsenitert das Spielfeld auf dem gespielt wird in der initialen Konfigurationsdatei des Spiels. (JSON-Format)
 * Lässt sich theoretisch anpassen um andere Spielfelder im Spiel zu ermöglichen.
 *
 * @author Michael Smirnov
 */
public class InitialGameFieldJSON {
    //Die Zellenbreite des Spielfeldes
    private final int gameFieldWidth;
    //Die Zellenhöhe des Spielfeldes
    private final int gameFieldHeight;
    //Das Spielfeld, wobei die Spielfeldzellen als String repräsentiert sind.
    //# sind Wände, " " sind Flurstellen und Ziffern sind die indices der Räume in der Konfig. Datei
    private final String[] gameField;

    /**
     * Konstruktor.
     *
     * @param gameFieldWidth  die Zellenbreite des Spielfeldes.
     * @param gameFieldHeight die Zellenhöhe des Spielfeldes.
     * @param gameField       das Spielfeld, mit Spielfeldzellen.
     */
    public InitialGameFieldJSON(int gameFieldWidth, int gameFieldHeight, String[] gameField) {
        this.gameFieldWidth = gameFieldWidth;
        this.gameFieldHeight = gameFieldHeight;
        this.gameField = gameField;
    }

    /**
     * Liefert die Zellenbreite des Spielfeldes.
     *
     * @return die Zellenbreite des Spielfeldes.
     */
    public int getGameFieldWidth() {
        return gameFieldWidth;
    }

    /**
     * Liefert die Zellenhöhe des Spielfeldes.
     *
     * @return die Zellenhöhe des Spielfeldes.
     */
    public int getGameFieldHeight() {
        return gameFieldHeight;
    }

    /**
     * Liefert das Spielfeld, mit Spielfeldzellen.
     *
     * @return das Spielfeld, mit Spielfeldzellen.
     */
    public String[] getGameField() {
        return gameField;
    }
}
