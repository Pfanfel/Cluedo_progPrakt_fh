package logic;


import logic.exceptions.CluedoException;

import java.util.*;

/**
 * Diese Klasse repräsentiert die möglichen Spielkarten im Spiel.
 *
 * @author Michael Smirnov
 */
public class Card {
    //Der Name der Karte
    private final String name;
    //Der Typ der Karte
    private final CardType type;

    /**
     * Konstruiert eine Karte.
     *
     * @param name der Name der Karte.
     * @param type der Typ der Karte.
     */
    public Card(String name, CardType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Teilt die Karten nach Kartentypen auf und schreibt diese in die übergebenen Listen.
     *
     * @param ownCards   die Karten welche zu teilen sind.
     * @param characters die Liste in die die Personenkarten geschrieben werden.
     * @param weapons    die Liste in die die Waffenkerten geschieben werden.
     * @param rooms      die Liste in die die Raumkarten geschieben werden.
     */
    public static void splitCards(List<Card> ownCards, List<String> characters, List<String> weapons, List<String> rooms) {
        for (Card card : ownCards) {
            switch (card.getType()) {
                case CHARACTER:
                    characters.add(card.getName());
                    break;
                case WEAPON:
                    weapons.add(card.getName());
                    break;
                case ROOM:
                    rooms.add(card.getName());
                    break;
            }
        }
    }

    /**
     * Liefert den Namen der Karte.
     *
     * @return der Name der Karte.
     */
    public String getName() {
        return name;
    }

    /**
     * Liefert den Kartentypen.
     *
     * @return der Typ der Karte.
     */
    public CardType getType() {
        return type;
    }

    /**
     * Prüft ob es sich um eine Raumkarte handelt.
     *
     * @return ob es eine Raumkarte ist.
     */
    public boolean isRoom() {
        return type == CardType.ROOM;
    }

    /**
     * Prüft ob es sich um eine Personenkarte handelt.
     *
     * @return ob es eine Personenkarte ist.
     */
    public boolean isCharacter() {
        return type == CardType.CHARACTER;
    }

    /**
     * Prüft ob es sich um eine Waffenkarte handelt.
     *
     * @return ob es eine Waffenkarte ist.
     */
    public boolean isWeapon() {
        return type == CardType.WEAPON;
    }

    /**
     * Liefert gegeben einer Collection aus Karten, die darin enthaltenen Räume.
     *
     * @param logic die Hauptspiellogik.
     * @param cards die Karten aus denen die Räume geholt werden sollen.
     * @return Eine Menge aus in der übergebenen Collention enhaltenen Räumen.
     */
    public static Set<Room> getRoomsFromCards(GameLogic logic, Collection<Card> cards) {
        //Alle bis auf die Räume entfernen
        cards.removeIf(card -> !card.isRoom());
        Set<Room> onlyRooms = new HashSet<>();
        //Die Karten zu Räumen umwandeln
        for (Card card : cards) {
            try {
                onlyRooms.add(logic.getRoomByName(card.getName()));
            } catch (CluedoException e) {
                //Zu jeder Karte in cards MUSS es einen Raum geben
                assert false;
            }
        }
        return onlyRooms;
    }

    /**
     * Liefert aus einer übergebenen Collection die darin enthaltenen Waffenkarten.
     *
     * @param cards die Collection aus der die Waffenkarten geholt werden sollen.
     * @return ein Set mit den enthaltenen Waffekarten.
     */
    public static Set<Card> getWeaponCardsFromCards(Collection<Card> cards) {
        Set<Card> result = new HashSet<>(cards);
        result.removeIf(card -> !card.isWeapon());
        return result;
    }

    /**
     * Liefert aus einer übergebenen Collection die darin enthaltenen Personenkarten.
     *
     * @param cards die Collection aus der die Personenkarten geholt werden sollen.
     * @return ein Set mit den enthaltenen Personenkarten.
     */
    public static Set<Card> getCharacterCardsFromCards(Collection<Card> cards) {
        Set<Card> result = new HashSet<>(cards);
        result.removeIf(card -> !card.isCharacter());
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card card = (Card) o;
        return Objects.equals(name, card.name) &&
                type == card.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "Card{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
