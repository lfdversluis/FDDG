package nl.tud.entities;

import java.io.Serializable;

/**
 * Created by martijndevos on 3/4/15.
 */
public class Unit implements Serializable {
    protected int maxHitPoints, attackPower, curHitPoints;
    protected int xPos, yPos;
    protected int unitId;

    public int getHitpoints() {
        return maxHitPoints;
    }

    public void setHitpoints(int hitpoints) {
        this.maxHitPoints = hitpoints;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public void setAttackPower(int attackPower) {
        this.attackPower = attackPower;
    }

    public int getxPos() {
        return xPos;
    }

    public void setxPos(int xPos) {
        this.xPos = xPos;
    }

    public int getyPos() {
        return yPos;
    }

    public void setyPos(int yPos) {
        this.yPos = yPos;
    }

    public int getCurHitPoints() {
        return curHitPoints;
    }

    public void setCurHitPoints(int curHitPoints) {
        this.curHitPoints = curHitPoints;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }
}
