package logic.json;

/**
 * Repräsenitert die Karten eines Spielers in einer Spielstandsdatei. (JSON-Format)
 *
 * @author Michael Smirnov
 */
public class CardsJSON {
    //Die Personenkarten
    private final String[] persons;
    //Die Raumkarten
    private final String[] rooms;
    //Die Waffenkarten
    private final String[] weapons;

    /**
     * Konstruktor
     *
     * @param persons die Personenkarten
     * @param rooms   die Raumkarten
     * @param weapons die Waffenkarten
     */
    public CardsJSON(String[] persons, String[] rooms, String[] weapons) {
        this.persons = persons;
        this.rooms = rooms;
        this.weapons = weapons;
    }

    /**
     * Liefert die Personenkarten
     *
     * @return die Personenkarten
     */
    public String[] getPersons() {
        return persons;
    }

    /**
     * Liefert die Raumkarten
     *
     * @return die Raumkarten
     */
    public String[] getRooms() {
        return rooms;
    }

    /**
     * Liefert die Waffenkarten
     *
     * @return die Waffenkarten
     */
    public String[] getWeapons() {
        return weapons;
    }
}
