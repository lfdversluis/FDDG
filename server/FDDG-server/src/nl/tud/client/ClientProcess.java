package nl.tud.client;

import nl.tud.Main;
import nl.tud.ServerInterface;
import nl.tud.entities.Dragon;
import nl.tud.entities.Player;
import nl.tud.gameobjects.Field;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientProcess extends UnicastRemoteObject implements ClientInterface, Runnable {

    private final int ID;
    private Logger logger;
    private ServerInterface server;
    private Field field;

    public ClientProcess(int id) throws RemoteException, AlreadyBoundException, MalformedURLException {
        this.ID = id;
        this.logger = Logger.getLogger(ClientProcess.class.getName());

        logger.log(Level.INFO, "Starting client with id " + id);

        java.rmi.Naming.bind("rmi://localhost:" + Main.SERVER_PORT + "/FDDGClient/" + id, this);
    }

    @Override
    public synchronized void updateField(Field field) throws RemoteException {
        logger.log(Level.INFO, "Client " + this.ID + " received field update");

        this.field = field;
    }

    @Override
    public void receiveError(int errorId, String message) throws RemoteException {
        logger.log(Level.SEVERE, "Client " + this.ID + " received error: " + message);
    }

    @Override
    public void ping() throws RemoteException {

    }

    public boolean isAlive() {
        return field.getPlayer(this.ID).getCurHitPoints() > 0;
    }

    @Override
    public void run() {

        Random random = new Random();

        // send a connect message to the server
        try {
            server = (ServerInterface) Naming.lookup("rmi://localhost:" + Main.SERVER_PORT + "/FDDGServer/0");
            server.connect(this.ID);

            while(isAlive() && !field.gameHasFinished()) {
                Thread.sleep(1000);

                // check if there is a nearby player with hp < 50% to heal
                Dragon dragonToAttack;
                Player playerToHeal = field.isInRangeToHeal(this.ID);
                if(playerToHeal != null && playerToHeal.getCurHitPoints() > 0) {
                    server.heal(this.ID, playerToHeal.getUnitId());
                } else if((dragonToAttack = field.dragonIsInRangeToAttack(this.ID)) != null) {
                    server.attack(this.ID, dragonToAttack.getUnitId());
                } else {
                    Player p = field.getPlayer(this.ID);
                    int move = field.getDirectionToNearestDragon(p.getxPos(), p.getyPos());
                    if(move == -1 ) {
                        logger.log(Level.INFO, "Player" + this.ID + " couldn't move towards a dragon (blocked?)");
                        continue;
                    }
                    server.move(this.ID, move);
                }

                // server.move(this.ID, random.nextInt(4));
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
