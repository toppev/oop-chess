package oopnet.chess.core.pieces;

import oopnet.chess.core.Chessboard;
import oopnet.chess.core.MovesBuilder;
import oopnet.chess.core.Position;

import java.util.Set;

public class Pawn extends ChessPiece {

    private boolean hasMoved;

    public Pawn(Color color, Position position) {
        super(color, position);
    }

    @Override
    public Set<Position> getLegalMovePositions(Chessboard chessboard, Position position) {
        MovesBuilder builder = new MovesBuilder(chessboard, position, getColor());
        // If the piece has not been moved it can move two spaces
        char file = position.getFile();
        // Forward move
        int rankModifier = getColor() == Color.WHITE ? 1 : -1;
        int rank = position.getRank() + rankModifier;
        // Pawn can't capture in the same direction
        addIfValid(file, rank, chessboard, false, builder);
        // has not moved and there's nothing in front of it
        if (!hasMoved && builder.checkBoundaries(position.getFileAsInt(), rank) == 0) {
            // Pawn can't capture in the same direction
            // add rankModifier again (+2)
            addIfValid(file, rank + rankModifier, chessboard, false, builder);
        }
        // But can capture forward and left or right
        file = (char) (position.getFile() + 1);
        addIfValid(file, rank, chessboard, true, builder);

        file = (char) (position.getFile() - 1);
        addIfValid(file, rank, chessboard, true, builder);

        return builder.getLegalPositions();
    }

    private void addIfValid(char file, int rank, Chessboard chessboard, boolean capture, MovesBuilder builder) {
        if (Position.validate(rank, file)) {
            boolean isCapture = new Position(rank, file).getPiece(chessboard) != null;
            if (isCapture == capture) {
                int ret = builder.checkBoundaries(file - 'a' + 1, rank);
                if (ret >= 0) {
                    builder.add(new Position(rank, file));
                }
            }
        }
    }

    @Override
    public char getNotationLetter() {
        return 'P';
    }

    /**
     * Pawns don't have an identifier in AN
     *
     * @return 0 (the NUL/null character)
     */
    @Override
    public Character getANLetter() {
        return 0;
    }

    @Override
    public void setPosition(Position position) {
        super.setPosition(position);
        hasMoved = true;
    }

    @Override
    public String getIcon() {
        return getColor() == Color.BLACK ? "♟" : "♙";
    }
}
