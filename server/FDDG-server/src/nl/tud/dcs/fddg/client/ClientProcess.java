package nl.tud.dcs.fddg.client;

import nl.tud.dcs.fddg.game.Field;
import nl.tud.dcs.fddg.game.actions.*;
import nl.tud.dcs.fddg.game.entities.Dragon;
import nl.tud.dcs.fddg.game.entities.Player;
import nl.tud.dcs.fddg.server.ClientServerInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientProcess extends UnicastRemoteObject implements nl.tud.dcs.fddg.client.ClientInterface, Runnable {

    private int ID;
    private Logger logger;
    private ClientServerInterface server;
    private Field field;
    private boolean isAlive, serverAlive;
    private String[] serverList;

    /**
     * Constructor: initializes the instance variables, the logger and binds the client to its registry
     *
     * @throws RemoteException
     */
    public ClientProcess() throws RemoteException {
        super();
        this.isAlive = true;
        this.serverAlive = false;
        this.logger = Logger.getLogger(ClientProcess.class.getName());
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);

        logger.log(Level.INFO, "Starting client");
    }

    /**
     * Function to give the initial field to a new connected client.
     *
     * @param field The new game field
     * @throws java.rmi.RemoteException
     */
    @Override
    public void initializeField(Field field) throws RemoteException {
        this.field = field;
    }

    /**
     * This method is used to send acknowledgements to the clients.
     *
     * @param action the action to be acknowledged.
     * @throws java.rmi.RemoteException
     */
    @Override
    public void performAction(Action action) throws RemoteException {
        logger.fine("Client "+this.ID+ " is performing a "+action.getClass().getName());
        // do additional work if the action is a delete unit action (as it requires access to this class' instance variables)
        if (action instanceof DeleteUnitAction) {
            DeleteUnitAction dua = (DeleteUnitAction) action;
            int unitID = dua.getUnitId();
            if ((field.getDragon(unitID) == null) && (unitID == this.ID)) {
                logger.info("I have been killed (player "+ID+")");
                isAlive = false;
            }
        }

        action.perform(field);
    }

    /**
     * This function allows the server to send an error to the client.
     *
     * @param errorId The ID of the error.
     * @param message The message that goes with the error.
     * @throws RemoteException
     */
    @Override
    public void receiveError(int errorId, String message) throws RemoteException {
        logger.log(Level.SEVERE, "Client " + this.ID + " received error: " + message);
    }

    /**
     * This function serves as a heartbeat to check if the client is still connected.
     *
     * @throws RemoteException
     */
    @Override
    public void ping() throws RemoteException {
    }

    /**
     * Main loop of the client process. Here, while the client is alive and the game is still going,
     * executes its strategy to win the game.
     */
    @Override
    public void run() {

        // send a connect message to the server
        try {
            this.ID = server.register();

            Naming.rebind("FDDGClient/" + this.ID, this);
            server.connect(this.ID);

            while (isAlive && !field.gameHasFinished()) {
                Thread.sleep(1000);

                // Check if the server is still alive
                try {
                    server.pong();
                    serverAlive = true;
                } catch (RemoteException e) {
                    if (serverAlive) {
                        serverAlive = false;
                    } else {
                        serverCrashed();
                    }
                }

                // check if there is a nearby player with hp < 50% to heal
                Dragon dragonToAttack;
                Player playerToHeal = field.isInRangeToHeal(this.ID);
                if (playerToHeal != null) {
                    server.requestAction(new HealAction(this.ID, playerToHeal.getUnitId()));
                    logger.fine("Client " + this.ID + " send request for a HealAction");
                } else if ((dragonToAttack = field.dragonIsInRangeToAttack(this.ID)) != null) {
                    server.requestAction(new AttackAction(this.ID, dragonToAttack.getUnitId()));
                    logger.fine("Client " + this.ID + " send request for a AttackAction");
                } else {
                    Player p = field.getPlayer(this.ID);
                    int move = field.getDirectionToNearestDragon(p.getxPos(), p.getyPos());
                    if (move == -1) {
                        logger.log(Level.INFO, "Player" + this.ID + " couldn't move towards a dragon (blocked?)");
                        continue;
                    }
                    final int MAX_WIDTH_HEIGHT = Math.max(Field.BOARD_HEIGHT, Field.BOARD_WIDTH) + 5;
                    int newX = move % MAX_WIDTH_HEIGHT;
                    int newY = move / MAX_WIDTH_HEIGHT;

                    MoveAction moveAction = new MoveAction(this.ID, newX, newY);
                    server.requestAction(moveAction);
                    logger.fine("Client " + this.ID + " send request for a MoveAction");
                }
            }

        } catch (MalformedURLException | RemoteException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function that selects one of the servers as its server.
     *
     * @param serverURLs The URLs of the servers
     */
    public void selectServer(String[] serverURLs) {
        if(serverList == null){
            serverList = serverURLs;
        }
        final int totalAttempts = 10;
        int attempts = 0;
        while(attempts < totalAttempts) {
            Random random = new Random();
            int randomServerId = random.nextInt(serverURLs.length);
            try {
                logger.info("Client " + ID + " trying to connect to " + serverURLs[randomServerId]);
                server = (ClientServerInterface) Naming.lookup(serverURLs[randomServerId]);
                return;
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                logger.severe("Could not connect to server: " + serverURLs[randomServerId]);
                e.printStackTrace();
                attempts++;
            }
        }
        logger.severe("All servers are down apparently (10 attempts failed)");
        System.exit(1);
    }

    /**
     * This function gets called when two consecutive server heartbeats were missed.
     */
    public void serverCrashed() {
        // Server crashed so we reconnect to another one.
        selectServer(serverList);
        this.run();
    }
}
