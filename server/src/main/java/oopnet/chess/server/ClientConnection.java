package oopnet.chess.server;

import oopnet.chess.core.Game;
import oopnet.chess.core.event.*;
import oopnet.chess.core.pieces.ChessPiece;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientConnection implements Runnable {

    private static final Logger logger = Logger.getLogger(ClientConnection.class.getName());

    private Connections connections;
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private String token;
    private GameContext gameContext;


    /**
     * Create a new instance of {@link ClientConnection}
     *
     * @param socket      the socket whose inputStream and outputStream are used
     * @param token       the token that will be sent to the client
     * @param connections an instance of {@link Connections} to use
     *
     * @throws IOException as described in {@link Socket#getInputStream()} and  {@link Socket#getOutputStream()}
     */
    public ClientConnection(Socket socket, String token, Connections connections) throws IOException {
        this.inputStream = new ObjectInputStream(socket.getInputStream());
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        this.socket = socket;
        this.token = token;
        this.connections = connections;
        // Send the new token to the client
        sendEvent(new TokenTransmitEvent(token));
    }

    // The method from Runnable interface
    @Override
    public void run() {
        // Read if the socket has not been closed
        while (!socket.isClosed()) {
            try {
                // doRead() blocks the thread so it may throw an error if the connection was lost
                doRead();
            } catch (IOException | ClassNotFoundException e) {
                // Something went wrong
                logger.log(Level.SEVERE, "Failed to read. Connection lost.", e);
                // Stop reading
                break;
            }
        }
    }

    private void doRead() throws IOException, ClassNotFoundException {
        // Read if there's anything
        Object obj = inputStream.readObject();
        // Only accept Event objects
        if (obj instanceof Event) {
            Event event = ((Event) obj);
            logger.info("Received a new event: " + event.getClass().getName());
            if (event instanceof TokenTransmitEvent) {
                // The client sent their token
                TokenTransmitEvent tokenEvent = (TokenTransmitEvent) event;
                handleTokenUpdate(tokenEvent.getToken());
            } else if (event instanceof GameCreateEvent) {
                Game game = ((GameCreateEvent) event).getGame();
                this.gameContext = new GameContext(connections, game);
                if (game.getCurrentPlayer() == ChessPiece.Color.BLACK) {
                    gameContext.setBlackToken(token);
                } else {
                    // Default to white as (by default) white starts unless Game#getCurrentPlayer is black
                    gameContext.setWhiteToken(token);
                }
                sendEvent(new ChatEvent("Server", "The game identifier is " + gameContext.getGameIdentifier()));
                logger.info("A new game has been created. Identifier: " + gameContext.getGameIdentifier());
            } else if (event instanceof GameJoinEvent) {
                handleGameJoin(((GameJoinEvent) event));
            }
            if (gameContext != null) {
                // Move and chat events are handled in gameContext
                gameContext.receiveEvent(event, token);
            }
        }
    }

    /**
     * Finds and updates the gameContext. Sends ChatEvent if the operation failed (the token is invalid or the game has
     * started)
     *
     * @param event the GameJoinEvent, not null
     */
    private void handleGameJoin(GameJoinEvent event) {
        String gameIdentifier = event.getGameIdentifier();
        GameContext gameContext = connections.getGameByIdentifier(gameIdentifier);
        if (gameContext != null) {
            this.gameContext = gameContext;
            if (gameContext.getWhiteToken() == null) {
                gameContext.setWhiteToken(token);
                sendEvent(new GameCreateEvent(gameContext.getGame()));
                gameContext.sendEvent(ChessPiece.Color.BLACK, new ChatEvent("Game",
                        event.getNickname() + " joined the game"));
            } else if (gameContext.getBlackToken() == null) {
                gameContext.setBlackToken(token);
                sendEvent(new GameCreateEvent(gameContext.getGame()));
                gameContext.sendEvent(ChessPiece.Color.WHITE, new ChatEvent("Game",
                        event.getNickname() + " joined the game"));
            } else {
                sendEvent(new ChatEvent("Server", "The game has already started!"));
            }
        } else {
            sendEvent(new ChatEvent("Server", "Invalid game identifier."));
        }
    }

    /**
     * Update token if it's a valid token. Also connects to their previous game if possible
     *
     * @param tokenCandidate the token to validate and use
     */
    private void handleTokenUpdate(String tokenCandidate) {
        if (tokenCandidate == null || tokenCandidate.length() != 16) {
            handleInvalidToken("Invalid token.");
        }
        // If the player is not in a game currently
        else if (gameContext != null) {
            handleInvalidToken("Can not change token now");
        } else {
            // Check if the player was in a game
            ClientConnection oldConnection = connections.getConnectionByToken(tokenCandidate);
            if (oldConnection != null) {
                // Put the player back in the old game
                gameContext = oldConnection.gameContext;
                // And update the old (new for this specific connection) GameContext's token
                String oldWhiteToken = oldConnection.gameContext.getWhiteToken();
                String oldBlackToken = oldConnection.gameContext.getBlackToken();
                if (tokenCandidate.equals(oldWhiteToken)) {
                    gameContext.setWhiteToken(tokenCandidate);
                } else if (tokenCandidate.equals(oldBlackToken)) {
                    gameContext.setBlackToken(tokenCandidate);
                } else {
                    // Shouldn't happen as the gameContext was found OwO
                    logger.warning("Client sent a valid token but neither black's old token nor white's old " +
                            "token matched it");
                    handleInvalidToken("Invalid token.");
                }
                // And send the game
                sendEvent(new GameCreateEvent(gameContext.getGame()));
            }
            // Finally replace the connection and current token with the new one
            token = tokenCandidate;
            connections.setConnectionToken(token, this);
        }
    }

    /**
     * Sends a message and the correct token to the client
     *
     * @param message the message to send
     */
    private void handleInvalidToken(String message) {
        sendEvent(new ChatEvent("Server", message));
        sendEvent(new TokenTransmitEvent(token));
    }

    /**
     * Send the given event to the client's connection
     *
     * @param event the event to send
     */
    public void sendEvent(Event event) {
        try {
            outputStream.writeObject(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GameContext getGameContext() {
        return gameContext;
    }

    /**
     * Sets the token to be used but does not send it to client. A new {@link TokenTransmitEvent} should be sent to
     * actually send the token.
     *
     * @param token the token string to use
     */
    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

}
