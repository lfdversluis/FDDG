package nl.tud.gameobjects;

import nl.tud.entities.Dragon;
import nl.tud.entities.Player;
import nl.tud.entities.Unit;

import java.io.Serializable;
import java.util.*;

/**
 * Created by martijndevos on 3/4/15.
 */
public class Field implements Serializable {
    private static final int BOARD_WIDTH = 25, BOARD_HEIGHT = 25;
    private static final int INITIAL_DRAGONS = 20;
    private static final int INITIAL_PLAYERS = 100;
    private Unit[][] entities;
    private int[] dx = { 0, 1, 0, -1 };
    private int[] dy = { -1, 0, 1, 0 };
    private HashSet<Integer> unitIds;
    private HashMap<Integer, Player> playerMap;
    private HashMap<Integer, Dragon> dragonMap;
    private Random random;

    public Field() {
        entities = new Unit[BOARD_HEIGHT][BOARD_WIDTH];
        unitIds = new HashSet<Integer>();
        playerMap = new HashMap<Integer, Player>();
        dragonMap = new HashMap<Integer, Dragon>();

        random = new Random(System.currentTimeMillis());

        // fill the field with dragons
        int randX, randY;
        for(int i = 0; i < INITIAL_DRAGONS; i++) {
            do {
                randX = random.nextInt(BOARD_WIDTH);
                randY = random.nextInt(BOARD_HEIGHT);
            } while (!isFree(randX, randY));

            Dragon d = new Dragon(randX, randY, getUniqueId());
            entities[randY][randX] = d;
            dragonMap.put(d.getUnitId(), d);
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
        return (newX >= 0 && newX < BOARD_WIDTH && newY >= 0 && newY < BOARD_HEIGHT && isFree(newX, newY));
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

    public int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public boolean isInRange(int thisPlayerId, int thatPlayerId, int range) {
        Player thisPlayer = playerMap.get(thisPlayerId);
        Player thatPlayer = playerMap.get(thatPlayerId);
        int distance = manhattanDistance(thisPlayer.getxPos(), thisPlayer.getyPos(), thatPlayer.getxPos(), thatPlayer.getyPos());
        return (distance <= range);
    }

    /**
     * This method returns a random player from a set of players that are eligible to heal.
     * We return a random player from this set to avoid overhealing of one player.
     * @param playerId
     * @return
     */
    public Player isInRangeToHeal(int playerId) {
        ArrayList<Player> eligible = new ArrayList<Player>();
        Player thisPlayer = playerMap.get(playerId);

        Iterator<Integer> it = playerMap.keySet().iterator();
        while(it.hasNext()) {
            Integer id = it.next();
            Player thatPlayer = playerMap.get(id);
            if(isInRange(playerId, id, 5) && thatPlayer.getCurHitPoints() < 0.5) { eligible.add(thatPlayer); }
        }

        if(eligible.size() == 0) { return null; }
        return eligible.get(new Random().nextInt(eligible.size()));
    }

    /**
     * This method returns a random dragon from a set of dragons that can be attacked.
     * @param playerId
     * @return
     */
    public Dragon dragonIsInRangeToAttack(int playerId) {
        ArrayList<Dragon> eligible = new ArrayList<Dragon>();
        Player thisPlayer = playerMap.get(playerId);

        Iterator<Integer> it = dragonMap.keySet().iterator();
        while(it.hasNext()) {
            Integer id = it.next();
            Dragon d = dragonMap.get(id);
            if(isInRange(playerId, id, 1)) { eligible.add(d); }
        }

        if(eligible.size() == 0) { return null; }
        return eligible.get(new Random().nextInt(eligible.size()));
    }

    public Player getPlayer(int playerId) {
        return playerMap.get(playerId);
    }

    public void removePlayer(int playerId) {
        Player p = playerMap.get(playerId);
        entities[p.getyPos()][p.getxPos()] = null;
        playerMap.remove(playerId);
    }

    public Dragon getDragon(int dragonId) {
        return dragonMap.get(dragonId);
    }

    public void removeDragon(int dragonId) {
        Dragon d = dragonMap.get(dragonId);
        entities[d.getyPos()][d.getxPos()] = null;
        dragonMap.remove(dragonId);
    }

    public Set<Integer> getPath(int startX, int startY, int endX, int endY) {

    }
}
