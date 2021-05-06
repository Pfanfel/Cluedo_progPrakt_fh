package logic;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Diese Klasse stellt eine Position innerhalb des Spielbretts dar.
 *
 * @author Michael Smirnov
 */
public class Position {
    //Die x Koordinate der Position
    private final int x;
    //Die y Koordinate der Position
    private final int y;

    /**
     * Konstruktor.
     *
     * @param x die x Koordinate der Position.
     * @param y die y Koordinate der Position.
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Liefert die x Koordinate der Position
     *
     * @return die x Koordinate der Position
     */
    public int getX() {
        return x;
    }

    /**
     * Liefert die y Koordinate der Position
     *
     * @return die y Koordinate der Position
     */
    public int getY() {
        return y;
    }

    /**
     * Liefert alle Nachbarpositionen in einem gegebenen Radius
     *
     * @param radius der Radius in dem die Nachbarpunkte gesucht werden sollen.
     * @return Ein Set mit allen umliegenden Positionen (können auch negativ sein!)
     */
    Set<Position> getNeighbours(int radius) {
        Set<Position> res = new HashSet<>();
        int posX = this.x;
        int posY = this.y;
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                int currPosX = posX + x;
                int currPosY = posY + y;
                if (currPosX == this.x && currPosY != this.y || currPosX != this.x && currPosY == this.y) {
                    res.add(new Position(currPosX, currPosY));
                }
            }
        }
        return res;
    }

    @Override
    public String toString() {
        return "{" +
                "x:" + x +
                ", y:" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (!(o instanceof Position))
            return false;
        Position position = (Position) o;
        return x == position.x &&
                y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
