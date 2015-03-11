package nl.tud.client;

import nl.tud.gameobjects.Action;
import nl.tud.gameobjects.Field;

import java.rmi.Remote;

/**
 * Created by Martijn on 09-03-15.
 */
public interface ClientInterface extends Remote {

    public void initializeField(Field field) throws java.rmi.RemoteException;

    public void ack(Action action) throws java.rmi.RemoteException;

    public void receiveError(int errorId, String message) throws java.rmi.RemoteException;

    public void ping() throws java.rmi.RemoteException;
}
