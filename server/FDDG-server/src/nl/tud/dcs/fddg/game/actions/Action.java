package nl.tud.dcs.fddg.game.actions;

import java.io.Serializable;

public abstract class Action implements Serializable {
    protected int senderId;

    /**
     * Simple getter for the sender ID of the action.
     * @return The ID of the sender that created the action.
     */
    public int getSenderId() {
        return senderId;
    }
}
