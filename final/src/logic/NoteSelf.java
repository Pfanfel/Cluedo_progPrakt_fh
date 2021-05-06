package logic;

import logic.exceptions.CluedoException;
import logic.exceptions.ExceptionType;

/**
 * Aufzählung, welche die Notizen, die der Spieler über seine eigenen Karten machen kann darstellt.
 */
public enum NoteSelf {
    NOTHING, OWN, SHOWN_ONCE, SHOWN_TWICE, SHOWN_THRICE, SHOWN_OVER_TRICE;

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
            case OWN:
                res = "eigene Karte";
                break;
            case SHOWN_ONCE:
                res = "1x gezeigt";
                break;
            case SHOWN_TWICE:
                res = "2x gezeigt";
                break;
            case SHOWN_THRICE:
                res = "3x gezeigt";
                break;
            case SHOWN_OVER_TRICE:
                res = ">3x gezeigt";
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
    public static NoteSelf fromString(String str) throws CluedoException {
        NoteSelf res;
        if (str == null) {
            throw new CluedoException(ExceptionType.IllegalNoteSelfInSavedGame);
        }
        switch (str) {
            case "-":
                res = NOTHING;
                break;
            case "eigene Karte":
                res = OWN;
                break;
            case "1x gezeigt":
                res = SHOWN_ONCE;
                break;
            case "2x gezeigt":
                res = SHOWN_TWICE;
                break;
            case "3x gezeigt":
                res = SHOWN_THRICE;
                break;
            case ">3x gezeigt":
                res = SHOWN_OVER_TRICE;
                break;
            default:
                throw new CluedoException(ExceptionType.IllegalNoteSelfInSavedGame);
        }
        return res;
    }

    /**
     * Erhöht die Notiz um einen Schritt.
     *
     * @param note die zu erhöhende Notiz.
     * @return die erhöhte Notiz.
     */
    public static NoteSelf increment(NoteSelf note) {
        switch (note) {
            case OWN:
                return SHOWN_ONCE;
            case SHOWN_ONCE:
                return SHOWN_TWICE;
            case SHOWN_TWICE:
                return SHOWN_THRICE;
            case SHOWN_THRICE:
            case SHOWN_OVER_TRICE:
                return SHOWN_OVER_TRICE;
            default:
                return note;
        }
    }
}
