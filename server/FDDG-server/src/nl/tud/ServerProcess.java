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
import java.util.Set;
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
                Set<Player> deadPlayerSet = field.dragonRage();

                for(Player p : deadPlayerSet){
                    DeleteUnitAction dua = new DeleteUnitAction(p.getUnitId());
                    broadcastActionToPlayers(dua);
                }
                Thread.sleep(1000);
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

    }

    public boolean isValidPlayerId(int playerId) {
        return field.isValidPlayerId(playerId);
    }

    public synchronized void broadcastActionToPlayers(Action action) {
        for(Map.Entry<Integer, ClientInterface> entry : connectedPlayers.entrySet()) {
            try {
                ClientInterface client = entry.getValue();
                client.ack(action);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void performAction(Action action) throws java.rmi.RemoteException  {
        if(action instanceof MoveAction) {
            MoveAction ma = (MoveAction) action;
            move(ma);
        } else if(action instanceof HealAction) {
            HealAction ha = (HealAction) action;
            heal(ha);
        } else if(action instanceof AttackAction) {
            AttackAction aa = (AttackAction) action;
            attack(aa);
        }
    }

    public void move(MoveAction ma) throws RemoteException {
        int playerId = ma.getSenderId();
        int x = ma.getX();
        int y = ma.getY();

        logger.log(Level.INFO, "Server " + this.ID + " received move to (" + x + ", " + y + ") from player " + playerId);
        if(!isValidPlayerId(playerId)) {
            // TODO send error message
        }

        boolean result = field.movePlayer(playerId, x, y);
        if(!result) {
            // TODO send error message
        } else {
            broadcastActionToPlayers(ma);
        }

        visualizerGUI.updateGUI();
    }

    public void heal(HealAction ha) throws RemoteException {
        int playerId = ha.getSenderId();
        int targetPlayer = ha.getTargetPlayer();

        logger.log(Level.INFO, "Server " + this.ID + " received heal to player " + targetPlayer + " from player " + playerId);

        if(!field.isInRange(playerId, targetPlayer, 5) || field.getPlayer(targetPlayer).getHitPointsPercentage() >= 0.5) {
            // TODO send error message
        } else {
            Player thisPlayer = field.getPlayer(playerId);
            field.getPlayer(targetPlayer).heal(thisPlayer.getAttackPower());
            broadcastActionToPlayers(ha);
        }

        visualizerGUI.updateGUI();
    }

    public void attack(AttackAction aa) throws RemoteException {
        int playerId = aa.getSenderId();
        int dragonId = aa.getDragonId();

        logger.log(Level.INFO, "Server " + this.ID + " received attack to dragon " + dragonId + " from player " + playerId);

        if(!field.isInRange(playerId, dragonId, 1)) {
            // TODO send error message
        } else {
            Player thisPlayer = field.getPlayer(playerId);
            field.getDragon(dragonId).getHit(thisPlayer.getAttackPower());
            if(field.getDragon(dragonId).getCurHitPoints() <= 0) {
                field.removeDragon(dragonId);
                DeleteUnitAction dua = new DeleteUnitAction(dragonId);
                broadcastActionToPlayers(dua);
            } else {
                broadcastActionToPlayers(aa);
            }
        }

        visualizerGUI.updateGUI();
    }

    @Override
    public void connect(int playerId) throws RemoteException {

        logger.log(Level.INFO, "Client with id " + playerId + " connected");

        try {
            ClientInterface ci = (ClientInterface) Naming.lookup("rmi://localhost:" + Main.SERVER_PORT + "/FDDGClient/" + playerId);
            field.addPlayer(playerId);
            ci.initializeField(field);

            AddPlayerAction apa = new AddPlayerAction(playerId, field.getPlayer(playerId).getxPos(), field.getPlayer(playerId).getyPos());
            broadcastActionToPlayers(apa);

            // Now the broadcast is done, add the player to the player map (so he doesn't add himself again on the field).
            connectedPlayers.put(playerId, ci);

        } catch (NotBoundException | MalformedURLException e) {
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
