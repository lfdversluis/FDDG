package nl.tud.dcs.fddg.game.actions;

import nl.tud.dcs.fddg.game.Field;

/**
 * Created by Niels on 23-3-2015.
 */
public class EndOfGameAction extends Action {

    /**
     * Constructor
     *
     * @param serverID The id of the server that ended the game
     */
    public EndOfGameAction(int serverID) {
        this.senderId = serverID;
    }

    /**
     * Method that all subclasses need to implement.
     * It performs the action on the field that is passed as parameter.
     *
     * @param field The field on which the action needs to be performed
     */
    @Override
    public void perform(Field field) {
        // do nothing here...
    }

    /**
     * Checks whether this action is valid in the current field.
     * Always just execute this action without checking any conditions
     *
     * @param field The current field
     * @return true iff the action is valid
     */
    @Override
    public boolean isValid(Field field) {
        return true;
    }
}
