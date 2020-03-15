package oopnet.chess.server;

import oopnet.chess.core.Chessboard;
import oopnet.chess.core.Game;
import oopnet.chess.core.Move;
import oopnet.chess.core.event.*;
import oopnet.chess.core.pieces.ChessPiece;
import oopnet.chess.core.pieces.King;

import java.util.Objects;
import java.util.logging.Logger;

public class GameContext {


    private static final Logger logger = Logger.getLogger(GameContext.class.getName());

    // Both players' tokens
    private final Connections connections;
    private final String gameIdentifier;
    private final Game game;
    private String whiteToken;
    private String blackToken;


    public GameContext(Connections connections, Game game) {
        Objects.requireNonNull(connections);
        Objects.requireNonNull(game);
        this.connections = connections;
        this.game = game;
        this.gameIdentifier = generateIdentifier();
    }

    private String generateIdentifier() {
        String token;
        do {
            token = String.valueOf(ChessServer.RANDOM.nextInt(90000) + 9999);
        } while (connections.getConnectionByToken(token) != null);
        return token;
    }

    /**
     * Called when the server receives an {@link Event} from the players
     *
     * @param event       the event that was sent
     * @param playerToken the player who sent the event
     */
    public void receiveEvent(Event event, String playerToken) {
        ChessPiece.Color senderColor = getColor(playerToken);
        ChessPiece.Color opponentColor = senderColor == ChessPiece.Color.BLACK ? ChessPiece.Color.WHITE :
                ChessPiece.Color.BLACK;
        if (senderColor == null) {
            // Something is wrong or player tried to cheat
            ClientConnection connection = connections.getConnectionByToken(playerToken);
            // Should never happen
            if (connection != null) {
                connection.sendEvent(new ChatEvent("Server", "Something went wrong."));
            }
        } else if (event instanceof PieceMoveEvent) {
            PieceMoveEvent moveEvent = ((PieceMoveEvent) event);
            if (!validateMove(moveEvent, senderColor)) {
                sendEvent(senderColor, new ChatEvent("Server", "Invalid move."));
            } else {
                if (game.handleMove(moveEvent) instanceof King) {
                    endGame(senderColor == ChessPiece.Color.WHITE ? Game.GameResult.WHITE_WINS : Game.GameResult.BLACK_WINS);
                }
                sendEvent(opponentColor, moveEvent);
                Move move = moveEvent.getMove(game.getChessboard());
                if (move.isCheckmate()) {
                    endGame(senderColor == ChessPiece.Color.WHITE ? Game.GameResult.WHITE_WINS : Game.GameResult.BLACK_WINS);
                }
            }
        } else if (event instanceof ChatEvent) {
            ChatEvent chatEvent = ((ChatEvent) event);
            // Send the message to both players
            sendEvent(opponentColor, new ChatEvent(chatEvent.getNickname(), chatEvent.getMessage()));
            sendEvent(senderColor, new ChatEvent(chatEvent.getNickname(), chatEvent.getMessage()));
        } else if (event instanceof SurrenderEvent) {
            endGame(senderColor == ChessPiece.Color.WHITE ? Game.GameResult.WHITE_SURRENDERS :
                    Game.GameResult.BLACK_SURRENDERS);
        } else if (event instanceof DrawOfferEvent) {
            if (game.getDrawOffer() == senderColor.getOpposite()) {
                endGame(Game.GameResult.DRAW);
            } else {
                game.setDrawOffer(senderColor);
                sendEvent(senderColor.getOpposite(), new ChatEvent("Server",
                        senderColor + " offered a draw." +
                                "\nClick \"offer a draw\" to accept."));
            }
        }
    }

    private boolean validateMove(PieceMoveEvent moveEvent, ChessPiece.Color senderColor) {
        // It's the player's turn
        if (game.getCurrentPlayer() != senderColor) {
            logger.warning(String.format("%s tried to move but it's %s's turn!", senderColor, game.getCurrentPlayer()));
            return false;
        }
        Chessboard oldBoard = game.getChessboard();
        Move move = moveEvent.getMove(oldBoard);
        ChessPiece piece = move.getFrom().getPiece(game.getChessboard());
        // Can not move nonexistent pieces
        if (piece != null) {
            if (piece.getColor() == senderColor) {
                // And must be a legal move
                if (piece.getLegalMovePositions(oldBoard).contains(move.getTo())) {
                    return true;
                } else {
                    logger.warning(String.format("%s tried to move to an illegal position", senderColor));
                }
            } else {
                logger.warning(String.format("%s tried to move %s piece", senderColor, piece.getColor()));
            }
        } else {
            logger.warning(String.format("%s tried to move nonexistent piece", senderColor));
        }
        return false;
    }

    private void endGame(Game.GameResult result) {
        GameEndEvent event = new GameEndEvent(result);
        sendEvent(ChessPiece.Color.BLACK, event);
        sendEvent(ChessPiece.Color.WHITE, event);
    }

    /**
     * Send an event to the player
     *
     * @param color the player who will receive the event
     * @param event the even to send
     *
     * @return whether the given color has a connection
     */
    public boolean sendEvent(ChessPiece.Color color, Event event) {
        String token = getColorToken(color);
        if (token != null) {
            ClientConnection connection = connections.getConnectionByToken(token);
            // Should never be null
            if (connection != null) {
                connection.sendEvent(event);
                return true;
            }
        }
        return false;
    }

    /**
     * Get the given {@link ChessPiece.Color}'s token
     *
     * @param color the color
     *
     * @return the white player's token if the given color is {@code ChessPiece.Color.WHITE} or the black player's token
     * if the color is  {@code ChessPiece.Color.BLACK}, otherwise null as the color is null
     */
    private String getColorToken(ChessPiece.Color color) {
        switch (color) {
            case WHITE:
                return whiteToken;
            case BLACK:
                return blackToken;
            default:
                return null;
        }
    }

    /**
     * Get the Color of the player's token.
     *
     * @param token the player's token string, not null
     *
     * @return If the token matches white's token this returns ChessPiece.Color.WHITE, if it matches the black's token
     * this returns ChessPiece.Color.WHITE, otherwise this returns null
     */
    private ChessPiece.Color getColor(String token) {
        if (whiteToken != null && whiteToken.equals(token)) {
            return ChessPiece.Color.WHITE;
        }
        if (blackToken != null && blackToken.equals(token)) {
            return ChessPiece.Color.BLACK;
        }
        return null;
    }

    public String getGameIdentifier() {
        return gameIdentifier;
    }

    public String getWhiteToken() {
        return whiteToken;
    }

    public String getBlackToken() {
        return blackToken;
    }

    public void setWhiteToken(String whiteToken) {
        Objects.requireNonNull(whiteToken);
        logger.info(String.format("whiteToken updated to %s", whiteToken));
        this.whiteToken = whiteToken;
    }

    public void setBlackToken(String blackToken) {
        Objects.requireNonNull(blackToken);
        logger.info(String.format("blackToken updated to %s", blackToken));
        this.blackToken = blackToken;
    }

    public Game getGame() {
        return game;
    }
}
