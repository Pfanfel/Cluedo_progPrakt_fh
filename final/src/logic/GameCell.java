package logic;

/**
 * Diese Klasse repräsentiert ein Spielfeldelement.
 * Aus diesen Elementen ist das Spielfeld aufgebaut.
 *
 * @author Michael Smirnov
 */
public class GameCell {
    //Ein Korridor.
    public static GameCell CORRIDOR = new GameCell();
    //Eine Wand.
    public static GameCell WALL = new GameCell();
    //Der Raum, falls es einer ist.
    private final Room room;

    /**
     * Privater Konstruktor für Korridor und Wand.
     */
    private GameCell() {
        this(null);
    }

    /**
     * Konstruiert ein Spielfeldelement aus einem Raum.
     * Repräsentiert nun diesen Raum
     *
     * @param room der Raum welcher repräsentiert werden soll.
     */
    public GameCell(Room room) {
        this.room = room;
    }

    /**
     * Liefert den Raum, sonst null.
     *
     * @return den Raum.
     */
    public Room getRoom() {
        return room;
    }

    /**
     * Prüft, ob es sich um einen Raum handelt.
     *
     * @return ob es sich um einen Raum handelt.
     */
    public boolean isRoom() {
        return this.room != null;
    }

    /**
     * Prüft, ob es sich um einen Korridor handelt.
     *
     * @return ob es sich um einen Korridor handelt.
     */
    public boolean isCorridor() {
        return this == CORRIDOR;
    }

}
