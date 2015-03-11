package nl.tud.gameobjects;

/**
 * Created by Laurens on 11-3-2015.
 */
public class AddPlayerAction extends Action {

    private int playerId, x, y;

    public AddPlayerAction(int playerId, int x, int y) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getPlayerId() {
        return playerId;
    }
}
