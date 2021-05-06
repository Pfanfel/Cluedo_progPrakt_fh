package logic.json;

/**
 * Repräsenitert die kompletten Notizen eines Spielers über einen anderen bestimmten Spieler in
 * einer Spielstandsdatei. (JSON-Format)
 *
 * @author Michael Smirnov
 */
public class NoteJSON {
    //Der Name des Spielers über den diese Notizen sind.
    private final String name;
    //Die Notizen zu diesem Spieler
    private final MemoJSON memo;

    /**
     * Konstruktor.
     *
     * @param name der Name des Spielers über den diese Notizen sind.
     * @param memo die Notizen zu diesem Spieler.
     */
    public NoteJSON(String name, MemoJSON memo) {
        this.name = name;
        this.memo = memo;
    }

    /**
     * Liefert der Name des Spielers über den diese Notizen sind.
     *
     * @return der Name des Spielers über den diese Notizen sind
     */
    public String getName() {
        return name;
    }

    /**
     * Liefert die Notizen zu diesem Spieler.
     *
     * @return die Notizen zu diesem Spieler.
     */
    public MemoJSON getMemo() {
        return memo;
    }
}
