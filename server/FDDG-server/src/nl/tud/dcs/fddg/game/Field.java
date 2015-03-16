package nl.tud.dcs.fddg.game;

import nl.tud.dcs.fddg.game.entities.Dragon;
import nl.tud.dcs.fddg.game.entities.Player;
import nl.tud.dcs.fddg.game.entities.Unit;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Field implements Serializable {
    public static final int BOARD_WIDTH = 25, BOARD_HEIGHT = 25;
    private static final int INITIAL_DRAGONS = 20;
    private Unit[][] entities;
    private int[] dx = { 0, 1, 0, -1 };
    private int[] dy = { -1, 0, 1, 0 };
    private HashSet<Integer> unitIds;
    private ConcurrentHashMap<Integer, Player> playerMap;
    private ConcurrentHashMap<Integer, Dragon> dragonMap;
    private Random random;

    public Field() {
        entities = new Unit[BOARD_HEIGHT][BOARD_WIDTH];
        unitIds = new HashSet<Integer>();
        playerMap = new ConcurrentHashMap<>();
        dragonMap = new ConcurrentHashMap<>();

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
        int uniqueId = random.nextInt(Integer.MAX_VALUE);
        while(unitIds.contains(uniqueId)) { uniqueId = random.nextInt(Integer.MAX_VALUE); }
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
        playerMap.put(playerId, p);
        entities[randY][randX] = p;
    }

    public boolean isValidPlayerId(int playerId) {
        return playerMap.containsKey(playerId);
    }

    private boolean canMove(int newX, int newY) {
        return (newX >= 0 && newX < BOARD_WIDTH && newY >= 0 && newY < BOARD_HEIGHT && isFree(newX, newY));
    }

    public boolean movePlayer(int playerId, int x, int y) {
        Player p = playerMap.get(playerId);

        if(!canMove(x, y)) {
            return false;
        }

        // move the player
        entities[p.getyPos()][p.getxPos()] = null;
        entities[y][x] = p;
        p.setxPos(x); p.setyPos(y);

        return true;
    }

    public Unit getUnit(int x, int y) {
        return entities[y][x];
    }

    public int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public boolean isInRange(int thisPlayerId, int thatUnitId, int range) {
        Player thisPlayer = playerMap.get(thisPlayerId);
        Unit thatPlayer;
        if(playerMap.containsKey(thatUnitId)){
            thatPlayer = playerMap.get(thatUnitId);
        } else {
            thatPlayer = dragonMap.get(thatUnitId);
        }

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
        ArrayList<Player> eligible = new ArrayList<>();

        Iterator<Integer> it = playerMap.keySet().iterator();
        while(it.hasNext()) {
            Integer id = it.next();
            Player thatPlayer = playerMap.get(id);
            if(isInRange(playerId, id, 5) && thatPlayer.getHitPointsPercentage() < 0.5) { eligible.add(thatPlayer); }
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

    public Dragon getDragon(int dragonId) {
        return dragonMap.get(dragonId);
    }

    public void removeDragon(int dragonId) {
        Dragon d = dragonMap.get(dragonId);
        entities[d.getyPos()][d.getxPos()] = null;
        dragonMap.remove(dragonId);
    }

    public int getDirectionToNearestDragon(int startX, int startY) {
        HashMap<Integer, Integer> stepMap = new HashMap<>();
        final int MAX_WIDTH_HEIGHT = Math.max(BOARD_HEIGHT, BOARD_WIDTH) + 5;
        int curPos = startX + startY * MAX_WIDTH_HEIGHT;
        State s = new State(curPos, new ArrayList<Integer>(), 0);

        Queue<State> queue = new LinkedList<>();
        queue.add(s);

        stepMap.put(curPos, 0);

        while(!queue.isEmpty()){
            State curState = queue.poll();
            ArrayList<Integer> path = curState.passed;
            int position = curState.curPos;
            int curX = position % MAX_WIDTH_HEIGHT;
            int curY = position / MAX_WIDTH_HEIGHT;
            int curSteps = curState.steps;

            for(int i=0; i<dx.length; i++){
                int newX = curX + dx[i];
                int newY = curY + dy[i];

                if(newX >= 0 && newX < BOARD_WIDTH && newY >= 0 && newY < BOARD_HEIGHT && (entities[newY][newX] instanceof Dragon)){
                    return path.get(0);
                }

                if(canMove(newX, newY)){
                    int newPos = newX + newY * MAX_WIDTH_HEIGHT;
                    if(!stepMap.containsKey(newPos) || (curSteps + 1 ) < stepMap.get(newPos) ){
                        // Make a copy (hard copy, no reference)
                        ArrayList<Integer> updatedPath = new ArrayList<>(path);
                        updatedPath.add(newPos);
                        stepMap.put(newPos, curSteps+1);
                        queue.add(new State(newPos, updatedPath, curSteps+1));
                    }
                }
            }
        }

        // No path possible, return -1
        return -1;
    }

    public void dragonRage() {
        for(int dragonId : dragonMap.keySet()){
            Dragon d = dragonMap.get(dragonId);

            int dragonX = d.getxPos();
            int dragonY = d.getyPos();

            for(int i=0; i<4; i++){
                int unitX = dragonX + dx[i];
                int unitY = dragonY + dy[i];

                if(unitX >= 0 && unitX < BOARD_WIDTH && unitY >= 0 && unitY < BOARD_HEIGHT && entities[unitY][unitX] instanceof Player){
                    Player p = (Player) entities[unitY][unitX];
                    p.setCurHitPoints(p.getCurHitPoints() - d.getAttackPower());

                    if(p.getCurHitPoints() <= 0){
                        entities[p.getyPos()][p.getxPos()] = null;
                    }
                }
            }
        }
    }

    public boolean gameHasFinished() {
        if(dragonMap.size() == 0) {
            return true;
        }

        for(int playerId : playerMap.keySet()) {
            if(playerMap.get(playerId).getCurHitPoints() > 0) {
                return false;
            }
        }

        return true;
    }
}

class State{

    int curPos, steps;
    ArrayList<Integer> passed;

    public State(int i, ArrayList<Integer> set, int steps){
        this.curPos = i;
        this.passed = set;
        this.steps = steps;
    }
}