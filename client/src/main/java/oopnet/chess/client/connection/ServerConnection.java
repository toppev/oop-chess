package oopnet.chess.client.connection;

import oopnet.chess.core.event.Event;
import oopnet.chess.core.event.TokenTransmitEvent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerConnection implements Runnable {


    private static final Logger logger = Logger.getLogger(ServerConnection.class.getName());

    private InetAddress host;
    private int port;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String token;
    private ClientEventManager clientEventManager;

    /**
     * Create a new instance of {@link ServerConnection}. Does not connect automatically, instead {@link #run()} needs
     * to be called.
     *
     * @param host               the host address of the server
     * @param port               the port of the server
     * @param token              the token to be sent, or null
     * @param clientEventManager the {@link ClientEventManager} that will receive the incoming events
     */
    public ServerConnection(InetAddress host, int port, String token, ClientEventManager clientEventManager) {
        this.host = host;
        this.port = port;
        this.token = token;
        this.clientEventManager = clientEventManager;
    }

    public void connect() {
        if (socket != null) {
            throw new IllegalStateException("already connected");
        }
        try {
            socket = new Socket(host, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            if (token != null && token.isEmpty()) {
                sendEvent(new TokenTransmitEvent(token));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Connection failed", e);
        }
    }

    @Override
    public void run() {
        if (socket == null) {
            throw new IllegalStateException("#connect has not been called");
        }
        while (!socket.isClosed()) {
            try {
                // Blocks until there's an object
                doRead();
            } catch (Exception e) {
                // If it throws something, we most likely lost connection (maybe)
                logger.log(Level.SEVERE, "Failed to read. Connection lost.", e);
                break;
            }
        }
    }

    private void doRead() throws IOException, ClassNotFoundException {
        // Blocks until there's an object
        Object obj = inputStream.readObject();
        // Only accept Event objects
        if (obj instanceof Event) {
            Event event = ((Event) obj);
            if (event instanceof TokenTransmitEvent) {
                // The client sent their token
                TokenTransmitEvent tokenEvent = (TokenTransmitEvent) event;
                // Replace current token with the new one
                token = tokenEvent.getToken();
                logger.info(String.format("Token updated to %s", token));
            }
            clientEventManager.receiveEvent(event);
        }
    }

    /**
     * Send the given event to server.
     *
     * @param event the event to send
     *
     * @see ObjectOutputStream#writeObject(Object)
     */
    public void sendEvent(Event event) {
        // Send the event
        try {
            outputStream.writeObject(event);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to send an event", e);
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
