package nl.tud.dcs.fddg.server;

import nl.tud.dcs.fddg.client.ClientInterface;
import nl.tud.dcs.fddg.game.Field;
import nl.tud.dcs.fddg.game.actions.Action;
import nl.tud.dcs.fddg.game.actions.AttackAction;
import nl.tud.dcs.fddg.game.actions.HealAction;
import nl.tud.dcs.fddg.game.actions.MoveAction;
import nl.tud.dcs.fddg.game.entities.Player;
import nl.tud.dcs.fddg.gui.VisualizerGUI;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerProcess extends UnicastRemoteObject implements ServerInterface, Runnable {

    private final int ID;
    private Field field;
    private Logger logger;
    private volatile Map<Integer, ClientInterface> connectedPlayers;
    private VisualizerGUI visualizerGUI = null;

<<<<<<< HEAD
    /**
     * The constructor of the ServerProcess class. It requires an ID and the number of servers as parameters.
     *
     * @param id The (unique) ID of the server
     * @param num_servers The total amount of servers used in the simulation.
     * @throws RemoteException
     * @throws AlreadyBoundException
     * @throws MalformedURLException
     */
    public ServerProcess(int id, int num_servers) throws RemoteException, AlreadyBoundException, MalformedURLException {
=======
    public ServerProcess(int id, boolean useGUI) throws RemoteException {
        super();
>>>>>>> master
        this.ID = id;
        this.field = new Field();
        this.logger = Logger.getLogger(ServerProcess.class.getName());
        this.connectedPlayers = new ConcurrentHashMap<>();

        // start GUI if necessary
        if (useGUI)
            this.visualizerGUI = new VisualizerGUI(field);

        logger.log(Level.INFO, "Starting server with id " + id);
    }

    /**
     * Main loop of the process.
     * It first waits 5 seconds the start the game, then lets the dragons attack every second.
     */
    @Override
    public void run() {
        try {
            Thread.sleep(5000);
            while (!field.gameHasFinished()) {
                field.dragonRage();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
<<<<<<< HEAD
     * Function to check if a player ID is valid.
     * @param playerId The player ID to check.
     * @return Returns a boolean indicating if the player is valid.
     */
=======
     * Only updates the GUI if there actually is one
     */
    private void checkAndUpdateGUI() {
        if (visualizerGUI != null)
            visualizerGUI.updateGUI();
    }

>>>>>>> master
    public boolean isValidPlayerId(int playerId) {
        return field.isValidPlayerId(playerId);
    }

    /**
     * DEPRECATED.
     * This function broadcasts the field to all clients currently connected to the game.
     */
    public synchronized void broadcastFieldToConnectedPlayers() {
        for (Map.Entry<Integer, ClientInterface> entry : connectedPlayers.entrySet()) {
            try {
                ClientInterface client = entry.getValue();
                client.updateField(field);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This function performs an action, can be one of the following:
     * MoveAction, HealAction, AttackAction.
     * @param action The action to be performed.
     * @throws java.rmi.RemoteException
     */
    @Override
    public synchronized void performAction(Action action) throws java.rmi.RemoteException {
        if (action instanceof MoveAction) {
            MoveAction ma = (MoveAction) action;
            move(action.getSenderId(), ma.getX(), ma.getY());
        } else if (action instanceof HealAction) {
            HealAction ha = (HealAction) action;
            heal(action.getSenderId(), ha.getTargetPlayer());
        } else if (action instanceof AttackAction) {
            AttackAction aa = (AttackAction) action;
            attack(action.getSenderId(), aa.getDragonId());
        }
    }

    /**
     * This function moves a player to a given position. If the move cannot
     * be done or if the player is invalid, it sends an error to the client.
     * @param playerId The (unique) ID of the player to be moved.
     * @param x The x in the grid position to move to.
     * @param y The y in the grid position to move to.
     * @throws RemoteException
     */
    public void move(int playerId, int x, int y) throws RemoteException {
        logger.log(Level.INFO, "Server " + this.ID + " received move to (" + x + ", " + y + ") from player " + playerId);
        if (!isValidPlayerId(playerId)) {
            // TODO send error message
        }

        boolean result = field.movePlayer(playerId, x, y);
        if (!result) {
            // TODO send error message
        } else {
            broadcastFieldToConnectedPlayers();
        }

        checkAndUpdateGUI();
    }

    /**
     * This functions handles a player healing another player.
     * If the player is not withing a range of 5 of the target player, or if the
     * target player's health percentage is above 50%, or if one or both are invalid players
     * then an error will be send back to the clients.
     * @param playerId The player's (unique) ID that wants to heal another player.
     * @param targetPlayer The player receiving the heal.
     * @throws RemoteException
     */
    public void heal(int playerId, int targetPlayer) throws RemoteException {
        logger.log(Level.INFO, "Server " + this.ID + " received heal to player " + targetPlayer + " from player " + playerId);

<<<<<<< HEAD
        // TODO check if both are valid players.

        if(!field.isInRange(playerId, targetPlayer, 5) || field.getPlayer(targetPlayer).getHitPointsPercentage() >= 0.5) {
=======
        if (!field.isInRange(playerId, targetPlayer, 5) || field.getPlayer(targetPlayer).getHitPointsPercentage() >= 0.5) {
>>>>>>> master
            // TODO send error message
        } else {
            Player thisPlayer = field.getPlayer(playerId);
            field.getPlayer(targetPlayer).heal(thisPlayer.getAttackPower());
            broadcastFieldToConnectedPlayers();
        }

        checkAndUpdateGUI();
    }

    /**
     * This function allows a player to attack a dragon.
     * If the dragon is not next to the player or if the dragon or play is invalid,
     * then an error message is send to the client.
     * @param playerId The player's (unique) ID that wants to attack.
     * @param dragonId The dragon's (unique) ID that is being attacked.
     * @throws RemoteException
     */
    public void attack(int playerId, int dragonId) throws RemoteException {
        logger.log(Level.INFO, "Server " + this.ID + " received attack to dragon " + dragonId + " from player " + playerId);

<<<<<<< HEAD
        // TODO check if player and dragon are valid.

        if(!field.isInRange(playerId, dragonId, 1)) {
=======
        if (!field.isInRange(playerId, dragonId, 1)) {
>>>>>>> master
            // TODO send error message
        } else {
            Player thisPlayer = field.getPlayer(playerId);
            field.getDragon(dragonId).getHit(thisPlayer.getAttackPower());
            if (field.getDragon(dragonId).getCurHitPoints() <= 0) {
                field.removeDragon(dragonId);
            }
            broadcastFieldToConnectedPlayers();
        }

        checkAndUpdateGUI();
    }

    /**
     * This function allows a client to connect with a (unique) ID.
     * @param playerId The ID of the player that wishes to connect.
     * @throws RemoteException
     */
    @Override
    public void connect(int playerId) throws RemoteException {

        logger.log(Level.INFO, "Client with id " + playerId + " connected");

        try {
            ClientInterface ci = (ClientInterface) Naming.lookup("//localhost:1099/FDDGClient/" + playerId);
            connectedPlayers.put(playerId, ci);
            field.addPlayer(playerId);
            broadcastFieldToConnectedPlayers();

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        checkAndUpdateGUI();
    }

    /**
     * This function sends a heartbeat to a remote machine, identified by its (unique) ID.
     * @param remoteId The ID of the remote machine.
     * @throws RemoteException
     */
    @Override
    public void heartBeat(int remoteId) throws RemoteException {

    }

    /**
     * This function send back a reply whenever a "ping" comes in.
     * @throws RemoteException
     */
    @Override
    public void pong() throws RemoteException {

    }
}
