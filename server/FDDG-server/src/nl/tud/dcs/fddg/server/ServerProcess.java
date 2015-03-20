package nl.tud.dcs.fddg.server;

import nl.tud.dcs.fddg.client.ClientInterface;
import nl.tud.dcs.fddg.game.Field;
import nl.tud.dcs.fddg.game.actions.*;
import nl.tud.dcs.fddg.game.entities.Player;
import nl.tud.dcs.fddg.gui.VisualizerGUI;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerProcess extends UnicastRemoteObject implements ServerInterface, Runnable {

    private final int ID;
    private Field field;
    private Logger logger;
    private volatile Map<Integer, ClientInterface> connectedPlayers;
    private VisualizerGUI visualizerGUI = null;
    private boolean gameStarted;
    private int IDCounter;
    private List<ServerInterface> otherServers;

    /**
     * The constructor of the ServerProcess class. It requires an ID and a flag indicating whether a GUI should be started or not..
     *
     * @param id     The (unique) ID of the server
     * @param useGUI The flag that tells whether this server should run a GUI or not
     * @throws RemoteException
     */
    public ServerProcess(int id, boolean useGUI) throws RemoteException {
        super();
        this.ID = id;
        this.field = new Field();
        this.logger = Logger.getLogger(ServerProcess.class.getName());
        this.connectedPlayers = new ConcurrentHashMap<>();
        this.gameStarted = false;
        this.IDCounter = 0;
        this.otherServers = new ArrayList<>();

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
            do {
                Thread.sleep(1000);
            } while (!gameStarted);

            while (!field.gameHasFinished()) {
                Set<Action> actionSet = field.dragonRage();

                for (Action a : actionSet) {
                    broadcastActionToPlayers(a);
                }
                Thread.sleep(1000);
            }

            logger.log(Level.INFO, "Server " + ID + " finished the game.");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcast an action to all players
     *
     * @param action The action to be broadcasted.
     */
    public synchronized void broadcastActionToPlayers(Action action) {
        for (Map.Entry<Integer, ClientInterface> entry : connectedPlayers.entrySet()) {
            try {
                ClientInterface client = entry.getValue();
                client.ack(action);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Only updates the GUI if there actually is one
     */
    private void checkAndUpdateGUI() {
        if (visualizerGUI != null)
            visualizerGUI.updateGUI();
    }

    /**
     * Function to check if a player ID is valid.
     *
     * @param playerId The player ID to check.
     * @return Returns a boolean indicating if the player is valid.
     */
    public boolean isValidPlayerId(int playerId) {
        return field.isValidPlayerId(playerId);
    }

    /**
     * This function performs an action, can be one of the following:
     * MoveAction, HealAction, AttackAction.
     *
     * @param action The action to be performed.
     * @throws java.rmi.RemoteException
     */
    @Override
    public synchronized void performAction(Action action) throws java.rmi.RemoteException {
        if (action instanceof MoveAction) {
            MoveAction ma = (MoveAction) action;
            move(ma);
        } else if (action instanceof HealAction) {
            HealAction ha = (HealAction) action;
            heal(ha);
        } else if (action instanceof AttackAction) {
            AttackAction aa = (AttackAction) action;
            attack(aa);
        }
    }

    /**
     * This function moves a player to a given position. If the move cannot
     * be done or if the player is invalid, it sends an error to the client.
     *
     * @param ma The MoveAction to be performed.
     * @throws RemoteException
     */
    public void move(MoveAction ma) throws RemoteException {
        int playerId = ma.getSenderId();
        int x = ma.getX();
        int y = ma.getY();

        logger.log(Level.INFO, "Server " + this.ID + " received move to (" + x + ", " + y + ") from player " + playerId);
        if (!isValidPlayerId(playerId)) {
            // TODO send error message
        }

        boolean result = field.movePlayer(playerId, x, y);
        if (!result) {
            // TODO send error message
        } else {
            broadcastActionToPlayers(ma);
        }
        checkAndUpdateGUI();
    }

    /**
     * This functions handles a player healing another player.
     * If the player is not withing a range of 5 of the target player, or if the
     * target player's health percentage is above 50%, or if one or both are invalid players
     * then an error will be send back to the clients.
     *
     * @param ha The HealAction to be performed.
     * @throws RemoteException
     */
    public void heal(HealAction ha) throws RemoteException {
        int playerId = ha.getSenderId();
        int targetPlayer = ha.getTargetPlayer();

        logger.log(Level.INFO, "Server " + this.ID + " received heal to player " + targetPlayer + " from player " + playerId);

        if (!field.isInRange(playerId, targetPlayer, 5) || field.getPlayer(targetPlayer).getHitPointsPercentage() >= 0.5 || field.getPlayer(targetPlayer).getCurHitPoints() <= 0) {
            // TODO send error message
        } else {
            Player thisPlayer = field.getPlayer(playerId);
            field.getPlayer(targetPlayer).heal(thisPlayer.getAttackPower());
            broadcastActionToPlayers(ha);
        }
        checkAndUpdateGUI();
    }

    /**
     * This function allows a player to attack a dragon.
     * If the dragon is not next to the player or if the dragon or play is invalid,
     * then an error message is send to the client.
     *
     * @param aa The AttackAction to be performed.
     * @throws RemoteException
     */
    public void attack(AttackAction aa) throws RemoteException {
        int playerId = aa.getSenderId();
        int dragonId = aa.getDragonId();

        logger.log(Level.INFO, "Server " + this.ID + " received attack to dragon " + dragonId + " from player " + playerId);

        if (!field.isInRange(playerId, dragonId, 1)) {
            // TODO send error message
        } else {
            Player thisPlayer = field.getPlayer(playerId);
            field.getDragon(dragonId).getHit(thisPlayer.getAttackPower());
            if (field.getDragon(dragonId).getCurHitPoints() <= 0) {
                field.removeDragon(dragonId);
                DeleteUnitAction dua = new DeleteUnitAction(dragonId);
                broadcastActionToPlayers(dua);
            } else {
                broadcastActionToPlayers(aa);
            }
        }
        checkAndUpdateGUI();
    }

    /**
     * This function gets called by a client to connect to a server.
     *
     * @throws RemoteException
     */
    @Override
    public int register() throws RemoteException {
        return IDCounter++;
    }

    /**
     * This function allows a client to connect with a (unique) ID.
     *
     * @param clientId The ID of the player that wishes to connect.
     * @throws RemoteException
     */
    @Override
    public void connect(int clientId) throws RemoteException {

        logger.log(Level.INFO, "Client with id " + clientId + " connected");

        if (!gameStarted) {
            logger.log(Level.INFO, "Game started on server " + ID);
            gameStarted = true;
        }

        try {
            ClientInterface ci = (ClientInterface) Naming.lookup("FDDGClient/" + clientId);
            field.addPlayer(clientId);
            ci.initializeField(field);

            AddPlayerAction apa = new AddPlayerAction(clientId, field.getPlayer(clientId).getxPos(), field.getPlayer(clientId).getyPos());
            broadcastActionToPlayers(apa);

            // Now the broadcast is done, add the player to the player map (so he doesn't add himself again on the field).
            connectedPlayers.put(clientId, ci);

        } catch (NotBoundException | MalformedURLException e) {
            e.printStackTrace();
        }
        checkAndUpdateGUI();
    }

    /**
     * This function sends a heartbeat to a remote machine, identified by its (unique) ID.
     *
     * @param remoteId The ID of the remote machine.
     * @throws RemoteException
     */
    @Override
    public void heartBeat(int remoteId) throws RemoteException {

    }

    /**
     * This function send back a reply whenever a "ping" comes in.
     *
     * @throws RemoteException
     */
    @Override
    public void pong() throws RemoteException {

    }

    /**
     * Binds this server to the registry and connects to all other servers.
     *
     * @param serverURLs The URLs of all servers
     * @throws MalformedURLException
     * @throws RemoteException
     */
    public void registerAndConnectToAll(String[] serverURLs) throws MalformedURLException, RemoteException {
        Naming.rebind(serverURLs[ID], this);
        for (int i = 0; i < serverURLs.length; i++)
            if (i != ID)
                connectToServer(serverURLs[i]);
    }

    /**
     * Blocking method that waits until the server (identified by the serverURL) is online.
     *
     * @param serverURL The URL of the server we are waiting for
     * @throws RemoteException
     * @throws MalformedURLException
     */
    private void connectToServer(String serverURL) throws RemoteException, MalformedURLException {
        while (true) {
            try {
                otherServers.add((ServerInterface) Naming.lookup(serverURL));
                logger.info("Connected to server: "+serverURL);
                break;
            } catch (NotBoundException ignoredException) {
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
