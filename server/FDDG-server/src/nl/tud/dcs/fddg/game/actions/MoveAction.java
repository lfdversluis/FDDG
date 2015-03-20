package nl.tud.dcs.fddg.game.actions;


import nl.tud.dcs.fddg.game.Field;

public class MoveAction extends Action {

    private int x, y;

    /**
     * Constructor of the Move action.
     *
     * @param senderId The ID of the unit that created this action.
     * @param x        The x coordinate of the location to move to.
     * @param y        The y coordinate of the location to move to.
     */
    public MoveAction(int senderId, int x, int y) {
        this.senderId = senderId;
        this.x = x;
        this.y = y;
    }

    /**
     * A simple getter that returns the x coordinate of the move action.
     *
     * @return The x coordinate of the new location.
     */
    public int getX() {
        return x;
    }

    /**
     * A simple getter that returns the y coordinate of the move action.
     *
     * @return The y coordinate of the new location.
     */
    public int getY() {
        return y;
    }

    /**
     * Method that all subclasses need to implement.
     * It performs the action on the field that is passed as parameter.
     *
     * @param field The field on which the action needs to be performed
     */
    @Override
    public void perform(Field field) {
        field.movePlayer(senderId, x, y);
    }
}
