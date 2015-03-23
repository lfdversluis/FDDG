package nl.tud.dcs.fddg.game.actions;

import nl.tud.dcs.fddg.game.Field;
import nl.tud.dcs.fddg.game.entities.Player;

public class AddPlayerAction extends Action {

    private int playerId, x, y, maxHitPoints, attackPower;

    public AddPlayerAction(int playerId, int x, int y, int maxHitPoints, int attackPower) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.maxHitPoints = maxHitPoints;
        this.attackPower = attackPower;
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
        Player newPlayer = new Player(x, y, playerId, maxHitPoints, attackPower);
        field.addPlayer(newPlayer);
    }

    /**
     * Checks whether this action is valid in the current field.
     * Here, it checks whether the location is free.
     *
     * @param field The current field
     * @return true iff the action is valid
     */
    @Override
    public boolean isValid(Field field) {
        return field.isFree(x, y);
    }
}