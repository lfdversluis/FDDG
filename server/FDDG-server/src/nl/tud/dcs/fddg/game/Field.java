package nl.tud.dcs.fddg.game;

import nl.tud.dcs.fddg.game.actions.Action;
import nl.tud.dcs.fddg.game.actions.DamageAction;
import nl.tud.dcs.fddg.game.actions.DeleteUnitAction;
import nl.tud.dcs.fddg.game.entities.Dragon;
import nl.tud.dcs.fddg.game.entities.Player;
import nl.tud.dcs.fddg.game.entities.Unit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Field implements Serializable {
    public static final int BOARD_WIDTH = 25, BOARD_HEIGHT = 25;
    private Unit[][] entities;
    private int[] dx = {0, 1, 0, -1};
    private int[] dy = {-1, 0, 1, 0};
    private HashSet<Integer> unitIds;
    private ConcurrentHashMap<Integer, Player> playerMap;
    private ConcurrentHashMap<Integer, Dragon> dragonMap;
    private Random random;

    /**
     * The constructor of the Field class.
     * Sets up the field and creates maps to keep track of the players and dragon
     * in the game.
     */
    public Field(String filename) throws FileNotFoundException {
        entities = new Unit[BOARD_HEIGHT][BOARD_WIDTH];
        unitIds = new HashSet<Integer>();
        playerMap = new ConcurrentHashMap<Integer, Player>();
        dragonMap = new ConcurrentHashMap<Integer, Dragon>();

        random = new Random(System.currentTimeMillis());

        readFromFile(filename);
    }

    private void readFromFile(String filename) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filename));
        int dragonCounter = 0;
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                String next = sc.next();
                if (next.equals("D")) {
                    Dragon dragon = new Dragon(x, y, dragonCounter + 10000);
                    entities[y][x] = dragon;
                    dragonMap.put(dragon.getUnitId(), dragon);
                    dragonCounter++;
                }
            }
        }

    }


    /**
     * This function checks if a given location is not occupied at the moment.
     *
     * @param x The x coordinate of the location.
     * @param y The y coordinate of the location.
     * @return Returns a boolean indicating if the location is unoccupied.
     */
    public boolean isFree(int x, int y) {
        return entities[y][x] == null;
    }

    /**
     * This function generates a new ID to be used.
     *
     * @return A unique ID.
     */
    private int getUniqueId() {
        Random random = new Random(System.currentTimeMillis());
        int uniqueId = random.nextInt(Integer.MAX_VALUE);
        while (unitIds.contains(uniqueId)) {
            uniqueId = random.nextInt(Integer.MAX_VALUE);
        }
        unitIds.add(uniqueId);
        return uniqueId;
    }

    /**
     * This function places a Player randomly on the field.
     *
     * @param playerId The (unique) ID of the player to be placed on the field.
     */
    public void addPlayer(int playerId) {
        int randX, randY;
        do {
            randX = random.nextInt(BOARD_WIDTH);
            randY = random.nextInt(BOARD_HEIGHT);
        } while (!isFree(randX, randY));

        Player p = new Player(randX, randY, playerId);
        playerMap.put(playerId, p);
        entities[randY][randX] = p;
    }

    /**
     * This function places a Player on the field
     * @param newPlayer The new player to be added.
     */
    public void addPlayer(Player newPlayer) {
        playerMap.put(newPlayer.getUnitId(), newPlayer);
        entities[newPlayer.getyPos()][newPlayer.getxPos()] = newPlayer;
    }

    /**
     * This function checks if a player ID belongs indeed to a player on the field.
     *
     * @param playerId The ID of the player to be checked.
     * @return A boolean indicating if the ID is valid and present on the field.
     */
    public boolean isValidPlayerId(int playerId) {
        return playerMap.containsKey(playerId);
    }

    /**
     * This function checks if a move action to a new location can be done.
     *
     * @param newX The x coordinate of the new location.
     * @param newY The y coordinate of the new location.
     * @return A boolean indicating if the move can be done.
     */
    public boolean canMove(int newX, int newY) {
        return isInBoard(newX, newY) && isFree(newX, newY);
    }

    /**
     * This function moves a player to a new location.
     *
     * @param playerId The (unique) ID of the player to be moved.
     * @param x        The x coordinate of the location the player wants to move to.
     * @param y        The y coordinate of the location the player wants to move to.
     * @return A boolean indicating if a player can move to the location provided.
     */
    public boolean movePlayer(int playerId, int x, int y) {
        Player p = playerMap.get(playerId);

        // TODO we forgot something... apparently the player can move anywhere it likes. No distance = 1 check?!

        if (p == null || !canMove(x, y)) {
            return false;
        }

        // move the player
        entities[p.getyPos()][p.getxPos()] = null;
        entities[y][x] = p;
        p.setxPos(x);
        p.setyPos(y);

        return true;
    }

    /**
     * Simple getter function that returns the unit (might be null) on a position in the field.
     * Null means the location is not occupied by a unit.
     *
     * @param x The x position on the field.
     * @param y The y position on the field.
     * @return A unit (might be null) that occupies this location on the field.
     */
    public Unit getUnit(int x, int y) {
        return entities[y][x];
    }

    public int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public boolean isInRange(int thisPlayerId, int thatUnitId, int range) {
        Player thisPlayer = playerMap.get(thisPlayerId);
        Unit thatUnit;
        if (playerMap.containsKey(thatUnitId)) {
            thatUnit = playerMap.get(thatUnitId);
        } else if (dragonMap.containsKey(thatUnitId)) {
            thatUnit = dragonMap.get(thatUnitId);
        } else {
            return false;
        }

        int distance = manhattanDistance(thisPlayer.getxPos(), thisPlayer.getyPos(), thatUnit.getxPos(), thatUnit.getyPos());
        return (distance <= range);
    }

    /**
     * This method returns a random player from a set of players that are eligible to heal.
     * We return a random player from this set to avoid overhealing of one player.
     *
     * @param playerId The ID of the player to heal.
     * @return The player that is in range to heal (can be null).
     */
    public Player isInRangeToHeal(int playerId) {
        ArrayList<Player> eligible = new ArrayList<Player>();

        Iterator<Integer> it = playerMap.keySet().iterator();
        while (it.hasNext()) {
            Integer id = it.next();
            Player thatPlayer = playerMap.get(id);
            if (isInRange(playerId, id, 5) && thatPlayer.getHitPointsPercentage() < 0.5 && thatPlayer.getCurHitPoints() > 0) {
                eligible.add(thatPlayer);
            }
        }

        if (eligible.size() == 0) {
            return null;
        }
        return eligible.get(new Random().nextInt(eligible.size()));
    }

    /**
     * This method returns a random dragon from a set of dragons that can be attacked.
     * Null indicates no dragon is in range.
     *
     * @param playerId The (unique) ID of the player that wishes to attack a dragon.
     * @return A dragon that the player can attack (might be null).
     */
    public Dragon dragonIsInRangeToAttack(int playerId) {
        ArrayList<Dragon> eligible = new ArrayList<Dragon>();

        Iterator<Integer> it = dragonMap.keySet().iterator();
        while (it.hasNext()) {
            Integer id = it.next();
            Dragon d = dragonMap.get(id);
            if (isInRange(playerId, id, 1)) {
                eligible.add(d);
            }
        }

        if (eligible.size() == 0) {
            return null;
        }
        return eligible.get(new Random().nextInt(eligible.size()));
    }

    /**
     * This function returns a player object on the field based on its ID.
     *
     * @param playerId The (unique) ID of the player object.
     * @return The player object that belongs to the ID.
     */
    // TODO Check if map contains?
    public Player getPlayer(int playerId) {
        return playerMap.get(playerId);
    }

    /**
     * This function returns a dragon object on the field based on its ID.
     *
     * @param dragonId The (unique) ID of the dragon object.
     * @return The dragon object that belongs to the ID.
     */
    // TODO Check if map contains?
    public Dragon getDragon(int dragonId) {
        return dragonMap.get(dragonId);
    }

    /**
     * This function removes a dragon from the field.
     * Called when it's dead.
     *
     * @param dragonId The (unique) ID of the dragon to be deleted.
     */
    public void removeDragon(int dragonId) {
        Dragon d = dragonMap.get(dragonId);
        entities[d.getyPos()][d.getxPos()] = null;
        dragonMap.remove(dragonId);
    }

    /**
     * This function computes the location to go to when heading for the next nearest
     * dragon on the field based on a start position (the current place of the player).
     *
     * @param startX The x coordinate of the start position.
     * @param startY The y coordinate of the start position.
     * @return An integer in which the x and y location of the next location are encoded.
     * The encode is as follows: the x coordinate of the new position + (Math.max(BOARD_HEIGHT, BOARD_WIDTH) + 5) * the y coordinate.
     * -1 if no next step is possible.
     */
    public int getDirectionToNearestDragon(int startX, int startY) {
        HashMap<Integer, Integer> stepMap = new HashMap<Integer, Integer>();
        final int MAX_WIDTH_HEIGHT = Math.max(BOARD_HEIGHT, BOARD_WIDTH) + 5;
        int curPos = startX + startY * MAX_WIDTH_HEIGHT;
        State s = new State(curPos, new ArrayList<Integer>(), 0);

        Queue<State> queue = new LinkedList<State>();
        queue.add(s);

        stepMap.put(curPos, 0);

        while (!queue.isEmpty()) {
            State curState = queue.poll();
            ArrayList<Integer> path = curState.passed;
            int position = curState.curPos;
            int curX = position % MAX_WIDTH_HEIGHT;
            int curY = position / MAX_WIDTH_HEIGHT;
            int curSteps = curState.steps;

            for (int i = 0; i < dx.length; i++) {
                int newX = curX + dx[i];
                int newY = curY + dy[i];

                if (isInBoard(newX, newY) && (entities[newY][newX] instanceof Dragon)) {
                    return path.get(0);
                }

                if (canMove(newX, newY)) {
                    int newPos = newX + newY * MAX_WIDTH_HEIGHT;
                    if (!stepMap.containsKey(newPos) || (curSteps + 1) < stepMap.get(newPos)) {
                        // Make a copy (hard copy, no reference)
                        ArrayList<Integer> updatedPath = new ArrayList<Integer>(path);
                        updatedPath.add(newPos);
                        stepMap.put(newPos, curSteps + 1);
                        queue.add(new State(newPos, updatedPath, curSteps + 1));
                    }
                }
            }
        }

        // No path possible, return -1
        return -1;
    }

    /**
     * Checks whether (x,y) is a valid location in the board
     *
     * @param x
     * @param y
     * @return
     */
    private boolean isInBoard(int x, int y) {
        return x >= 0 && x < BOARD_WIDTH && y >= 0 && y < BOARD_HEIGHT;
    }

    /**
     * This function lets all dragon alive on the field attack nearby players (that are connected to this server).
     */
    public Set<Action> dragonRage(Set<Integer> connectedPlayers) {
        Set<Action> actionSet = new HashSet<Action>();
        Set<Integer> playersToDelete = new HashSet<Integer>();

        for (int dragonId : dragonMap.keySet()) {
            Dragon d = dragonMap.get(dragonId);

            for (int playerId : connectedPlayers) {
                Player p = playerMap.get(playerId);

                if(isInRange(playerId, dragonId, 2) && !playersToDelete.contains(playerId)) {
                    if (p.getCurHitPoints() - d.getAttackPower() <= 0 && p.getCurHitPoints() > 0) {
                        p.setCurHitPoints(p.getCurHitPoints() - d.getAttackPower());
                        DeleteUnitAction dua = new DeleteUnitAction(p.getUnitId());
                        actionSet.add(dua);
                        playersToDelete.add(p.getUnitId());
                        removePlayer(p.getUnitId());
                    } else {
                        p.setCurHitPoints(p.getCurHitPoints() - d.getAttackPower());
                        DamageAction da = new DamageAction(p.getUnitId(), d.getAttackPower());
                        actionSet.add(da);
                    }
                }
            }
        }
        return actionSet;
    }

    public void removePlayer(int playerId) {
        Player p = getPlayer(playerId);
        entities[p.getyPos()][p.getxPos()] = null;
    }

    /**
     * This function checks if the game has been finished yet.
     *
     * @return A boolean indicating if the game has finished.
     */
    public boolean gameHasFinished() {
        if (dragonMap.size() == 0) {
            return true;
        }

        for (int playerId : playerMap.keySet()) {
            if (playerMap.get(playerId).getCurHitPoints() > 0) {
                return false;
            }
        }
        return true;
    }
}

/**
 * An inner class representing a state in the Breadth-first Search algorithm to determine the next location to go to in the {@link Field#getDirectionToNearestDragon(int, int)} function.
 */
class State {

    int curPos, steps;
    ArrayList<Integer> passed;

    public State(int i, ArrayList<Integer> set, int steps) {
        this.curPos = i;
        this.passed = set;
        this.steps = steps;
    }
}