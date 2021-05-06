package logic;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Diese Klasse repräsentiert drei Karten, der drei verschiedenen im Spiel enthaltenen Typen.
 * Diese Klasse wird sowohl für Verdächtigungen als auch für Anklagen genutzt.
 *
 * @author Michael Smirnov
 */
public class CardTriple {
    //Die größe von drei Karten
    public static final int TRIPLE_SIZE = 3;
    //Die enthaltene Raumkarte.
    private Card room;
    //Die enthaltene Personenkarte.
    private Card character;
    //Die enthaltene Waffenkarte.
    private Card weapon;

    /**
     * Konstruiert ein leeres Triple.
     */
    public CardTriple() {
    }

    /**
     * Konstruiert ein befülltes Triple.
     *
     * @param room      die enthaltene Raumkarte.
     * @param character die enthaltene Personenkarte.
     * @param weapon    die enthaltene Waffenkarte.
     */
    public CardTriple(Card room, Card character, Card weapon) {
        this.room = room;
        this.character = character;
        this.weapon = weapon;
    }

    /**
     * Liefert die enthaltene Raumkarte.
     *
     * @return die Raumkarte.
     */
    public Card getRoom() {
        return room;
    }

    /**
     * Liefert die enthaltene Personenkarte.
     *
     * @return die Personenkarte.
     */
    public Card getCharacter() {
        return character;
    }

    /**
     * Liefert die enthaltene Waffenkarte.
     *
     * @return die Waffenkarte.
     */
    public Card getWeapon() {
        return weapon;
    }

    /**
     * Setzt die Raumkarte.
     *
     * @param room die Raumkarte.
     */
    public void setRoom(Card room) {
        this.room = room;
    }

    /**
     * Setzt die Personenkarte.
     *
     * @param character die Personenkarte.
     */
    public void setCharacter(Card character) {
        this.character = character;
    }

    /**
     * Setzt die Waffenkarte.
     *
     * @param weapon die Wafffenkarte.
     */
    public void setWeapon(Card weapon) {
        this.weapon = weapon;
    }

    /**
     * Prüft ob keine Karten enthlten sind.
     *
     * @return on keine Karte enthalten sind.
     */
    public boolean isEmpty() {
        return room == null && character == null && weapon == null;
    }

    /**
     * Prüft ob die übergebene Karte in dem Triple enthalten ist.
     *
     * @param cardToCheck die Karte, welche enthalten sein soll.
     * @return ob die Karte enthalten ist.
     */
    public boolean contains(Card cardToCheck) {
        Set<Card> cards = getCards();
        return cards.contains(cardToCheck);
    }


    /**
     * Prüft ob im Triple eine Raumkarte enthalten ist.
     *
     * @return ob im Triple eine Raumkarte enthalten ist.
     */
    public boolean hasRoom() {
        return room != null;
    }

    /**
     * Prüft ob im Triple eine Personenkarte enthalten ist.
     *
     * @return ob im Triple eine Personenkarte enthalten ist.
     */
    public boolean hasCharacter() {
        return character != null;
    }

    /**
     * Prüft ob im Triple eine Waffenkarte enthalten ist.
     *
     * @return ob im Triple eine Waffenkarte enthalten ist.
     */
    public boolean hasWeapon() {
        return weapon != null;
    }

    /**
     * Liefert ein Set bestehend aus den Karten, welche im Triple enthalten sind.
     *
     * @return ein Set aus den Karten des Tripels.
     */
    public Set<Card> getCards() {
        Set<Card> cards = new HashSet<>();
        if (this.weapon != null) {
            cards.add(this.weapon);
        }
        if (this.room != null) {
            cards.add(this.room);
        }
        if (this.character != null) {
            cards.add(this.character);
        }
        return cards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardTriple)) return false;
        CardTriple that = (CardTriple) o;
        return Objects.equals(room, that.room) &&
                Objects.equals(character, that.character) &&
                Objects.equals(weapon, that.weapon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(room, character, weapon);
    }

    @Override
    public String toString() {
        return "CardTriple{" +
                "room=" + room +
                ", character=" + character +
                ", weapon=" + weapon +
                '}';
    }
}
