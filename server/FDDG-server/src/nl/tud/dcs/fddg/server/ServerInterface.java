package nl.tud.dcs.fddg.server;

import nl.tud.dcs.fddg.game.Field;
import nl.tud.dcs.fddg.game.actions.Action;
import nl.tud.dcs.fddg.server.requests.ActionRequest;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The RMI interface between the different servers.
 */
public interface ServerInterface extends Remote {

    /**
     * Method that one server calls on all other servers to request a certain action.
     *
     * @param request A request containing the id and the action the server wants to perform
     * @throws RemoteException
     */
    public void requestAction(ActionRequest request) throws RemoteException;

    /**
     * Method that is used to acknowledge a request send by another server
     *
     * @param requestID The id of the request
     * @throws RemoteException
     */
    public void acknowledgeRequest(int requestID) throws RemoteException;

    /**
     * Method that indicates that the server should perform a certain action.
     * This method is only invoked when all other servers have acknowledged the request for the action.
     *
     * @param action The action that should be executed
     * @throws RemoteException
     */
    public void performAction(Action action) throws RemoteException;

    /**
     * This function sends a heartbeat to a remote machine, identified by its (unique) ID.
     *
     * @param remoteId The ID of the remote machine.
     * @throws RemoteException
     */
    public void heartBeat(int remoteId) throws RemoteException;

    /**
     * Sends the entire field upon request to a peer. This function is called by
     * a crashed server that is recovering.
     *
     * @return The current Field (state) of the game.
     * @throws RemoteException
     */
    public Field sendField() throws RemoteException;
}
