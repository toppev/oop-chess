package oopnet.chess.client;

import oopnet.chess.client.connection.ClientEventManager;
import oopnet.chess.core.Chessboard;
import oopnet.chess.core.Game;
import oopnet.chess.core.event.GameCreateEvent;
import oopnet.chess.core.event.GameJoinEvent;
import oopnet.chess.core.pieces.ChessPiece;

import javax.swing.*;
import java.net.InetAddress;

public class ChessClient {

    private ClientProperties properties = new ClientProperties();
    private ClientEventManager eventManager = new ClientEventManager(this);

    public static void main(String[] args) {
        new ChessClient().start();
        System.exit(0);
    }

    private void start() {
        System.out.println("Starting client...");
        // Open the selector dialog
        // The callback is fired when the user clicks join game/server/connect
        new GameSelectorDialog(properties, (server, id, nick) -> {
            System.out.println(String.format("Connecting: server: %s, game identifier: %s, " +
                    "nickname: %s", server, id, nick));
            try {
                Object portObj = properties.get(ClientProperties.SERVER_PORT_KEY);
                int port = portObj == null ? 8080 : Integer.parseInt(portObj.toString());
                Object token = properties.get(ClientProperties.TOKEN_KEY);

                eventManager.connect(InetAddress.getByName(server), port, token == null ? null : token.toString());
                if (!eventManager.isConnected()) {
                    System.out.println("Failed to connect. Please check input details");
                    return false;
                }
                if (id == null || id.trim().isEmpty()) {
                    // Create a new game
                    Chessboard chessboard = new Chessboard();
                    chessboard.loadDefault();
                    Game game = new Game(chessboard);
                    buildGameUI(game, ChessPiece.Color.WHITE);
                    // Temp fix: makes sure the UI is ready for the identifier message
                    Thread.sleep(2000);
                    eventManager.sendEvent(new GameCreateEvent(game));
                } else {
                    // Or join the game
                    eventManager.sendEvent(new GameJoinEvent(nick, id));
                }
                // Save the working properties
                properties.setProperty(ClientProperties.NICKNAME_KEY, nick);
                properties.setProperty(ClientProperties.SERVER_ADDRESS_KEY, server);
                if (token != null) {
                    properties.setProperty(ClientProperties.TOKEN_KEY, token.toString());
                }
                properties.save();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });

        eventManager.addListener(event -> {
            if (event instanceof GameCreateEvent) {
                GameCreateEvent createEvent = ((GameCreateEvent) event);
                buildGameUI(createEvent.getGame(), ChessPiece.Color.BLACK);
            }
        });

    }

    public void buildGameUI(Game game, ChessPiece.Color color) {
        // Should be used to update UI
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new ChessGameUI(this, game, properties.getProperty(ClientProperties.NICKNAME_KEY), color);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.pack();
            // Maybe fix later
            // Don't allow resizing for now
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public ClientEventManager getEventManager() {
        return eventManager;
    }
}
