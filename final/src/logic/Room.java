package logic;

import logic.json.InitialRoomJSON;

/**
 * Stellt einen Raum auf dem Spielfeld dar.
 *
 * @author Michael Smirnov
 */
public class Room {
    //Der Name des Raumes
    private final String name;
    //Der Mittelpunkt des Raumes
    private final Position midPoint;
    //Die Positionen der Türen des Raumes
    private final Position[] doors;
    //Der Raume der über den Geheimgang erreichbar ist.
    private Room secretCorridor;


    /**
     * Konstrukor des Raumes.
     *
     * @param name     der Name.
     * @param midPoint der Mittelpunkt.
     * @param doors    die Positionen der Türen.
     */
    public Room(String name, Position midPoint, Position[] doors) {
        this.name = name;
        this.midPoint = midPoint;
        this.doors = doors;
        this.secretCorridor = null;
    }

    /**
     * Liefert den Namen des Raumes.
     *
     * @return der Name des Raumes.
     */
    public String getName() {
        return name;
    }

    /**
     * Liefert den Mittelpunkt des Raumes.
     *
     * @return der Mittelpunkt des Raumes.
     */
    public Position getMidPoint() {
        return midPoint;
    }

    /**
     * Liefert die Türen des Raumes.
     *
     * @return die Türen des Raumes.
     */
    public Position[] getDoors() {
        return doors;
    }

    /**
     * Liefert den Raum, der über die Geheimgang erreichbar ist.
     *
     * @return der Raum, der über die Geheimgang erreichbar ist.
     */
    public Room getSecretCorridor() {
        return secretCorridor;
    }

    /**
     * Setzt den Geheimgang.
     *
     * @param room der Raum zu dem der Geheimgang führen soll.
     */
    public void setSecretCorridor(Room room) {
        this.secretCorridor = room;
    }

    /**
     * Konstruiert einen Raum für die Logik aus einem Raum des Spielstandformats.
     * Aus statischem Kontext.
     *
     * @return der Konstruierte Raum.
     */
    public static Room fromJSON(InitialRoomJSON room) {
        return new Room(room.getName(), room.getPosition(), room.getDoors());
    }

    @Override
    public String toString() {
        return "Room{" +
                "name='" + name + '\'' +
                '}';
    }
}