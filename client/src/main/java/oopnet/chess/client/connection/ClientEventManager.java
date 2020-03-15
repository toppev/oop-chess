package oopnet.chess.client.connection;

import oopnet.chess.client.ChessClient;
import oopnet.chess.core.event.Event;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Manages the current connection, events and event listeners
 */
public class ClientEventManager {

    private List<Consumer<Event>> listeners = new ArrayList<>();

    private ServerConnection connection;
    private ChessClient client;

    public ClientEventManager(ChessClient chessClient) {
        this.client = chessClient;
    }

    /**
     * Connect to the given server. Calling this will replace existing connection.
     *
     * @param inetAddress the address of the server
     * @param port        the port of the server
     * @param token       the token to use or null
     */
    public void connect(InetAddress inetAddress, int port, String token) {
        connection = new ServerConnection(inetAddress, port, token, this);
        connection.connect();
        // Because connection is a runnable and #run will keep blocking as long as there's a connection
        new Thread(connection).start();
    }

    /**
     * Send an event
     *
     * @param event the event to send
     *
     * @throws IllegalStateException if the connection is closed
     */
    public void sendEvent(Event event) {
        if (!isConnected()) {
            throw new IllegalStateException("Connection is closed");
        }
        CompletableFuture.runAsync(() -> {
            connection.sendEvent(event);
        });
    }

    /**
     * Add a new listener, will not check any duplicates
     *
     * @param listener the listener that will be invoked when the client receives an event
     */
    public void addListener(Consumer<Event> listener) {
        listeners.add(listener);
    }

    /**
     * Called when the connection receives a new event. Invokes all listeners
     */
    void receiveEvent(Event event) {
        listeners.forEach(l -> l.accept(event));
    }

    public boolean isConnected() {
        return connection != null && !connection.getSocket().isClosed();
    }
}
