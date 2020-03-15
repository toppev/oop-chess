package oopnet.chess.core;

import oopnet.chess.core.pieces.ChessPiece;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static java.util.function.IntUnaryOperator.identity;
import static org.junit.jupiter.api.Assertions.*;

class MovesBuilderTest {

    @Test
    public void testMoveBuilderFunction() {
        Chessboard chessboard = new Chessboard();
        chessboard.setPieces(new ChessPiece[8][8]);
        // Get legal positions
        MovesBuilder builder = new MovesBuilder(chessboard, new Position(2, 'b'), ChessPiece.Color.BLACK);
        Set<Position> result = builder.function(c -> c + 1, identity()).getLegalPositions();
        // A few simple assertions
        assertEquals(6, result.size());
        assertTrue(result.contains(new Position(2, 'h')));
        assertFalse(result.contains(new Position(3, 'c')));

    }

}