package logic;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Testklasse für die Postitonsklasse
 *
 * @author Michael Smirnov
 */
public class PositionTest {

    @Test
    public void getNeigbours_Radus_1() {
        Position testPos = new Position(1, 1);
        Set<Position> res = new HashSet<>();
        res.add(new Position(0, 1));
        res.add(new Position(1, 0));
        res.add(new Position(1, 2));
        res.add(new Position(2, 1));
        Set<Position> actual = testPos.getNeighbours(1);
        assertEquals(res, actual);
    }

}
