package logic.json;

/**
 * Repräsenitert die kompletten Notizen eines Spielers über einen anderen Spieler in einer Spielstandsdatei.
 * (JSON-Format)
 *
 * @author Michael Smirnov
 */
public class MemoJSON {
    //Notizen zu den Personen
    private final String[] persons;
    //Notizen zu den Räumen
    private final String[] rooms;
    //Notizen zu den Waffen
    private final String[] weapons;

    /**
     * Konstruktor.
     *
     * @param persons Notizen zu den Personen
     * @param rooms   Notizen zu den Räumen
     * @param weapons Notizen zu den Waffen
     */
    public MemoJSON(String[] persons, String[] rooms, String[] weapons) {
        this.persons = persons;
        this.rooms = rooms;
        this.weapons = weapons;
    }

    /**
     * Liefert die Notizen zu den Personen.
     *
     * @return die Notizen zu den Personen.
     */
    public String[] getPersons() {
        return persons;
    }

    /**
     * Liefert die Notizen zu den Räumen.
     *
     * @return die Notizen zu den Räumen.
     */
    public String[] getRooms() {
        return rooms;
    }

    /**
     * Liefert die Notizen zu den Waffen.
     *
     * @return die Notizen zu den Waffen.
     */
    public String[] getWeapons() {
        return weapons;
    }
}
