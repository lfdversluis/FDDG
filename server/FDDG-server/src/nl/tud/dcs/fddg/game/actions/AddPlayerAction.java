package nl.tud.dcs.fddg.game.actions;

import nl.tud.dcs.fddg.game.Field;

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

    /**
     * Method that all subclasses need to implement.
     * It performs the action on the field that is passed as parameter.
     *
     * @param field The field on which the action needs to be performed
     */
    @Override
    public void perform(Field field) {
        field.addPlayer(playerId, x, y);
    }
}