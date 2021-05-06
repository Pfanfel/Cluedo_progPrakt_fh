package logic.json;

/**
 * Repräsentiert die Konfigurationsdatei des Spiels. (JSON-Format)
 * Diese legt alle Spielfiguren, Waffen, Räume (deren Türen, Mittelpunkte und event. Geheimtüren), sowie
 * das aktuelle Spielfeld fest.
 * Diese könnte theoretisch angepasst werden und das Spiel ließe sich erweitern, da keine
 * Enums für die Spielelemente benutzt wurden, sondern Klassen.
 * Diese lassen sich über deren Namen identifizieren.
 * Der Name wird aus eben dieser Initialdatei geladen und als Referenz genutzt.
 *
 * Anmerkung: Die Initialisierungsdatei, welche uns auf der Aufgabenstellungsseite
 * zur Verfügung gestellt, wurde stimmt NICHT mit den Daten der Mittelpunkte der Spieler
 * in der Beispiel-Spielstandsdatei überein!
 * Da die Spielstandsdatei wahrscheinlich Basis für weitere Tests sein wird, wurde die Initialdatei
 * so verändert, dass der Spielstand ohne Fehler gelesen werden kann.
 * Der Unterschied liegt bei dem Mittelpunkt der Küche.
 * Dieser ist in der Beispiel-Initialisierungsdatei bei 3,3 und in der Beispiel-Spielstandsdatei
 * bei 3,4.
 * Da Spieler, die sich in einem Raum befinden, zwingend auf dem Mittelpunkt stehen müssen,
 * könnte der Unterschied zu fälschlich fehlerhaften Tests führen.
 *
 * @author Michael Smirnov
 */
public class InitialGameDataJSON {
    //Die im Spiel befindlichen Spielfiguren
    private final InitialCharacterJSON[] players;
    //Die im Spiel befindlichen Räume
    private final InitialRoomJSON[] rooms;
    //Die im Spiel befindlichen Waffen
    private final String[] weapons;
    //Das Spielfeld auf dem gespielt werden soll
    private final InitialGameFieldJSON gameField;


    /**
     * Konstruktor.
     *
     * @param players   die im Spiel befindlichen Spielfiguren.
     * @param rooms     die im Spiel befindlichen Räume.
     * @param weapons   die im Spiel befindlichen Waffen.
     * @param gameField das Spielfeld auf dem gespielt werden soll.
     */
    public InitialGameDataJSON(InitialCharacterJSON[] players, InitialRoomJSON[] rooms, String[] weapons, InitialGameFieldJSON gameField) {
        this.players = players;
        this.rooms = rooms;
        this.weapons = weapons;
        this.gameField = gameField;
    }

    /**
     * Liefert die im Spiel befindlichen Spielfiguren.
     *
     * @return die im Spiel befindlichen Spielfiguren.
     */
    public InitialCharacterJSON[] getPlayers() {
        return players;
    }

    /**
     * Liefert die im Spiel befindlichen Räume.
     *
     * @return die im Spiel befindlichen Räume.
     */
    public InitialRoomJSON[] getRooms() {
        return rooms;
    }

    /**
     * Liefert die im Spiel befindlichen Waffen.
     *
     * @return die im Spiel befindlichen Waffen.
     */
    public String[] getWeapons() {
        return weapons;
    }

    /**
     * Liefert das Spielfeld auf dem gespielt wird.
     *
     * @return das Spielfeld auf dem gespielt wird.
     */
    public InitialGameFieldJSON getGameField() {
        return gameField;
    }
}
