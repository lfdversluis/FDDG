package nl.tud.dcs.fddg.game.actions;

import nl.tud.dcs.fddg.game.Field;
import nl.tud.dcs.fddg.game.entities.Dragon;
import nl.tud.dcs.fddg.game.entities.Player;

public class AttackAction extends Action {
    private int dragonId;

    /**
     * Constructor of the Attack action.
     *
     * @param senderId The ID of the unit that created this action.
     * @param dragonId The id of the dragon that is being attacked.
     */
    public AttackAction(int senderId, int dragonId) {
        this.senderId = senderId;
        this.dragonId = dragonId;
    }

    /**
     * Simple getter for the dragon ID.
     *
     * @return The dragon ID of the dragon being attacked.
     */
    public int getDragonId() {
        return dragonId;
    }

    /**
     * Method that all subclasses need to implement.
     * It performs the action on the field that is passed as parameter.
     * In this case, the dragon is attacked
     *
     * @param field The field on which the action needs to be performed
     */
    @Override
    public void perform(Field field) {
        Dragon dragon = field.getDragon(dragonId);
        Player player = field.getPlayer(senderId);
        if(player != null && dragon != null) {
            dragon.getHit(player.getAttackPower());
        }
    }

    /**
     * Checks whether this action is valid in the current field.
     * Here, is checks whether the player is next (range <=2) to the dragon it wants to attack.
     *
     * @param field The current field
     * @return true iff the action is valid
     */
    @Override
    public boolean isValid(Field field) {
        return field.isInRange(senderId, dragonId, 2);
    }
}
