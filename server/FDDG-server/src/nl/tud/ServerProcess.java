package nl.tud;

import nl.tud.gameobjects.Field;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by Martijn on 09-03-15.
 */
public class ServerProcess extends UnicastRemoteObject implements ServerInterface, Runnable {

    private final int ID, NUM_SERVERS;
    private Field field;

    public ServerProcess(int id, int num_servers) throws RemoteException, AlreadyBoundException, MalformedURLException {
        this.ID = id;
        this.NUM_SERVERS = num_servers;
        this.field = new Field();

        System.out.println("Starting server " + id + ".");

        java.rmi.Naming.bind("rmi://localhost:" + Main.SERVER_PORT + "/FDDGServer" + id, this);
    }

    @Override
    public void run() {
        while (true) {
            // TODO implementation here
        }
    }

    public boolean isValidPlayerId(int playerId) {
        return field.isValidPlayerId(playerId);
    }

    @Override
    public void move(int playerId, int direction) throws RemoteException {
        if(!isValidPlayerId(playerId)) {
            // TODO send error message
        } else if(direction < 0 || direction > 3) {
            // TODO send error message
        }

        boolean result = field.movePlayer(playerId, direction);
        if(!result) {
            // TODO send error message
        } else {
            // TODO send updated field
        }
    }

    @Override
    public void heal(int playerId, int targetPlayer) throws RemoteException {

    }

    @Override
    public void attack(int playerId, int dragonId) throws RemoteException {

    }

    @Override
    public void connect(int playerId) throws RemoteException {

    }

    @Override
    public void sendField(int remoteId, Field field) throws RemoteException {

    }

    @Override
    public void sendError(int remoteId, int errorId, String message) throws RemoteException {

    }

    @Override
    public void heartBeat(int remoteId) throws RemoteException {

    }
}
