package nl.tud.gameobjects;

/**
 * Created by martijndevos on 3/11/15.
 */
public class HealAction extends Action {
    private int targetPlayer;

    public HealAction(int senderId, int targetPlayer) {
        this.senderId = senderId;
        this.targetPlayer = targetPlayer;
    }

    public int getTargetPlayer() {
        return targetPlayer;
    }
}
