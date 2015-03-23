package nl.tud.dcs.fddg.server;

import nl.tud.dcs.fddg.game.actions.Action;

import java.rmi.Remote;

/**
 * The RMI interface between the clients and the server.
 * It contains all methods that the clients can call on the server.
 */
public interface ClientServerInterface extends Remote {

    /**
     * A client can call this function to let the server know it wishes to perform a certain action.
     *
     * @param action The action the clients wants to perform
     * @throws java.rmi.RemoteException
     */
    public void requestAction(Action action) throws java.rmi.RemoteException;

    /**
     * This function is called by clients to tell the server they wish to register.
     * The server then replies with sending an identifier that the client should use.
     *
     * @return The ID of the client.
     * @throws java.rmi.RemoteException
     */
    public int register() throws java.rmi.RemoteException;

    /**
     * The client calls this function if it wishes to connect to the server and play the game.
     *
     * @param clientId The ID of the client that wants to connect
     * @throws java.rmi.RemoteException
     */
    public void connect(int clientId) throws java.rmi.RemoteException;

    /**
     * This function can be called by other servers to check if this server is still functional.
     * This server will send a heartbeat back to acknowledge it is still alive.
     *
     * @param remoteId The (unique) id of the machine that issues the heartbeat.
     * @throws java.rmi.RemoteException
     */
    public void heartBeat(int remoteId) throws java.rmi.RemoteException;

    /**
     * This message can be called by a client to let the server know it is still connected.
     *
     * @throws java.rmi.RemoteException
     */
    public void pong() throws java.rmi.RemoteException;
}
