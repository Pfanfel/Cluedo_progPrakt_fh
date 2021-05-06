package logic;

import logic.json.InitialCharacterJSON;

import java.util.Objects;

/**
 * Diese Klasse repräsentert eine Person/Spielfigur, welche im Spiel vorkommt.
 * Diese werden aus einer Initialdatei geladen.
 * Dies ermöglicht es theoretisch andere Personen/Spielfiguren im Spiel vorkommen zu lassen.
 */
public class Character {
    //Der Name der Person
    private final String name;
    //Die aktuelle Position der Spielfigur
    private Position position;

    /**
     * Konstruiert eine Spielfigur/Person.
     *
     * @param name     der Name der Person/Spielfigur.
     * @param position die initiale Position der Spielfigur auf dem Spielfeld.
     */
    public Character(String name, Position position) {
        this.name = name;
        this.position = position;
    }

    /**
     * Setzt die Spielfigur an die übergebene Position.
     *
     * @param position an die die Spielfigur gesetzt werden soll.
     */
    public void setPosition(Position position) {
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
     * Liefert die aktuelle Position der Spielfigur.
     *
     * @return die aktuelle Position der Spielfigur.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Konstruiert eine Spielfigur aus den Initialdaten.
     *
     * @param character die Spielfigur aus den Initialdaten.
     * @return die erzeuge Spielfigur.
     */
    public static Character fromJSON(InitialCharacterJSON character) {
        return new Character(character.getName(), character.getPosition());
    }

    @Override
    public String toString() {
        return "Character{" +
                "name='" + name + '\'' +
                ", position=" + position +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Character)) return false;
        Character character = (Character) o;
        return name.equals(character.name) &&
                position.equals(character.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, position);
    }
}
