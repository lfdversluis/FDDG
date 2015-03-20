package nl.tud.dcs.fddg.game.actions;

import nl.tud.dcs.fddg.game.Field;

import java.io.Serializable;

public abstract class Action implements Serializable {
    protected int senderId;

    /**
     * Simple getter for the sender ID of the action.
     *
     * @return The ID of the sender that created the action.
     */
    public int getSenderId() {
        return senderId;
    }

    /**
     * Method that all subclasses need to implement.
     * It performs the action on the field that is passed as parameter.
     *
     * @param field The field on which the action needs to be performed
     */
    public abstract void perform(Field field);
}
