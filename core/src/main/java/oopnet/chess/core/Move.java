package oopnet.chess.core;


import oopnet.chess.core.pieces.ChessPiece;
import oopnet.chess.core.pieces.King;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

public class Move implements Serializable {

    // Transient so it won't be serialized and sent every time
    private transient Chessboard chessboard;
    private final ChessPiece piece;

    private final Position to;
    private final Position from;

    private boolean capture;
    private boolean check;
    private boolean checkmate;

    public Move(Chessboard chessboard, ChessPiece piece, Position to) {
        this.chessboard = chessboard;
        this.to = to;
        this.piece = piece;
        this.from = piece.getPosition();
        // Store these so the getters' results won't change even if the chessboard is modified
        this.capture = checkCapture();
        this.check = checkCheck();
        this.checkmate = checkCheckmate();
    }

    public Chessboard getChessboard() {
        return chessboard;
    }

    public ChessPiece getPiece() {
        return piece;
    }

    public Position getTo() {
        return to;
    }

    public Position getFrom() {
        return from;
    }

    /**
     * Get whether this move is a capture
     *
     * @return true if there is a piece on chessboard in the {@link #getTo()} position, otherwise false
     */
    public boolean isCapture() {
        return capture;
    }

    /**
     * Checks whether this move is a check. Basically just checks if the piece could capture the king next. Note that a
     * checkmate is always a check too.
     *
     * @return true if it's a check, otherwise false
     */
    public boolean isCheck() {
        return check;
    }


    /**
     * Get whether this move is a checkmate
     *
     * @return true if it's a checkmate, otherwise false
     */
    public boolean isCheckmate() {
        return checkmate;
    }

    /**
     * Checks if there is a piece on chessboard in the {@link #getTo()} position
     *
     * @return true if it's a capture
     */
    private boolean checkCapture() {
        return this.to.getPiece(chessboard) != null;
    }

    /**
     * Checks if the piece could capture the king next. Note that a checkmate is always a check too.
     *
     * @return true if it's a check
     */
    private boolean checkCheck() {
        return this.piece.getLegalMovePositions(chessboard, to).stream().anyMatch(pos -> pos.getPiece(chessboard) instanceof King);
    }

    /**
     * Get whether this move is a checkmate
     *
     * @return true if it's a checkmate
     */
    private boolean checkCheckmate() {
        // Find the king's position
        Optional<Position> optional = this.piece.getLegalMovePositions(chessboard, to).stream()
                                                .filter(pos -> pos.getPiece(chessboard) instanceof King)
                                                .findAny();
        if (optional.isPresent()) {
            Position pos = optional.get();
            King king = (King) pos.getPiece(chessboard);
            // Get where the king could move
            Set<Position> allowedMoves = king.getLegalMovePositions(chessboard, pos);
            // Either allowed moves is 0 (no positions to go)
            // Also need to check whether the attacker's "to" position would make it impossible to move to any of the
            // available spots as the piece hasn't actually moved yet and KingPiece#getLegalMovePositions ignores that
            return king.removeIllegalPositions(allowedMoves, chessboard, piece, to).isEmpty();
        }
        return false;
    }

    // https://en.wikipedia.org/wiki/Algebraic_notation_(chess)
    public String toString(Chessboard chessboard) {
        // TODO: support castling
        // Sometimes we need an identifier to know which piece moved
        // check of any other piece could have moved there
        String ambiguousIdentifier = "";
        for (ChessPiece piece : chessboard.getPiecesList()) {
            if (piece == null || piece == this.piece) {
                continue;
            }
            if (piece.getLegalMovePositions(chessboard, from).stream().filter(to::equals).findAny().isPresent()) {
                // we have ambiguity
                // first try if file is enough, then rank and finally both
                if (piece.getPosition().getFile() != from.getFile()) {
                    ambiguousIdentifier = String.valueOf(from.getFile());
                } else if (piece.getPosition().getRank() != from.getRank()) {
                    ambiguousIdentifier = String.valueOf(from.getRank());
                } else {
                    ambiguousIdentifier = from.toString();
                }
            }
        }
        char pieceLetter = piece.getANLetter();
        // Either "#" if it's a checkmate or "+" if it's a capture or "" (an empty string) if neither
        String checkOrCheckmate;
        if (isCheckmate()) {
            checkOrCheckmate = "#";
        } else {
            checkOrCheckmate = isCheck() ? "+" : "";
        }
        return pieceLetter + ambiguousIdentifier + (isCapture() ? "x" : "") + to.toString() + checkOrCheckmate;
    }


    public void setChessboard(Chessboard chessboard) {
        this.chessboard = chessboard;
    }
}
