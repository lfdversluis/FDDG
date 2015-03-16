package nl.tud.dcs.fddg.game.entities;

import java.util.Random;

public class Dragon extends Unit {

    public Dragon(int x, int y, int id) {
        Random random = new Random(System.currentTimeMillis());
        maxHitPoints = random.nextInt(51) + 50;
        curHitPoints = maxHitPoints;

        attackPower = random.nextInt(16) + 5;

        this.xPos = x; this.yPos = y;
        this.unitId = id;
    }

    public void getHit(int ap) {
        curHitPoints -= ap;
    }
}