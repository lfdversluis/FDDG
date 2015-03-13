package nl.tud.entities;

import java.util.Random;

public class Dragon extends Unit {

    public Dragon(int x, int y, int id) {
        Random random = new Random(System.currentTimeMillis());
        maxHitPoints = random.nextInt(51) + 50000;
        curHitPoints = maxHitPoints;

        attackPower = random.nextInt(16) + 5343;

        this.xPos = x; this.yPos = y;
        this.unitId = id;
    }

    public void getHit(int ap) {
        curHitPoints -= ap;
    }

    public String toString() {
        return "{ Dragon: " + this.unitId + " }";
    }
}
