package nl.tud.dcs.fddg.client;

import nl.tud.dcs.fddg.game.Field;
import nl.tud.dcs.fddg.game.actions.AttackAction;
import nl.tud.dcs.fddg.game.actions.HealAction;
import nl.tud.dcs.fddg.game.actions.MoveAction;
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

    private final int ID;
    private Logger logger;
    private ServerInterface server;
    private Field field;

    /**
     * Constructor: initializes the instance variables, the logger and binds the client to its registry
     *
     * @param id The process identifier of this client
     * @throws RemoteException
     */
    public ClientProcess(int id) throws RemoteException {
        super();
        this.ID = id;
        this.logger = Logger.getLogger(ClientProcess.class.getName());

        logger.log(Level.INFO, "Starting client with id " + id);
    }

    /**
     * This method can be called to update the field with another field object.
     * @param field The field to set for the client.
     * @throws RemoteException
     */
    @Override
    public synchronized void updateField(Field field) throws RemoteException {
        // logger.log(Level.INFO, "Client " + this.ID + " received field update");
        this.field = field;
    }

    /**
     * This function allows the server to send an error to the client.
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
     * @throws RemoteException
     */
    @Override
    public void ping() throws RemoteException {

    }

    /**
     * This function can be called to check if the character of the client
     *  is alive, that is its current hp is above zero.
     *
     * @return true iff the player is still alive
     */
    public boolean isAlive() {
        return field.getPlayer(this.ID).getCurHitPoints() > 0;
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
            server.connect(this.ID);

            while (isAlive() && !field.gameHasFinished()) {
                Thread.sleep(1000);

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
                    final int MAX_WIDTH_HEIGHT = Math.max(field.BOARD_HEIGHT, field.BOARD_WIDTH) + 5;
                    int newX = move % MAX_WIDTH_HEIGHT;
                    int newY = move / MAX_WIDTH_HEIGHT;

                    MoveAction moveAction = new MoveAction(this.ID, newX, newY);
                    server.performAction(moveAction);
                }
            }

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
