package nl.tud.dcs.fddg.game.actions;

import java.io.Serializable;

/**
 * Created by martijndevos on 3/11/15.
 */
public abstract class Action implements Serializable {
    protected int senderId;

    public int getSenderId() {
        return senderId;
    }
}