package nl.tud.gameobjects;

/**
 * Created by Martijn on 13-03-15.
 */
public class DamageAction extends Action {
    private int playerId;
    private int damage;

    public DamageAction(int playerId, int damage) {
        this.playerId = playerId;
        this.damage = damage;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
}
