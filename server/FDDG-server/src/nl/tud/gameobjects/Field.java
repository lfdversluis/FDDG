package nl.tud.gameobjects;

import nl.tud.entities.Dragon;
import nl.tud.entities.Player;
import nl.tud.entities.Unit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by martijndevos on 3/4/15.
 */
public class Field {
    private static final int BOARD_WIDTH = 25, BOARD_HEIGHT = 25;
    private static final int INITIAL_DRAGONS = 20;
    private static final int INITIAL_PLAYERS = 100;
    private Unit[][] entities;
    private int[] dx = { 0, 1, 0, -1 };
    private int[] dy = { -1, 0, 1, 0 };
    private HashSet<Integer> unitIds;
    private HashMap<Integer, Player> playerMap;
    private Random random;

    public Field() {
        entities = new Unit[BOARD_HEIGHT][BOARD_WIDTH];
        unitIds = new HashSet<Integer>();
        playerMap = new HashMap<Integer, Player>();

        random = new Random(System.currentTimeMillis());

        // fill the field with dragons
        int randX, randY;
        for(int i = 0; i < INITIAL_DRAGONS; i++) {
            do {
                randX = random.nextInt(BOARD_WIDTH);
                randY = random.nextInt(BOARD_HEIGHT);
            } while (!isFree(randX, randY));

            entities[randY][randX] = new Dragon(randX, randY, getUniqueId());
        }
    }

    private boolean isFree(int x, int y) {
        return entities[y][x] == null;
    }

    private int getUniqueId() {
        Random random = new Random(System.currentTimeMillis());
        int uniqueId = random.nextInt();
        while(unitIds.contains(uniqueId)) { uniqueId = random.nextInt(); }
        unitIds.add(uniqueId);
        return uniqueId;
    }

    public void addPlayer(int playerId) {
        int randX, randY;
        do {
            randX = random.nextInt(BOARD_WIDTH);
            randY = random.nextInt(BOARD_HEIGHT);
        } while (!isFree(randX, randY));

        Player p = new Player(randX, randY, playerId);
        playerMap.put(p.getUnitId(), p);
        entities[randY][randX] = p;
    }

    public boolean isValidPlayerId(int playerId) {
        return playerMap.containsKey(playerId);
    }

    private boolean canMove(int newX, int newY) {
        return (isFree(newX, newY) && newX >= 0 && newX < BOARD_WIDTH && newY >= 0 && newY < BOARD_HEIGHT);
    }

    public boolean movePlayer(int playerId, int direction) {
        Player p = playerMap.get(playerId);
        int newX = p.getxPos() + dx[direction];
        int newY = p.getyPos() + dy[direction];

        if(!canMove(newX, newY)) {
            return false;
        }

        // move the player
        entities[p.getyPos()][p.getxPos()] = null;
        entities[newY][newX] = p;
        p.setxPos(newX); p.setyPos(newY);

        return true;
    }
}
