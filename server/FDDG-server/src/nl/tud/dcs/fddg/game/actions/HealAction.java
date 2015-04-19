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
     * In this case the targetPlayer will be healed
     *
     * @param field The field on which the action needs to be performed
     */
    @Override
    public void perform(Field field) {
        Player thisPlayer = field.getPlayer(senderId);
        Player thatPlayer = field.getPlayer(targetPlayer);
        if(thisPlayer != null && thatPlayer != null) {
            thatPlayer.heal(thisPlayer.getAttackPower());
        }
    }

    /**
     * Checks whether this action is valid in the current field.
     * Here, it checks whether the player is within a range of 5 of the target player.
     * Also it checks whether the target player actually needs to be healed.
     *
     * @param field The current field
     * @return true iff the action is valid
     */
    @Override
    public boolean isValid(Field field) {
        return field.isInRange(senderId, targetPlayer, 5) &&
                field.getPlayer(targetPlayer).getHitPointsPercentage() < 0.5 &&
                field.getPlayer(targetPlayer).getCurHitPoints() > 0;
    }
}
