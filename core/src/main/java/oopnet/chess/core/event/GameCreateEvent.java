package oopnet.chess.core.event;

import oopnet.chess.core.Game;

public class GameCreateEvent extends Event {

    private final Game game;

    public GameCreateEvent(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

}