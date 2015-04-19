package nl.tud.dcs.fddg.client;

import nl.tud.dcs.fddg.game.Field;
import nl.tud.dcs.fddg.game.actions.Action;

import java.rmi.Remote;

/**
 * Created by Martijn on 09-03-15.
 */
public interface ClientInterface extends Remote {

    /**
     * Function to give the initial field to a new connected client.
     *
     * @param field The new game field
     * @throws java.rmi.RemoteException
     */
    public void initializeField(Field field) throws java.rmi.RemoteException;

    /**
     * This method is used to send actions to the clients.
     *
     * @param action the action to be performed.
     * @throws java.rmi.RemoteException
     */
    public void performAction(Action action) throws java.rmi.RemoteException;

    /**
     * This function can be called by the server to let the client know something went wrong.
     *
     * @param errorId The ID of the error that occurred.
     * @param message The message that comes with the error.
     * @throws java.rmi.RemoteException
     */
    public void receiveError(int errorId, String message) throws java.rmi.RemoteException;

    /**
     * The server can call this function to check if the client is still connected to the server.
     * The client will have to call the {@Link ServerProcess@pong} message to acknowledge this heartbeat.
     *
     * @throws java.rmi.RemoteException
     */
    public void ping() throws java.rmi.RemoteException;
}
