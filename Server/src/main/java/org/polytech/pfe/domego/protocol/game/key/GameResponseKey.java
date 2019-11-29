package org.polytech.pfe.domego.protocol.game.key;

public enum GameResponseKey {
    RESPONSE("response"),
    RESOURCES("resources"),
    MONEY("money"),
    GAMEID("gameID"),
    NOPC("numberOfPlayersConnected"),
    PLAYER("player"),
    USERID("userID"),
    AMOUNT("amount"),
    ROLEID("roleID"),
    USERNAME("username"),
    BONUSTYPE("bonusType"),
    CURRENTACTIVITYID("currentActivityID"),
    DELAYPROJECT("delayProject"),
    COSTPROJECT("costProject"),
    FAILUREPROJECT("failureProject"),
    ACTIVITIES("activities");

    public final String key;

    GameResponseKey(String key) {
        this.key = key;
    }
}
