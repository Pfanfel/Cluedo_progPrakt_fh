package logic;

import logic.exceptions.CluedoException;
import logic.exceptions.ExceptionType;

/**
 * Aufzählung, welche die Notizen über andere Spieler darstellt.
 *
 * @author Michael Smirnov
 */
public enum NoteOthers {
    NOTHING, SEEN, HAS_NOT, SUSPITION_A, SUSPITION_B, SUSPITION_C, SUSPITION_D;

    /**
     * Strings die in der Spielstandsdatei gespeichert werden
     * Nicht für die GUI!
     *
     * @return der String, welcher zum Speichern des Spielstandes verwendet wird.
     */
    @Override
    public String toString() {
        String res;
        switch (this) {
            case NOTHING:
                res = "-";
                break;
            case SEEN:
                res = "gesehen";
                break;
            case HAS_NOT:
                res = "hat nicht";
                break;
            case SUSPITION_A:
                res = "Verdacht A";
                break;
            case SUSPITION_B:
                res = "Verdacht B";
                break;
            case SUSPITION_C:
                res = "Verdacht C";
                break;
            case SUSPITION_D:
                res = "Verdacht D";
                break;
            default:
                throw new IllegalStateException("Kann nicht passieren!");
        }
        return res;
    }

    /**
     * Konvertiert einen String in die jew. Notiz.
     * Nicht für die GUI!
     *
     * @param str der String der eine Notiz repräsentiert.
     * @return die entprechende Notiz.
     * @throws CluedoException falls zu diesem String keine Notizart vorhanden.
     */
    public static NoteOthers fromString(String str) throws CluedoException {
        NoteOthers res;
        switch (str) {
            case "-":
                res = NOTHING;
                break;
            case "gesehen":
                res = SEEN;
                break;
            case "hat nicht":
                res = HAS_NOT;
                break;
            case "Verdacht A":
                res = SUSPITION_A;
                break;
            case "Verdacht B":
                res = SUSPITION_B;
                break;
            case "Verdacht C":
                res = SUSPITION_C;
                break;
            case "Verdacht D":
                res = SUSPITION_D;
                break;
            default:
                throw new CluedoException(ExceptionType.IllegalNoteOthersInSavedGame);
        }
        return res;
    }
}
