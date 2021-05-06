package logic.json;

/**
 * Repräsenitert eine Waffe in einer Spielstandsdatei. (JSON-Format)
 *
 * @author Michael Smirnov
 */
public class WeaponJSON {
    //Der Waffenname
    private final String name;
    //Aufenthaltsort der Waffe (einer der Räume)
    private final String room;

    /**
     * Konstruktor.
     *
     * @param name der Waffenname
     * @param room der Aufenthaltsort der Waffe (einer der Räume)
     */
    public WeaponJSON(String name, String room) {
        this.name = name;
        this.room = room;
    }

    /**
     * Liefert den Waffennamen.
     *
     * @return der Waffenname.
     */
    public String getName() {
        return name;
    }

    /**
     * Liefert den aufenthaltsort der Waffe.
     *
     * @return Der Aufenthaltsort der Waffe (einer der Räume).
     */
    public String getRoom() {
        return room;
    }
}
