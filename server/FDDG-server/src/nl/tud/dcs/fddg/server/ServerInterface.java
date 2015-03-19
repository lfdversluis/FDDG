package nl.tud.dcs.fddg.server;

import nl.tud.dcs.fddg.game.actions.Action;
import nl.tud.dcs.fddg.game.actions.ClientConnectAction;

import java.rmi.Remote;

public interface ServerInterface extends Remote {

    /**
     * A client can call this function to let the server know it wishes to perform a certain action.
     * @param action
     * @throws java.rmi.RemoteException
     */
    public void performAction(Action action) throws java.rmi.RemoteException;

    public ClientConnectAction register() throws java.rmi.RemoteException;

    /**
     * The client calls this function if it wishes to connect to the server and play the game.
     * @param playerId
     * @throws java.rmi.RemoteException
     */
    public void connect(int playerId) throws java.rmi.RemoteException;

    /**
     * This function can be called by other servers to check if this server is still functional.
     * This server will send a heartbeat back to acknowledge it is still alive.
     * @param remoteId The (unique) id of the machine that issues the heartbeat.
     * @throws java.rmi.RemoteException
     */
    public void heartBeat(int remoteId) throws java.rmi.RemoteException;

    /**
     * This message can be called by a client to let the server know it is still connected.
     * @throws java.rmi.RemoteException
     */
    public void pong() throws java.rmi.RemoteException;
}
