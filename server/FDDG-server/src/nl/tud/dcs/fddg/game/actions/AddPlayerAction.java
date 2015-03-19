package nl.tud.dcs.fddg.game.actions;

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