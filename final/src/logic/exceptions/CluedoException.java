package logic.exceptions;

/**
 * Die Exceptionklasse welche von dem Programm genutzt wird, um spezifische Probleme zu signalisieren.
 *
 * @author Michael Smirnov
 */
public class CluedoException extends Exception {

    //Der Pfad, falls etwas nicht gefunden wurde, um es dem User mitzuteilen (Laden/Speichern)
    private String path;
    //Der eigentliche Exceptiontyp um nicht immer wieder neue Exceptionklassen zu erzeugen
    //Benutzung von Type um in der Gui sprachenunabhängige Fehlermeldungen anzeigen zu können
    private final ExceptionType type;


    /**
     * Konstruktor.
     *
     * @param type der Exceptiontyp.
     */
    public CluedoException(ExceptionType type) {
        this.type = type;
    }

    /**
     * Konstruktor, der ebenfalls einen String nimmt.
     *
     * @param type der Exceptiontyp.
     * @param path der Pfad zu einer nicht gefundenen Datei.
     */
    public CluedoException(ExceptionType type, String path) {
        this(type);
        this.path = path;
    }

    /**
     * Setzt den Pfad. (Laden/Speichern)
     *
     * @param path der Pfad.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Holt den Typen der Exception.
     *
     * @return der Typ der Exception.
     */
    public ExceptionType getType() {
        return type;
    }

    /**
     * Holt den Pfad der Exception. (Laden/Speichern)
     *
     * @return der Pfad der Exception.
     */
    public String getPath() {
        return path;
    }
}
