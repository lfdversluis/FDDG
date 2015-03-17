package nl.tud.client;

import nl.tud.gameobjects.Field;

import java.rmi.Remote;

public interface ClientInterface extends Remote {

    /**
     * DEPRECATED.
     * This function can be called by the server to update the field of a player.
     * @param field The field that the player should update to.
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
