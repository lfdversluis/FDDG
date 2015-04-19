package nl.tud.dcs.fddg.game.entities;

import java.util.Random;

public class Player extends Unit {

    /**
     * Constructor of the player class. This initializes a new Player object
     * and sets the basic parameters: x, y and id.
     *
     * @param x  The initial x coordinate of the player on the field.
     * @param y  The initial y coordinate of the player on the field.
     * @param id The (unique) ID of the player.
     */
    public Player(int x, int y, int id) {
        Random random = new Random(System.currentTimeMillis());
        maxHitPoints = random.nextInt(11) + 10;
        curHitPoints = maxHitPoints;

        attackPower = random.nextInt(10) + 1;

        this.xPos = x;
        this.yPos = y;
        this.unitId = id;
    }

    public Player(int x, int y, int id, int maxHitPoints, int attackPower) {
        this.maxHitPoints = maxHitPoints;
        curHitPoints = maxHitPoints;

        this.attackPower = attackPower;

        this.xPos = x;
        this.yPos = y;
        this.unitId = id;
    }

    /**
     * This function is called to heal the player up to its maximal amount
     * of hit points.
     *
     * @param healHp The amount of hit points the user is healed with.
     */
    public void heal(int healHp) {
        curHitPoints = Math.min(maxHitPoints, curHitPoints + healHp);
    }
}
