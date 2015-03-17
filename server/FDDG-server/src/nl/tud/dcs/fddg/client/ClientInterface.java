package nl.tud.dcs.fddg.client;

import nl.tud.dcs.fddg.game.Field;

import java.rmi.Remote;

/**
 * Created by Martijn on 09-03-15.
 */
public interface ClientInterface extends Remote {

    /**
     * Function to notify a client of an update to the game state.
     *
     * @param field The new game field
     * @throws java.rmi.RemoteException
     */
    public void updateField(Field field) throws java.rmi.RemoteException;

    /**
     * This function can be called by the server to let the client know something went wrong.
     * @param errorId The ID of the error that occurred.
     * @param message The message that comes with the error.
     * @throws java.rmi.RemoteException
     */
    public void receiveError(int errorId, String message) throws java.rmi.RemoteException;

    /**
     * The server can call this function to check if the client is still connected to the server.
     * The client will have to call the {@Link ServerProcess@pong} message to acknowledge this heartbeat.
     * @throws java.rmi.RemoteException
     */
    public void ping() throws java.rmi.RemoteException;
}
