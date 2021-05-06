package logic;

/**
 * Klasse die eine Waffe im Spiel repräsentiert.
 */
public class Weapon {

    //Der Name der Waffe
    private final String name;

    /**
     * Konstruktor.
     *
     * @param name der Name der Waffe
     */
    public Weapon(String name) {
        this.name = name;
    }

    /**
     * Liefert den Namen der Waffe
     *
     * @return den Namen der Waffe
     */
    public String getName() {
        return name;
    }

    /**
     * Konstruiert eine Waffe für die Logik aus einem String des Spielstandes.
     * Aus statischem Kontext.
     *
     * @param name der Name der zu erstellenden Waffe.
     * @return die erstellte Waffe.
     */
    public static Weapon fromJSON(String name) {
        return new Weapon(name);
    }
}
