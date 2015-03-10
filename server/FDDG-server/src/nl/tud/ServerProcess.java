package nl.tud;

import nl.tud.client.ClientInterface;
import nl.tud.entities.Dragon;
import nl.tud.entities.Player;
import nl.tud.entities.Unit;
import nl.tud.gameobjects.Field;
import nl.tud.gui.VisualizerGUI;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerProcess extends UnicastRemoteObject implements ServerInterface, Runnable {

    private final int ID, NUM_SERVERS;
    private Field field;
    private Logger logger;
    private Map<Integer, ClientInterface> connectedPlayers;
    private VisualizerGUI visualizerGUI;

    public ServerProcess(int id, int num_servers) throws RemoteException, AlreadyBoundException, MalformedURLException {
        this.ID = id;
        this.NUM_SERVERS = num_servers;
        this.field = new Field();
        this.logger = Logger.getLogger(ServerProcess.class.getName());
        this.connectedPlayers = new HashMap<>();
        this.visualizerGUI = new VisualizerGUI(field);

        logger.log(Level.INFO, "Starting server with id " + id);

        java.rmi.Naming.bind("rmi://localhost:" + Main.SERVER_PORT + "/FDDGServer/" + id, this);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(5000);
            while (!field.gameHasFinished()) {
                field.dragonRage();
                Thread.sleep(1000);
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

    }

    public boolean isValidPlayerId(int playerId) {
        return field.isValidPlayerId(playerId);
    }

    public synchronized void broadcastFieldToConnectedPlayers() {
        Iterator<Integer> it = connectedPlayers.keySet().iterator();
        while(it.hasNext()) {
            Integer id = it.next();
            ClientInterface client = connectedPlayers.get(id);
            try {
                client.updateField(field);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void move(int playerId, int direction) throws RemoteException {
        logger.log(Level.INFO, "Server " + this.ID + " received move with direction " + direction + " from player " + playerId);
        if(!isValidPlayerId(playerId)) {
            // TODO send error message
        } else if(direction < 0 || direction > 3) {
            // TODO send error message
        }

        boolean result = field.movePlayer(playerId, direction);
        if(!result) {
            // TODO send error message
        } else {
            broadcastFieldToConnectedPlayers();
        }

        visualizerGUI.updateGUI();
    }

    @Override
    public synchronized void heal(int playerId, int targetPlayer) throws RemoteException {
        logger.log(Level.INFO, "Server " + this.ID + " received heal to player " + targetPlayer + " from player " + playerId);

        if(!field.isInRange(playerId, targetPlayer, 5)) {
            // TODO send error message
        } else {
            Player thisPlayer = field.getPlayer(playerId);
            field.getPlayer(targetPlayer).heal(thisPlayer.getAttackPower());
            broadcastFieldToConnectedPlayers();
        }

        visualizerGUI.updateGUI();
    }

    @Override
    public void attack(int playerId, int dragonId) throws RemoteException {
        logger.log(Level.INFO, "Server " + this.ID + " received attack to dragon " + dragonId + " from player " + playerId);

        if(!field.isInRange(playerId, dragonId, 1)) {
            // TODO send error message
        } else {
            Player thisPlayer = field.getPlayer(playerId);
            field.getDragon(dragonId).getHit(thisPlayer.getAttackPower());
            if(field.getDragon(dragonId).getCurHitPoints() <= 0) {
                field.removeDragon(dragonId);
            }
            broadcastFieldToConnectedPlayers();
        }

        visualizerGUI.updateGUI();
    }

    @Override
    public void connect(int playerId) throws RemoteException {

        logger.log(Level.INFO, "Client with id " + playerId + " connected");

        try {
            ClientInterface ci = (ClientInterface) Naming.lookup("rmi://localhost:" + Main.SERVER_PORT + "/FDDGClient/" + playerId);
            connectedPlayers.put(playerId, ci);
            field.addPlayer(playerId);
            broadcastFieldToConnectedPlayers();

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        visualizerGUI.updateGUI();
    }

    @Override
    public void heartBeat(int remoteId) throws RemoteException {

    }

    @Override
    public void pong() throws RemoteException {

    }
}
