package nl.tud.dcs.fddg.game.actions;

/**
 * Created by Laurens on 19-3-2015.
 */
public class ClientConnectAction extends Action {

    private int connectionId;

    /**
     * Constructor of the ClientConnectAction
     * @param connectionId The ID the unit receives by the server.
     */
    public  ClientConnectAction(int connectionId) {
        this.connectionId = connectionId;
    }

    /**
     * A simple getter that returns the new ID for the client.
     * @return The ID assigned to the new client.
     */
    public int getConnectionId() {
        return connectionId;
    }
}
