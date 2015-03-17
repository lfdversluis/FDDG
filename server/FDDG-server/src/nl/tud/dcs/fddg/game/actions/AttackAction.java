package nl.tud.dcs.fddg.game.actions;

public class AttackAction extends Action {
    private int dragonId;

    /**
     * Constructor of the Attack action.
     * @param senderId The ID of the unit that created this action.
     * @param dragonId The id of the dragon that is being attacked.
     */
    public AttackAction(int senderId, int dragonId) {
        this.senderId = senderId;
        this.dragonId = dragonId;
    }

    /**
     * Simple getter for the dragon ID.
     * @return The dragon ID of the dragon being attacked.
     */
    public int getDragonId() {
        return dragonId;
    }
}
