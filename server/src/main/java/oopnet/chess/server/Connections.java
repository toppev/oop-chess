package oopnet.chess.server;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Connections {

    // Keep a map of connections
    // The key is the client's token
    private final Map<String, ClientConnection> connections = new ConcurrentHashMap<>();

    public ClientConnection getConnectionByToken(String token) {
        return connections.get(token);
    }

    public void setConnectionToken(String token, ClientConnection clientConnection) {
        connections.put(token, clientConnection);
    }

    /**
     * Find a {@link GameContext} by the given gameIdentifier. Goes through all {@link ClientConnection} and {@link
     * ClientConnection#getGameContext()} and matches {@link GameContext#getGameIdentifier()}
     *
     * @param gameIdentifier the identifier (case insensitive), not null
     *
     * @return the {@link GameContext} or null
     */
    public GameContext getGameByIdentifier(String gameIdentifier) {
        return connections.values().stream()
                          .map(ClientConnection::getGameContext)
                          .filter(Objects::nonNull)
                          .filter(context -> gameIdentifier.equalsIgnoreCase(context.getGameIdentifier()))
                          .findAny().orElse(null);
    }
}
