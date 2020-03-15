package oopnet.chess.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChessServer {

    private static final Logger logger = Logger.getLogger(ChessServer.class.getName());
    static final Random RANDOM = new SecureRandom();

    private final Connections connections = new Connections();

    public static void main(String[] args) {
        ChessServer server = new ChessServer();
        try {
            // start() method has a loop
            server.start();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An exception was caught and the server shut down", e);
        }
    }

    private void start() throws IOException {
        logger.info("Starting the server...");
        Properties properties = new Properties();

        // Load settings from server.properties file
        String fileName = "/server.properties";
        logger.info("Loading " + fileName);
        properties.load(this.getClass().getResourceAsStream(fileName));
        // Parse the port to use
        String portString = properties.getProperty("port", "8080");
        int port = Integer.parseInt(portString);
        // Start the server
        ServerSocket server = new ServerSocket(port);
        logger.info("Listening on port " + port);
        while (true) {
            try {
                // Accept a new connection
                Socket socket = server.accept();

                // Always generate a new token (at least for now)
                String token = generateToken();
                ClientConnection connection = new ClientConnection(socket, token, connections);
                connections.setConnectionToken(token, connection);
                // Start the ClientConnection in a new thread (as it implements Runnable)
                new Thread(connection).start();
                logger.info("Accepted a new connection from " + socket.getInetAddress().getHostAddress());
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to accept a connection", e);
            }
        }
    }

    /**
     * Generates a random String to be used as a token
     *
     * @return the random String, 16 characters
     */
    private String generateToken() {
        int len = 16;
        String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(characters.charAt(RANDOM.nextInt(characters.length())));
        }
        return sb.toString();
    }

}
