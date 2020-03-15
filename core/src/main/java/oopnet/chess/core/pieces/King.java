package oopnet.chess.core.pieces;

import oopnet.chess.core.Chessboard;
import oopnet.chess.core.MovesBuilder;
import oopnet.chess.core.Position;

import java.util.Set;

public class King extends ChessPiece {

    public King(Color color, Position position) {
        super(color, position);
    }

    @Override
    public Set<Position> getLegalMovePositions(Chessboard chessboard, Position position) {
        return new MovesBuilder(chessboard, position, getColor())
                .addIfLegal(position, 1, 0)
                .addIfLegal(position, -1, 0)
                .addIfLegal(position, 0, 1)
                .addIfLegal(position, 0, -1)
                .addIfLegal(position, 1, 1)
                .addIfLegal(position, -1, -1)
                .addIfLegal(position, 1, -1)
                .addIfLegal(position, -1, 1)
                .getLegalPositions();
    }

    @Override
    public char getNotationLetter() {
        return 'K';
    }

    /**
     * As king can not be placed in a position where it can be captured.
     *
     * @param positions        the positions to filter
     * @param chessboard       the current chessboard
     * @param attacker         the {@link ChessPiece} whose next moves should be filtered from the given positions
     * @param attackerPosition the attacker's current positions, the starting position where it will move from
     *
     * @return the given positions but possible moves the attacker could perform next have been removed
     */
    public Set<Position> removeIllegalPositions(Set<Position> positions, Chessboard chessboard, ChessPiece attacker,
                                                Position attackerPosition) {
        // Where the attacker could go next
        Set<Position> attackerMoves = attacker.getLegalMovePositions(chessboard, attackerPosition);
        positions.removeIf(attackerMoves::contains);
        return positions;
    }

    @Override
    public String getIcon() {
        return getColor() == Color.BLACK ? "♚" : "♔";
    }
}
