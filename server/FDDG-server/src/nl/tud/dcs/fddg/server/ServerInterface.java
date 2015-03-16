package nl.tud.dcs.fddg.server;

import nl.tud.dcs.fddg.game.actions.Action;

import java.rmi.Remote;

public interface ServerInterface extends Remote {

    public void performAction(Action action) throws java.rmi.RemoteException;

    public void connect(int playerId) throws java.rmi.RemoteException;

    public void heartBeat(int remoteId) throws java.rmi.RemoteException;

    public void pong() throws java.rmi.RemoteException;
}
