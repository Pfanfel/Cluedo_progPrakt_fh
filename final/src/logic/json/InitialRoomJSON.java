package logic.json;

import logic.Position;

/**
 * Repräsenitert einen Raum in der initialen Konfigurationsdatei des Spiels. (JSON-Format)
 * Lässt sich theoretisch anpassen um andere Räume im Spiel zu ermöglichen.
 *
 * @author Michael Smirnov
 */
public class InitialRoomJSON {
    //Der Name des Raumes
    private final String name;
    //Der Mittelpunkt des Raumes
    private final Position position;
    //Die Türen des Raumes (Flurzellen um in den Raum zu kommen)
    private final Position[] doors;
    //Geheimgang der zu einem anderem Raum führt (kann null sein für kein)
    private final String secretCorridor;

    /**
     * Konstruktor.
     *
     * @param name           der Name des Raumes
     * @param position       der Mittelpunkt des Raumes
     * @param doors          die Türen des Raumes
     * @param secretCorridor Geheimgang der zu einem anderem Raum führt (kann null sein für kein)
     */
    public InitialRoomJSON(String name, Position position, Position[] doors, String secretCorridor) {
        this.name = name;
        this.position = position;
        this.doors = doors;
        this.secretCorridor = secretCorridor;
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
    public Position getPosition() {
        return position;
    }

    /**
     * Liefert die Türen des Raumes.
     *
     * @return die Türen des Raumes. (Flurzellen um in den Raum zu kommen)
     */
    public Position[] getDoors() {
        return doors;
    }

    /**
     * Liefert den Geheimgang des Raumes.
     *
     * @return den Geheimgang des Raumes. (kann null sein, falls kein Geheimgang vorhanden)
     */
    public String getSecretCorridor() {
        return secretCorridor;
    }
}
