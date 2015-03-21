package nl.tud;

import nl.tud.client.ClientInterface;
import nl.tud.entities.Player;
import nl.tud.gameobjects.*;
import nl.tud.gui.VisualizerGUI;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerProcess extends UnicastRemoteObject implements ServerInterface, Runnable {

    private final int ID, NUM_SERVERS;
    private Field field;
    private Logger logger;
    private volatile Map<Integer, ClientInterface> connectedPlayers;
    private VisualizerGUI visualizerGUI;

    public ServerProcess(int id, int num_servers) throws RemoteException, AlreadyBoundException, MalformedURLException {
        this.ID = id;
        this.NUM_SERVERS = num_servers;
        this.field = new Field();
        this.logger = Logger.getLogger(ServerProcess.class.getName());
        this.connectedPlayers = new ConcurrentHashMap<>();
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
        for(Map.Entry<Integer, ClientInterface> entry : connectedPlayers.entrySet()) {
            try {
                ClientInterface client = entry.getValue();
                client.updateField(field);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void performAction(Action action) throws java.rmi.RemoteException  {
        if(action instanceof MoveAction) {
            MoveAction ma = (MoveAction) action;
            move(action.getSenderId(), ma.getX(), ma.getY());
        } else if(action instanceof HealAction) {
            HealAction ha = (HealAction) action;
            heal(action.getSenderId(), ha.getTargetPlayer());
        } else if(action instanceof AttackAction) {
            AttackAction aa = (AttackAction) action;
            attack(action.getSenderId(), aa.getDragonId());
        }
    }

    public void move(int playerId, int x, int y) throws RemoteException {
        logger.log(Level.INFO, "Server " + this.ID + " received move to (" + x + ", " + y + ") from player " + playerId);
        if(!isValidPlayerId(playerId)) {
            // TODO send error message
        }

        boolean result = field.movePlayer(playerId, x, y);
        if(!result) {
            // TODO send error message
        } else {
            broadcastFieldToConnectedPlayers();
        }

        visualizerGUI.updateGUI();
    }

    public void heal(int playerId, int targetPlayer) throws RemoteException {
        logger.log(Level.INFO, "Server " + this.ID + " received heal to player " + targetPlayer + " from player " + playerId);

        if(!field.isInRange(playerId, targetPlayer, 5) || field.getPlayer(targetPlayer).getHitPointsPercentage() >= 0.5) {
            // TODO send error message
        } else {
            Player thisPlayer = field.getPlayer(playerId);
            field.getPlayer(targetPlayer).heal(thisPlayer.getAttackPower());
            broadcastFieldToConnectedPlayers();
        }

        visualizerGUI.updateGUI();
    }

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