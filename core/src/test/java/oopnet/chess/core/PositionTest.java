package oopnet.chess.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PositionTest {

    @Test
    public void testToString() {
        Position positionWithIntFile = new Position(2, 2);
        assertEquals("b2", positionWithIntFile.toString());

        Position positionWithCharFile = new Position(3, 'a');
        assertEquals("a3", positionWithCharFile.toString());
    }

    @Test
    public void testFileAsInt() {
        Position position = new Position(1, 'a');
        assertEquals(1, position.getFileAsInt());
    }
}