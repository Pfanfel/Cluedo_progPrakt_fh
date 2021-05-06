package logic.json;

import logic.Position;

/**
 * Repräsenitert eine Spielfigur in der initialen Konfigurationsdatei des Spiels. (JSON-Format)
 * Lässt sich theoretisch anpassen um andere Spielfiguren im Spiel zu ermöglichen.
 *
 * @author Michael Smirnov
 */

public class InitialCharacterJSON {
    //Der Name der Spielfigur
    private final String name;
    //Die Startposition der Spielfigur
    private final Position position;

    /**
     * Konstruktor.
     *
     * @param name     der Name der Spielfigur.
     * @param position die Startposition der Spielfigur.
     */
    public InitialCharacterJSON(String name, Position position) {
        this.name = name;
        this.position = position;
    }

    /**
     * Liefert den Namen der Spielfigur.
     *
     * @return der Name der Spielfigur.
     */
    public String getName() {
        return name;
    }

    /**
     * Liefert die Startposition der Spielfigur.
     *
     * @return die Startposition der Spielfigur.
     */
    public Position getPosition() {
        return position;
    }
}
