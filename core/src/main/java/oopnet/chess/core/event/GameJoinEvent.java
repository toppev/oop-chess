package oopnet.chess.core.event;

public class GameJoinEvent extends Event {

    private final String nickname;
    private final String gameIdentifier;

    public GameJoinEvent(String nickname, String gameIdentifier) {
        this.nickname = nickname;
        this.gameIdentifier = gameIdentifier;
    }

    public String getNickname() {
        return nickname;
    }

    public String getGameIdentifier() {
        return gameIdentifier;
    }
}
