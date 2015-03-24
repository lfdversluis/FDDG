package nl.tud.dcs.fddg.game.actions;

import nl.tud.dcs.fddg.game.Field;
import nl.tud.dcs.fddg.game.entities.Player;

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
        try {
            Player p = field.getPlayer(playerId);
            if(p != null) {
                p.setCurHitPoints(p.getCurHitPoints() - damage);
            }

        } catch(Exception e) {
            System.out.println("ERRORERRORERROR ------" + field + ", " + playerId + ", " + field.getPlayer(playerId));
            e.printStackTrace();
        }

    }

    /**
     * Checks whether this action is valid in the current field.
     * In this case, the action is valid if the player exists.
     *
     * @param field The current field
     * @return true iff the action is valid
     */
    @Override
    public boolean isValid(Field field) {
        return field.isValidPlayerId(playerId);
    }
}