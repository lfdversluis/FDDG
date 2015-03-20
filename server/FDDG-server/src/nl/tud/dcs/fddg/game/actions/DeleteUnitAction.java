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
            field.removePlayer(p.getUnitId());
        } else {
            Dragon d = field.getDragon(unitId);
            field.removeDragon(d.getUnitId());
        }
    }
}