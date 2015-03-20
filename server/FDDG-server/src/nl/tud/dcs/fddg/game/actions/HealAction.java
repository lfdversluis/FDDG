package nl.tud.dcs.fddg.game.actions;

import nl.tud.dcs.fddg.game.Field;
import nl.tud.dcs.fddg.game.entities.Player;

public class HealAction extends Action {
    private int targetPlayer;

    /**
     * Constructor of the Heal action.
     *
     * @param senderId     The ID of the unit that created this action.
     * @param targetPlayer The id of the player that will receive the heal.
     */
    public HealAction(int senderId, int targetPlayer) {
        this.senderId = senderId;
        this.targetPlayer = targetPlayer;
    }

    /**
     * A simple getter that returns the target player ID.
     *
     * @return The ID of the player to be healed.
     */
    public int getTargetPlayer() {
        return targetPlayer;
    }

    /**
     * Method that all subclasses need to implement.
     * It performs the action on the field that is passed as parameter.
     *
     * @param field The field on which the action needs to be performed
     */
    @Override
    public void perform(Field field) {
        Player thisPlayer = field.getPlayer(senderId);
        field.getPlayer(targetPlayer).heal(thisPlayer.getAttackPower());
    }
}
