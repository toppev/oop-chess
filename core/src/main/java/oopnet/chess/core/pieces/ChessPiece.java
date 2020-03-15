package oopnet.chess.core.pieces;

import oopnet.chess.core.Chessboard;
import oopnet.chess.core.Position;

import java.io.Serializable;
import java.util.Set;

public abstract class ChessPiece implements Serializable {

    private final Color color;
    private Position position;

    protected ChessPiece(Color color, Position position) {
        this.color = color;
        this.position = position;
    }

    public Color getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }

    public abstract String getIcon();

    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Get all legal move positions from the given position.
     *
     * @param chessboard the current {@link Chessboard}
     * @param position   the position of this piece. This position should not be included in the returned list
     *
     * @return a set of legal positions
     */
    public abstract Set<Position> getLegalMovePositions(Chessboard chessboard, Position position);

    /**
     * Get all legal move positions from the current location
     *
     * @param chessboard the current chessboard
     *
     * @return a set of all legal positions from the currente position
     *
     * @see #getLegalMovePositions(Chessboard, Position)
     */
    public Set<Position> getLegalMovePositions(Chessboard chessboard) {
        return getLegalMovePositions(chessboard, position);
    }

    /**
     * Get the character used in notations.
     *
     * @return the unique character that refers to this piece
     */
    public abstract char getNotationLetter();

    /**
     * Get the letter in FEN (Forsyth–Edwards Notation). For black pieces this returns {@link #getNotationLetter()} in
     * lowercase and for white pieces this returns in uppercase.
     *
     * @return the FEN character
     *
     * @see #getNotationLetter()
     */
    public char getFENLetter() {
        char c = getNotationLetter();
        return getColor() == Color.BLACK ? Character.toLowerCase(c) : Character.toUpperCase(c);
    }

    /**
     * Get the letter in AN (Algebraic Notation). The character is always uppercase.
     *
     * @return the AN character
     *
     * @see #getNotationLetter()
     */
    public Character getANLetter() {
        return Character.toUpperCase(getNotationLetter());
    }

    public enum Color {
        BLACK,
        WHITE;

        /**
         * Convenience method to get the opposite color <br/>
         *
         * @return the other color
         */
        public Color getOpposite() {
            return this == BLACK ? WHITE : BLACK;
        }
    }
}
