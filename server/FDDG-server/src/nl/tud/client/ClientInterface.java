package nl.tud.client;

import nl.tud.gameobjects.Field;

import java.rmi.Remote;

public interface ClientInterface extends Remote {

    public void updateField(Field field) throws java.rmi.RemoteException;

    public void receiveError(int errorId, String message) throws java.rmi.RemoteException;

    public void ping() throws java.rmi.RemoteException;
}
