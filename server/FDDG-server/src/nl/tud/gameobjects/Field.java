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

    public Field() {
        entities = new Unit[BOARD_HEIGHT][BOARD_WIDTH];
        unitIds = new HashSet<Integer>();

        Random random = new Random(System.currentTimeMillis());

        // fill the field with dragons
        int randX, randY;
        for(int i = 0; i < INITIAL_DRAGONS; i++) {
            do {
                randX = random.nextInt(BOARD_WIDTH);
                randY = random.nextInt(BOARD_HEIGHT);
            } while (isFree(randX, randY));

            entities[randY][randX] = new Dragon(randX, randY, getUniqueId());
        }

        // fill the field with players
        for(int i = 0; i < INITIAL_DRAGONS; i++) {
            do {
                randX = random.nextInt(BOARD_WIDTH);
                randY = random.nextInt(BOARD_HEIGHT);
            } while (isFree(randX, randY));

            Player p = new Player(randX, randY, getUniqueId());
            playerMap.put(p.getUnitId(), p);
            entities[randY][randX] = p;
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

    /**
     * This methods moves a player to a new direction.
     * @param playerId
     * @param direction 0 = top, 1 = right, 2 = bottom, 3 = left
     * @return a boolean indicating whether the move action was successful
     */
    private boolean movePlayer(int playerId, int direction) {
        // TODO: check whether new position is not occupied
        Player p = playerMap.get(playerId);
        int oldX = p.getxPos(); int oldY = p.getyPos();
        p.setxPos(oldX + dx[direction]);
        p.setyPos(oldY + dy[direction]);
        return true;
    }
}
