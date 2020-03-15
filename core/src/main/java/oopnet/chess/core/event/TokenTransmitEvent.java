package oopnet.chess.core.event;

public class TokenTransmitEvent extends Event {

    private String token;

    public TokenTransmitEvent(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
