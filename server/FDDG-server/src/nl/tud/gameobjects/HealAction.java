package nl.tud.gameobjects;

public class HealAction extends Action {
    private int targetPlayer;

    /**
     * Constructor of the Heal action.
     * @param senderId The ID of the unit that created this action.
     * @param targetPlayer The id of the player that will receive the heal.
     */
    public HealAction(int senderId, int targetPlayer) {
        this.senderId = senderId;
        this.targetPlayer = targetPlayer;
    }

    /**
     * A simple getter that returns the target player ID.
     * @return The ID of the player to be healed.
     */
    public int getTargetPlayer() {
        return targetPlayer;
    }
}
