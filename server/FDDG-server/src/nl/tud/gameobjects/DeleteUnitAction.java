package nl.tud.gameobjects;

public class DeleteUnitAction extends Action {

    private int unitId;

    public DeleteUnitAction(int unitId) {
        this.unitId = unitId;
    }

    public int getUnitId() {
        return unitId;
    }
}
