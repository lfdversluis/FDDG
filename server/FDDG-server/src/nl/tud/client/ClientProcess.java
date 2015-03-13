package nl.tud.client;

import nl.tud.Main;
import nl.tud.ServerInterface;
import nl.tud.entities.Dragon;
import nl.tud.entities.Player;
import nl.tud.gameobjects.*;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
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

    public ClientProcess(int id) throws RemoteException, AlreadyBoundException, MalformedURLException {
        this.ID = id;
        this.logger = Logger.getLogger(ClientProcess.class.getName());

        logger.log(Level.INFO, "Starting client with id " + id);

        java.rmi.Naming.bind("rmi://localhost:" + Main.SERVER_PORT + "/FDDGClient/" + id, this);
    }

    @Override
    public void initializeField(Field field) throws RemoteException {
        this.field = field;
    }

    @Override
    public synchronized void ack (Action action) throws RemoteException {
        // logger.log(Level.INFO, "Received  " + this.ID + " received field update");

        if(action instanceof AddPlayerAction) {
            AddPlayerAction apa = (AddPlayerAction) action;
            field.addPlayer(apa.getPlayerId(), apa.getX(), apa.getY());
        } else if (action instanceof AttackAction) {
            AttackAction ata = (AttackAction) action;
            Dragon d = field.getDragon(ata.getDragonId());
            Player p = field.getPlayer(ata.getSenderId());
            d.setCurHitPoints(d.getCurHitPoints() - p.getAttackPower());
        } else if (action instanceof  DeleteUnitAction) {
            DeleteUnitAction dua = (DeleteUnitAction) action;
            if(field.getDragon(dua.getUnitId()) == null){
                Player p = field.getPlayer(dua.getUnitId());
                field.removePlayer(p.getUnitId());
            } else {
                Dragon d = field.getDragon(dua.getUnitId());
                field.removeDragon(d.getUnitId());
            }
        } else if(action instanceof HealAction){
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
        }

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

        // send a connect message to the server
        try {
            server = (ServerInterface) Naming.lookup("rmi://localhost:" + Main.SERVER_PORT + "/FDDGServer/0");
            server.connect(this.ID);

            while(isAlive() && !field.gameHasFinished()) {
                Thread.sleep(1000);

                // check if there is a nearby player with hp < 50% to heal
                Dragon dragonToAttack;
                Player playerToHeal = field.isInRangeToHeal(this.ID);
                if(playerToHeal != null) {
                    server.performAction(new HealAction(this.ID, playerToHeal.getUnitId()));
                } else if((dragonToAttack = field.dragonIsInRangeToAttack(this.ID)) != null) {
                    server.performAction(new AttackAction(this.ID, dragonToAttack.getUnitId()));
                } else {
                    Player p = field.getPlayer(this.ID);
                    int move = field.getDirectionToNearestDragon(p.getxPos(), p.getyPos());
                    if(move == -1) {
                        logger.log(Level.INFO, "Player" + this.ID + " can't move towards any dragon)");
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
}
