package nl.tud.dcs.fddg.client;

import nl.tud.dcs.fddg.game.Field;
import nl.tud.dcs.fddg.game.actions.*;
import nl.tud.dcs.fddg.game.entities.Dragon;
import nl.tud.dcs.fddg.game.entities.Player;
import nl.tud.dcs.fddg.server.ClientServerInterface;

import java.io.File;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.rmi.Naming;
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

    // Logging
    private PrintWriter writer;
    private int messagesToServer, messagesFromServer, pingsToServer, pingsFromServer;

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
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);

        if(logger.getHandlers().length == 0) {
            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            logger.addHandler(consoleHandler);
        }

        this.messagesFromServer = 0;
        this.messagesToServer = 0;
        this.pingsFromServer = 0;
        this.pingsToServer = 0;

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
        logger.fine("Client " + this.ID + " is performing a " + action.getClass().getName());
        messagesFromServer++;
        // do additional work if the action is a delete unit action (as it requires access to this class' instance variables)
        if (action instanceof DeleteUnitAction) {
            DeleteUnitAction dua = (DeleteUnitAction) action;
            int unitID = dua.getUnitId();
            if ((field.getDragon(unitID) == null) && (unitID == this.ID)) {
                logger.info("I have been killed (player " + ID + ")");
                isAlive = false;
            }
        } else if (action instanceof EndOfGameAction) {
            logger.info("Server " + action.getSenderId() + " finished the game.");
            isAlive = false;
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
        pingsFromServer++;
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

            File file = new File("logs/ClientProcess_log_"+ this.ID + ".txt");
            file.getParentFile().mkdirs();
            writer = new PrintWriter(file, "UTF-8");

            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            String remoteName = "//"+ipAddress+":1099/FDDGClient/"+this.ID;
            Naming.rebind("FDDGClient/"+this.ID, this);
            server.connect(this.ID, remoteName);

            while (isAlive && !field.gameHasFinished()) {
                // Check if the server is still alive
                try {
                    server.pong();
                    pingsToServer++;

                    serverAlive = true;
                } catch (RemoteException e) {
                    if (serverAlive) {
                        serverAlive = false;
                        // we sleep 1 second, then retry with a ping
                        Thread.sleep(1000);
                        continue;
                    } else {
                        // Stop current run and reconnect to another server
                        isAlive = false;
                        serverCrashed();
                        break;
                    }
                }

                // check if there is a nearby player with hp < 50% to heal
                Dragon dragonToAttack;
                Player playerToHeal = field.isInRangeToHeal(this.ID);
                if (playerToHeal != null) {
                    server.requestAction(new HealAction(this.ID, playerToHeal.getUnitId()));
                    logger.fine("Client " + this.ID + " send request for a HealAction");
                    writer.println("Client " + this.ID + " send request for a HealAction");
                } else if ((dragonToAttack = field.dragonIsInRangeToAttack(this.ID)) != null) {
                    server.requestAction(new AttackAction(this.ID, dragonToAttack.getUnitId()));
                    logger.fine("Client " + this.ID + " send request for a AttackAction");
                    writer.println("Client " + this.ID + " send request for a AttackAction");
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
                    writer.println("Client " + this.ID + " send request for a MoveAction");
                }

                messagesToServer++;

                Thread.sleep(1000);
            }

            Thread.sleep(1000);

            logger.fine("Client " + this.ID + " stopping.");
            writer.println("Client " + this.ID + " pings-to-server " + pingsToServer);
            writer.println("Client "+ this.ID + " pings-from-server " + pingsFromServer);
            writer.println("Client "+ this.ID + " messages-to-server " + messagesToServer);
            writer.println("Client "+ this.ID + " messages-from-server " + messagesFromServer);
            writer.println("Client "+ this.ID + " game finished");
            writer.flush();
            writer.close();

            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function that selects one of the servers as its server.
     *
     * @param serverURLs The URLs of the servers
     */
    public void selectServer(String[] serverURLs, boolean shouldReconnect) {
        if (serverList == null) {
            serverList = serverURLs;
        }
        final int totalAttempts = 10;
        int attempts = 0;
        while (attempts < totalAttempts) {
            Random random = new Random();
            int randomServerId = random.nextInt(serverURLs.length);
            try {
                logger.info("Client trying to connect to " + serverURLs[randomServerId]);
                server = (ClientServerInterface) Naming.lookup(serverURLs[randomServerId]);
                if(shouldReconnect) {
                    String clientName = "//" + InetAddress.getLocalHost().getHostAddress() + ":1099/FDDGClient/" + ID;
                    server.reconnect(this.ID, clientName);
                    messagesToServer++;
                    writer.println("Client " + this.ID + "  connect " + randomServerId);
                }
                return;
            } catch (Exception e) {
                logger.severe("Could not connect to server: " + serverURLs[randomServerId]);
                e.printStackTrace();
                attempts++;
            }
        }
        logger.severe("All servers are down apparently (10 attempts failed)");
        writer.println("Client " + this.ID + " connect failure");
        writer.flush();
        writer.close();
        System.exit(1);
    }

    /**
     * This function gets called when two consecutive server heartbeats were missed.
     */
    public void serverCrashed() {
        // Server crashed so we reconnect to another one.
        writer.println("Server crashed");
        selectServer(serverList, true);
        isAlive = true;
        this.run();
    }
}
