package nl.tud.gameobjects;

/**
 * Created by martijndevos on 3/11/15.
 */
public class AttackAction extends Action {
    private int dragonId;

    public AttackAction(int senderId, int dragonId) {
        this.senderId = senderId;
        this.dragonId = dragonId;
    }

    public int getDragonId() {
        return dragonId;
    }
}
