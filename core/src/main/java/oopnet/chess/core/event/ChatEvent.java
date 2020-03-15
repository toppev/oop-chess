package oopnet.chess.core.event;

public class ChatEvent extends Event {

    private String nickname;
    private String message;

    public ChatEvent(String nickname, String message) {
        this.nickname = nickname;
        this.message = message;
    }

    public String getNickname() {
        return nickname;
    }

    public String getMessage() {
        return message;
    }
}
