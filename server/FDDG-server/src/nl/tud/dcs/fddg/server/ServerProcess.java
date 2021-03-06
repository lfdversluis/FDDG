package nl.tud.dcs.fddg.server;

import nl.tud.dcs.fddg.client.ClientInterface;
import nl.tud.dcs.fddg.game.Field;
import nl.tud.dcs.fddg.game.actions.*;
import nl.tud.dcs.fddg.game.entities.Player;
import nl.tud.dcs.fddg.gui.VisualizerGUI;
import nl.tud.dcs.fddg.server.requests.ActionRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerProcess extends UnicastRemoteObject implements ClientServerInterface, Runnable, ServerInterface {

    // internal administration
    private int ID;
    private Field field;
    private Logger logger;
    private VisualizerGUI visualizerGUI = null;
    private boolean gameStarted;
    private boolean gameFinishedByOtherServer;

    // client administration
    private volatile Map<Integer, ClientInterface> connectedPlayers;
    private Map<Integer, Boolean> clientPings;
    private int IDCounter;

    // server administration
    private Map<Integer, ServerInterface> otherServers; //(id, RMI object)
    private int requestCounter;
    private ConcurrentMap<Integer, ActionRequest> pendingRequests; //(requestID,request)
    private Map<Integer, Timer> requestTimers; //(requestID, timer
    private Map<Integer, Integer> pendingAcknowledgements; //(requestID, nr of acks still to receive)
    private Map<Integer, Integer> serverPings; // (ID, # consecutive pings missed)

    // Logging
    private PrintWriter writer;
    private int serverAmountOfMessagesReceived, serverAmountOfMessagedSent, serverAmountOfPingsSent, serverAmountOfPingsReceived;
    private int clientAmountOfMessagesReceived, clientAmountOfMessagedSent, clientAmountOfPingsSent, clientAmountOfPingsReceived;

    /**
     * The constructor of the ServerProcess class.
     * It requires an ID and a flag indicating whether a GUI should be started or not..
     *
     * @param id     The (unique) ID of the server
     * @param useGUI The flag that tells whether this server should run a GUI or not
     * @throws RemoteException
     */
    public ServerProcess(int id, boolean useGUI, String fieldFile) throws RemoteException, FileNotFoundException {
        super();
        this.ID = id;
        this.field = new Field(fieldFile);
        this.logger = Logger.getLogger(ServerProcess.class.getName());
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);
        this.gameStarted = false;
        this.serverPings = new HashMap<Integer, Integer>();
        this.gameFinishedByOtherServer = false;
        this.connectedPlayers = new ConcurrentHashMap<Integer, ClientInterface>();
        this.clientPings = new HashMap<Integer, Boolean>();
        this.IDCounter = 1000 * this.ID;

        this.requestCounter = 0;
        this.otherServers = new HashMap<Integer, ServerInterface>();
        this.pendingRequests = new ConcurrentHashMap<Integer, ActionRequest>();
        this.requestTimers = new HashMap<Integer, Timer>();
        this.pendingAcknowledgements = new HashMap<Integer, Integer>();

        try {
            File file = new File("logs/ServerProcess_log_"+ this.ID + ".txt");
            file.getParentFile().mkdirs();
            writer = new PrintWriter(file, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.serverAmountOfMessagesReceived = 0;
        this.serverAmountOfMessagedSent = 0;
        this.serverAmountOfPingsReceived = 0;
        this.serverAmountOfPingsSent = 0;
        this.clientAmountOfMessagesReceived = 0;
        this.clientAmountOfMessagedSent = 0;
        this.clientAmountOfPingsReceived = 0;
        this.clientAmountOfPingsSent = 0;

        // start GUI if necessary
        if (useGUI) {
            this.visualizerGUI = new VisualizerGUI(field);
        }

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
            } while (!gameStarted && !gameFinishedByOtherServer);

            while (!gameFinishedByOtherServer && !field.gameHasFinished()) {
                // Ping all clients
                for (int clientId : connectedPlayers.keySet()) {
                    ClientInterface ci = connectedPlayers.get(clientId);

                    clientAmountOfPingsSent++;

                    try {
                        ci.ping();
                        clientPings.put(clientId, true);
                    } catch (RemoteException e) {
                        if (clientPings.containsKey(clientId) && !clientPings.get(clientId)) {
                            clientCrashed(clientId);
                        } else {
                            clientPings.put(clientId, false);
                        }
                    }
                }

                // Ping all servers
                for (int serverId : otherServers.keySet()) {
                    serverAmountOfPingsSent++;

                    try {
                        otherServers.get(serverId).heartBeat(serverId);
                        serverPings.put(serverId, 0);
                    } catch (RemoteException e) {
                        serverPings.put(serverId, serverPings.get(serverId) + 1);
                    }
                }

                // Check if a server has not answered to 2 or more consecutive pings.
                for (int serverId : serverPings.keySet()) {
                    if (serverPings.get(serverId) > 1) {
                        serverCrashed(serverId);
                    }
                }

                //only attack the servers own players (not the others!!!!). Then, no acks are required
                Set<Action> actionSet = field.dragonRage(connectedPlayers.keySet());
                for (Action a : actionSet) {
                    broadcastActionToClients(a);
                    broadcastActionToServers(a);
                    checkAndUpdateGUI();
                }

                writer.println("Server " + this.ID + " clients " + connectedPlayers.size());

                Thread.sleep(1000);
            }

            if (!gameFinishedByOtherServer) {
                // game is finished on this server, so inform all other clients and servers
                logger.info("Server " + ID + " finished the game.");
                EndOfGameAction endAction = new EndOfGameAction(this.ID);
                broadcastActionToClients(endAction);
                broadcastActionToServers(endAction);
            }

            logger.info("Server " + ID + " is going to sleep and exit now");
            writer.println("Server " + this.ID + " server-pings-sent " + serverAmountOfPingsSent);
            writer.println("Server " + this.ID + " server-pings-received " + serverAmountOfPingsReceived);
            writer.println("Server " + this.ID + " server-messages-sent " + serverAmountOfMessagedSent);
            writer.println("Server " + this.ID + " server-messages-received " + serverAmountOfMessagesReceived);
            writer.println("Server " + this.ID + " client-pings-sent " + clientAmountOfPingsSent);
            writer.println("Server " + this.ID + " client-pings-received " + clientAmountOfPingsReceived);
            writer.println("Server " + this.ID + " client-messages-sent " + clientAmountOfMessagedSent);
            writer.println("Server " + this.ID + " client-messages-received " + clientAmountOfMessagesReceived);
            writer.print("Server " + this.ID + " game finished");
            writer.flush();
            writer.close();
            Thread.sleep(1000);
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcast an action to all connected clients
     *
     * @param action The action to be broadcasted.
     */
    public void broadcastActionToClients(Action action) {
        for (ClientInterface client : connectedPlayers.values()) {
            try {
                client.performAction(action);
                clientAmountOfMessagedSent++;
            } catch (RemoteException e) {
                logger.severe("Unable to send action to client.");
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
     * A client calls this function if it wants to perform one of the following actions:
     * MoveAction, HealAction, AttackAction.
     *
     * @param action The action the client wants to be performed.
     * @throws RemoteException
     */
    @Override
    public void requestAction(Action action) throws RemoteException {
        logger.fine("Received " + action.getClass().getSimpleName() + " request from client " + action.getSenderId());

        clientAmountOfMessagesReceived++;

        if (isValidAction(action)) {
            if (!otherServers.isEmpty()) {
                sendRequestsForAction(action);
            }
            else {
                performAction(action);
            }
        }
    }

    /**
     * Checks whether the action is valid for this server.
     * This is done by checking the validity of the action in the field of this server
     * and by checking whether is does not conflict with this server's pending requests.
     *
     * @param action The action of which the validity needs to be checked
     * @return true iff the action can safely be performed on this server
     */
    private boolean isValidAction(Action action) {
        return action.isValid(field) && checkPendingRequestsForAction(action);
    }

    /**
     * Checks whether there are conflicts between the current request and the pending requests.
     *
     * @param action The action a client wants to perform
     * @return true iff there are no conflicts with the pending actions
     */
    private boolean checkPendingRequestsForAction(Action action) {
        if (action instanceof MoveAction) {
            //check if one of the pending requests also wants to go to the same destination tile
            MoveAction move = (MoveAction) action;
            for (ActionRequest request : pendingRequests.values())
                if (request.getAction() instanceof MoveAction)
                    if (((MoveAction) request.getAction()).hasSameDestinationAs(move))
                        return false;
        }
        return true;
    }

    /**
     * Creates an ActionRequest for the action and broadcasts this to the other servers
     *
     * @param action The action that is requested
     */
    private void sendRequestsForAction(Action action) throws RemoteException {
        //create request
        final ActionRequest request = new ActionRequest(requestCounter++, this.ID, action);

        //initialize acknowledgement counter for the request
        pendingRequests.put(request.getRequestID(), request);
        pendingAcknowledgements.put(request.getRequestID(), otherServers.size());

        //init timer to remove request from the data structures when not all acks are received after 5s
        Timer timer = new Timer();
        requestTimers.put(request.getRequestID(), timer);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                removeRequest(request.getRequestID());
            }
        }, 5000);

        logger.fine("Sending request " + request.getRequestID() + " with action " + request.getAction().getClass().getSimpleName() + " to all servers...");

        //broadcast the request to the other servers
        for (ServerInterface server : otherServers.values()) {
            serverAmountOfMessagedSent++;
            server.requestAction(request);
        }
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
    public void connect(int clientId, String clientName) throws RemoteException {
        logger.log(Level.INFO, "Client " + clientName + " connected");

        clientAmountOfMessagesReceived++;

        try {
            ClientInterface ci = (ClientInterface) Naming.lookup(clientName);
            field.addPlayer(clientId);
            ci.initializeField(field);

            Player newPlayer = field.getPlayer(clientId);
            AddPlayerAction apa = new AddPlayerAction(clientId, newPlayer.getxPos(), newPlayer.getyPos(), newPlayer.getHitpoints(), newPlayer.getAttackPower());
            broadcastActionToClients(apa);
            broadcastActionToServers(apa); //inform other servers

            // Now the broadcast is done, add the player to the player map (so he doesn't add himself again on the field).
            connectedPlayers.put(clientId, ci);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!gameStarted) {
            logger.log(Level.INFO, "Game started on server " + ID);
            gameStarted = true;
        }
        checkAndUpdateGUI();
    }

    /**
     * Function that is called when a client's server crashed and it select another one
     *
     * @param clientID   The id of de client of which the server crashed
     * @param clientName The name of the remote object of the client
     * @throws java.rmi.RemoteException
     */
    @Override
    public void reconnect(int clientID, String clientName) throws RemoteException {

        clientAmountOfMessagesReceived++;

        try {
            if (!connectedPlayers.containsKey(clientID)) {
                connectedPlayers.put(clientID, (ClientInterface) Naming.lookup(clientName));
            }
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function sends a heartbeat to a remote machine, identified by its (unique) ID.
     *
     * @param remoteId The ID of the remote machine.
     * @throws RemoteException
     */
    @Override
    public void heartBeat(int remoteId) throws RemoteException {
        serverAmountOfPingsReceived++;
    }

    /**
     * This function send back a reply whenever a "ping" comes in.
     *
     * @throws RemoteException
     */
    @Override
    public void pong() throws RemoteException {
        clientAmountOfPingsReceived++;
    }

    /**
     * This server gets called when a client hasn't responded to two consecutive heartbeats.
     *
     * @param clientId The ID of the client that probably has crashed.
     */
    public void clientCrashed(int clientId) throws RemoteException {
        logger.info("Client " + clientId + " has crashed, removing him now");

        //remove it from connectedPlayers
        connectedPlayers.remove(clientId);

        //make, perform and broadcast deleteUnitAction to all other clients
        DeleteUnitAction delAction = new DeleteUnitAction(clientId);
        performAction(delAction);

        //broadcast deleteUnitAction to all other servers
        broadcastActionToServers(delAction);
    }

    /**
     * This function gets called when a server suspects a peer server has dropped.
     *
     * @param serverId The ID of the server that probably has crashed.
     */
    public void serverCrashed(int serverId) {
        logger.info("Server " + serverId + " has crashed, removing him now");

        //remove it from otherServers and the serverPings.
        otherServers.remove(serverId);
        serverPings.remove(serverId);
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
        int id = Integer.parseInt(serverURL.substring(serverURL.lastIndexOf("/") + 1));
        while (true) {
            try {
                otherServers.put(id, (ServerInterface) Naming.lookup(serverURL));
                logger.info("Connected to server: " + serverURL);
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

    /**
     * Method that one server calls on all other servers to request a certain action.
     *
     * @param request A request containing the id and the action the server wants to perform
     * @throws java.rmi.RemoteException
     */
    @Override
    public void requestAction(ActionRequest request) throws RemoteException {

        serverAmountOfMessagesReceived++;

        if (isValidAction(request.getAction())) {
            int senderID = request.getSenderID();
            int requestID = request.getRequestID();

            logger.fine("Acknowledging request " + requestID + " with " + request.getAction().getClass().getSimpleName() + " to server " + senderID);
            otherServers.get(senderID).acknowledgeRequest(requestID);
            serverAmountOfMessagedSent++;
        }
    }

    /**
     * Method that is used to acknowledge a request send by another server
     *
     * @param requestID The id of the request
     * @throws java.rmi.RemoteException
     */
    @Override
    public void acknowledgeRequest(int requestID) throws RemoteException {
        logger.finer("Received acknowledgement for request " + requestID);

        serverAmountOfMessagesReceived++;

        //decrement pending acknowledgement counter (if the request still exists)
        if (pendingAcknowledgements.containsKey(requestID)) {
            int newCount = pendingAcknowledgements.get(requestID) - 1;
            pendingAcknowledgements.put(requestID, newCount);

            //if all acknowledgements are received, remove the request and perform the action
            if (newCount == 0) {
                logger.fine("All acknowledgements for request " + requestID + " received");
                Action action = pendingRequests.get(requestID).getAction();

                //perform action on local field + connected clients
                performAction(action);

                broadcastActionToServers(action);

                //cleanup
                removeRequest(requestID);
            }
        }
    }

    /**
     * Broadcast an action to all other servers
     *
     * @param action The action to be broadcasted.
     */
    private void broadcastActionToServers(Action action) {
        for (int serverId : otherServers.keySet()) {
            serverAmountOfMessagedSent++;
            try {
                ServerInterface server = otherServers.get(serverId);
                server.performAction(action);
            } catch(Exception e) {
                serverCrashed(serverId);
            }

        }
    }

    /**
     * Removes the request with requestID from the pending requests (and acknowledgement counter).
     * It also stops and removes the timer associated with the request
     *
     * @param requestID The id of the request to be removed
     */
    private void removeRequest(int requestID) {
        if(requestTimers.containsKey(requestID)) {
            requestTimers.get(requestID).cancel();
            requestTimers.remove(requestID);
            pendingAcknowledgements.remove(requestID);
            pendingRequests.remove(requestID);
        }
    }

    /**
     * Method that indicates that the server should perform a certain action.
     * This method is only invoked when all other servers have acknowledged the request for the action.
     *
     * @param action The action that should be executed
     * @throws java.rmi.RemoteException
     */
    @Override
    public void performAction(Action action) throws RemoteException {
        logger.fine("Performing " + action.getClass() + " from client " + action.getSenderId());

        serverAmountOfMessagesReceived++;

        //perform the action on the local field
        action.perform(field);

        //if a dragon is killed, the action is changed to a DeleteUnitAction
        if (action instanceof AttackAction) {
            AttackAction aa = (AttackAction) action;
            int dragonID = aa.getDragonId();
            if (field.getDragon(dragonID) != null && field.getDragon(dragonID).getCurHitPoints() <= 0) {
                field.removeDragon(dragonID);
                action = new DeleteUnitAction(dragonID);
            }
        }

        checkAndUpdateGUI();
        broadcastActionToClients(action);

        if (action instanceof EndOfGameAction) {
            logger.info("Server " + action.getSenderId() + " finished the game.");
            gameFinishedByOtherServer = true;
        }
    }
}
