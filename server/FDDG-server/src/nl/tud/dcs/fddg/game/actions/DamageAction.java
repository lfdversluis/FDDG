package nl.tud.dcs.fddg.game.actions;

import nl.tud.dcs.fddg.game.Field;

public class DamageAction extends Action {
    private int playerId;
    private int damage;

    public DamageAction(int playerId, int damage) {
        this.playerId = playerId;
        this.damage = damage;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    /**
     * Method that all subclasses need to implement.
     * It performs the action on the field that is passed as parameter.
     *
     * @param field The field on which the action needs to be performed
     */
    @Override
    public void perform(Field field) {
        field.getPlayer(playerId).setCurHitPoints(field.getPlayer(playerId).getCurHitPoints() - damage);
    }
}