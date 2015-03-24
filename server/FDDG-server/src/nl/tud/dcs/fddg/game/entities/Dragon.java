package nl.tud.dcs.fddg.game.entities;

import java.util.Random;

public class Dragon extends Unit {

    /**
     * Constructor of the Dragon class. This initializes a new Dragon object
     * and sets the basic parameters: x, y and id.
     *
     * @param x  The initial x coordinate of the dragon on the field.
     * @param y  The initial y coordinate of the dragon on the field.
     * @param id The (unique) ID of the Dragon.
     */
    public Dragon(int x, int y, int id) {
        Random random = new Random(1337); // we need a hard coded ID here to let the dragons be consistent on all servers
        this.maxHitPoints = random.nextInt(51) + 50;
        this.curHitPoints = maxHitPoints;

        this.attackPower = random.nextInt(16) + 50;

        this.xPos = x;
        this.yPos = y;
        this.unitId = id;
    }

    /**
     * This function decreases the dragon's current hit points.
     *
     * @param ap The attack power of the hit that hits the dragon.
     */
    public void getHit(int ap) {
        curHitPoints -= ap;
    }
}
