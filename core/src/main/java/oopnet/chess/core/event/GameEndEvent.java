package oopnet.chess.core.event;

import oopnet.chess.core.Game;

public class GameEndEvent extends Event {

    private final Game.GameResult gameResult;

    public GameEndEvent(Game.GameResult gameResult) {
        this.gameResult = gameResult;
    }

    public Game.GameResult getGameResult() {
        return gameResult;
    }
}
