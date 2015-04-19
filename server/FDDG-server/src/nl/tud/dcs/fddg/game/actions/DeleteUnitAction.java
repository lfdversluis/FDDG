package nl.tud.dcs.fddg.game.actions;

import nl.tud.dcs.fddg.game.Field;
import nl.tud.dcs.fddg.game.entities.Dragon;
import nl.tud.dcs.fddg.game.entities.Player;

public class DeleteUnitAction extends Action {

    private int unitId;

    public DeleteUnitAction(int unitId) {
        this.unitId = unitId;
    }

    public int getUnitId() {
        return unitId;
    }

    /**
     * Method that all subclasses need to implement.
     * It performs the action on the field that is passed as parameter.
     *
     * @param field The field on which the action needs to be performed
     */
    @Override
    public void perform(Field field) {
        if (field.getDragon(unitId) == null) {
            Player p = field.getPlayer(unitId);
            if(p != null) {
                field.removePlayer(p.getUnitId());
            }
        } else {
            Dragon d = field.getDragon(unitId);
            field.removeDragon(d.getUnitId());
        }
    }

    /**
     * Checks whether this action is valid in the current field.
     * Here, it checks whether the unit is actually in the game
     *
     * @param field The current field
     * @return true iff the action is valid
     */
    @Override
    public boolean isValid(Field field) {
        return field.isValidPlayerId(unitId) || (field.getDragon(unitId) != null);
    }
}