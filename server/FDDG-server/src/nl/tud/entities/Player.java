package nl.tud.entities;

import java.util.Random;

/**
 * Created by martijndevos on 3/4/15.
 */
public class Player extends Unit {

    public Player(int x, int y, int id) {
        Random random = new Random(System.currentTimeMillis());
        maxHitPoints = random.nextInt(11) + 10;
        curHitPoints = maxHitPoints;

        attackPower = random.nextInt(10) + 1;

        this.xPos = x; this.yPos = y;
        this.unitId = id;
    }
}
