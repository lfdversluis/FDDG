package nl.tud.gameobjects;

/**
 * Created by martijndevos on 3/11/15.
 */
public class MoveAction extends Action {

    private int x, y;

    public MoveAction(int senderId, int x, int y) {
        this.senderId = senderId;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
