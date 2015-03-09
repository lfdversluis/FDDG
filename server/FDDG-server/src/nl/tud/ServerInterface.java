package nl.tud;

import nl.tud.gameobjects.Field;

import java.rmi.Remote;

/**
 * Created by Martijn on 09-03-15.
 */
public interface ServerInterface extends Remote {

    public void move(int playerId, int direction) throws java.rmi.RemoteException;

    public void heal(int playerId, int targetPlayer) throws java.rmi.RemoteException;

    public void attack(int playerId, int dragonId) throws java.rmi.RemoteException;

    public void connect(int playerId) throws java.rmi.RemoteException;

    public void sendField(int remoteId, Field field) throws java.rmi.RemoteException;

    public void sendError(int remoteId, int errorId, String message) throws java.rmi.RemoteException;;

    public void heartBeat(int remoteId) throws java.rmi.RemoteException;
}
