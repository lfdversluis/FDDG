package nl.tud.dcs.fddg.game.actions;

public class DeleteUnitAction extends Action {

    private int unitId;

    public DeleteUnitAction(int unitId) {
        this.unitId = unitId;
    }

    public int getUnitId() {
        return unitId;
    }
}