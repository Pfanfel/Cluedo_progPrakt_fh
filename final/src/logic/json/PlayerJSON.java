package logic.json;

import logic.Position;

/**
 * Repräsenitert einen teilnehmenden Spieler in einer Spielstandsdatei. (JSON-Format)
 *
 * @author Michael Smirnov
 */

public class PlayerJSON {
    //Der Spielername
    private final String name;
    //Seine Intilligenz (KI-Stärke)
    private final String iq;
    //Sein aufenthaltsort (einer der Räume oder der Flur)
    private final String room;
    //Seine genaue Position auf dem Spielfeld
    private final Position position;
    //Ob er aktuell in den Raum gewünscht wurde (durch Verdächtigung)
    private final boolean requested;
    //Seine Karten (Personenkarten, Räume, Waffen)
    private final CardsJSON cards;

    /**
     * Konstruktor.
     *
     * @param name      der Spielername
     * @param iq        seine Intilligenz (KI-Stärke)
     * @param room      sein Aufenthaltsort (einer der Räume oder der Flur)
     * @param position  seine genaue Position auf dem Spielfeld
     * @param requested ob er aktuell in den Raum gewünscht wurde
     * @param cards     seine Karten auf der Hand
     */
    public PlayerJSON(String name, String iq, String room, Position position, boolean requested, CardsJSON cards) {
        this.name = name;
        this.iq = iq;
        this.room = room;
        this.position = position;
        this.requested = requested;
        this.cards = cards;
    }

    /**
     * Liefert den Spielernamen.
     *
     * @return der Spielername.
     */
    public String getName() {
        return name;
    }

    /**
     * Liefert seine Intilligenz (KI-Stärke).
     *
     * @return seine Intilligenz (KI-Stärke).
     */
    public String getIq() {
        return iq;
    }


    /**
     * Liefert sein Aufenthaltsort (einer der Räume oder der Flur).
     *
     * @return sein Aufenthaltsort (einer der Räume oder der Flur).
     */
    public String getRoom() {
        return room;
    }

    /**
     * Liefert seine genaue Position auf dem Spielfeld.
     *
     * @return seine genaue Position auf dem Spielfeld.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Liefert ob er aktuell in den Raum gewünscht wurde.
     *
     * @return ob er aktuell in den Raum gewünscht wurde.
     */
    public boolean getRequested() {
        return requested;
    }

    /**
     * Liefert seine Karten auf der Hand.
     *
     * @return seine Karten auf der Hand.
     */
    public CardsJSON getCards() {
        return cards;
    }

}
