package nl.tud.dcs.fddg.client;

import nl.tud.dcs.fddg.game.Field;

import java.rmi.Remote;

/**
 * Created by Martijn on 09-03-15.
 */
public interface ClientInterface extends Remote {

    /**
     * Function to notify a client of an update to the game state
     *
     * @param field The new game field
     * @throws java.rmi.RemoteException
     */
    public void updateField(Field field) throws java.rmi.RemoteException;

    public void receiveError(int errorId, String message) throws java.rmi.RemoteException;

    /**
     * Function for the heartbeat mechanism from server to client
     *
     * @throws java.rmi.RemoteException
     */
    public void ping() throws java.rmi.RemoteException;
}
