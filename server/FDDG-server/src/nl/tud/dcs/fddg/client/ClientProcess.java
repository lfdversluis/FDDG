package nl.tud.dcs.fddg.client;

import nl.tud.dcs.fddg.game.Field;
import nl.tud.dcs.fddg.game.actions.*;
import nl.tud.dcs.fddg.game.entities.Dragon;
import nl.tud.dcs.fddg.game.entities.Player;
import nl.tud.dcs.fddg.server.ServerInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientProcess extends UnicastRemoteObject implements ClientInterface, Runnable {

    private int ID;
    private Logger logger;
    private ServerInterface server;
    private Field field;
    private boolean isAlive, serverAlive;

    /**
     * Constructor: initializes the instance variables, the logger and binds the client to its registry
     *
     * @throws RemoteException
     */
    public ClientProcess() throws RemoteException {
        super();
        this.isAlive = true;
        this.serverAlive = true;
        this.logger = Logger.getLogger(ClientProcess.class.getName());

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
    public synchronized void ack(Action action) throws RemoteException {
        if (action instanceof AddPlayerAction) {
            AddPlayerAction apa = (AddPlayerAction) action;
            field.addPlayer(apa.getPlayerId(), apa.getX(), apa.getY());
        } else if (action instanceof AttackAction) {
            AttackAction ata = (AttackAction) action;
            Dragon d = field.getDragon(ata.getDragonId());
            Player p = field.getPlayer(ata.getSenderId());
            d.setCurHitPoints(d.getCurHitPoints() - p.getAttackPower());
        } else if (action instanceof DeleteUnitAction) {
            DeleteUnitAction dua = (DeleteUnitAction) action;
            if (field.getDragon(dua.getUnitId()) == null) {
                Player p = field.getPlayer(dua.getUnitId());
                field.removePlayer(p.getUnitId());
                if (p.getUnitId() == this.ID) {
                    isAlive = false;
                }
            } else {
                Dragon d = field.getDragon(dua.getUnitId());
                field.removeDragon(d.getUnitId());
            }
        } else if (action instanceof HealAction) {
            HealAction ha = (HealAction) action;
            int playerId = ha.getSenderId();
            int targetPlayer = ha.getTargetPlayer();
            Player thisPlayer = field.getPlayer(playerId);
            field.getPlayer(targetPlayer).heal(thisPlayer.getAttackPower());
        } else if (action instanceof MoveAction) {
            MoveAction ma = (MoveAction) action;
            int playerId = ma.getSenderId();
            int x = ma.getX();
            int y = ma.getY();
            field.movePlayer(playerId, x, y);
        } else if (action instanceof DamageAction) {
            DamageAction da = (DamageAction) action;
            int playerId = da.getPlayerId();
            int damage = da.getDamage();
            field.getPlayer(playerId).setCurHitPoints(field.getPlayer(playerId).getCurHitPoints() - damage);
        }
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
    public boolean ping() throws RemoteException {
        return true;
    }

    /**
     * Main loop of the client process. Here, while the client is alive and the game is still going,
     * executes its strategy to win the game.
     */
    @Override
    public void run() {

        // send a connect message to the server
        try {
            server = (ServerInterface) Naming.lookup("FDDGServer/0");
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
                    server.performAction(new HealAction(this.ID, playerToHeal.getUnitId()));
                } else if ((dragonToAttack = field.dragonIsInRangeToAttack(this.ID)) != null) {
                    server.performAction(new AttackAction(this.ID, dragonToAttack.getUnitId()));
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
                    server.performAction(moveAction);
                }
            }

        } catch (NotBoundException | MalformedURLException | RemoteException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function gets called when two consecutive server heartbeats were missed.
     */
    public void serverCrashed() {
        // TODO implement what to do when server has crashed.
    }
}
