package nl.tud.dcs.fddg;

import nl.tud.dcs.fddg.client.ClientProcess;

import java.io.File;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StartClientsSimulation {

    private static Map<Integer, Thread> clientMap = new HashMap<Integer, Thread>();
    private static String[] serverURLs;
    private static Logger logger;

    public static void startClient(int logID) {
        try {
            ClientProcess client = new ClientProcess();
            client.selectServer(serverURLs, false);
            Thread clientThread = new Thread(client);
            clientThread.start();
            clientMap.put(logID, clientThread);
            Thread.sleep(250); // TODO we hardcoded a timeout here between connecting the players
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void stopClient(int logID) {
        Thread clientThread = clientMap.get(logID);
        clientThread.interrupt();
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.err.println("Usage: StartClientsSimulation <simulation file> <server file>");
            System.exit(1);
        }

        logger = Logger.getLogger(StartClientsSimulation.class.getName());
        logger.log(Level.INFO, "Starting client simulation");

        // parse servers file
        Scanner sc = new Scanner(new File(args[1]));
        int nrOfServers = sc.nextInt();
        sc.nextLine(); //skip whitespace
        serverURLs = new String[nrOfServers];
        for (int i = 0; i < nrOfServers; i++) {
            serverURLs[i] = sc.nextLine();
        }

        // parse simulation file
        logger.log(Level.INFO, "Connecting initial players to the server with timeout of 250 sec.");
        sc = new Scanner(new File(args[0]));
        int initialPlayers = sc.nextInt();
        for(int i = 0; i < initialPlayers; i++) {
            int logID = sc.nextInt();
            logger.log(Level.INFO, "Player with log ID " + logID + " connecting");
            startClient(logID);
        }

        // now we read the events
        int lastTimestamp = -1;
        Map<Integer, List<String>> eventMap = new HashMap<Integer, List<String>>();
        while(sc.hasNext()) {
            String line = sc.nextLine();
            if(line.equals("")) { continue; }
            String[] parts = line.split(" ");
            List<String> events = new ArrayList<String>();
            int timestamp = Integer.parseInt(parts[0]);
            if(timestamp > lastTimestamp) { lastTimestamp = timestamp; }
            for(int i = 1; i < parts.length; i++) {
                events.add(parts[i]);
            }
            eventMap.put(timestamp, events);
        }

        // start the simulation loop
        int time = 0;
        while(time != lastTimestamp) {

            if(eventMap.containsKey(time)) {
                // we should execute some event during this timestamp
                List<String> eventsToBeExecuted = eventMap.get(time);
                for(String event : eventsToBeExecuted) {
                    char cmd = event.charAt(0);
                    int playerId = Integer.parseInt(event.substring(1));
                    if(cmd == 'C') {
                        logger.log(Level.INFO, "Player with log ID " + playerId + " connecting");
                        startClient(playerId);
                    } else if(cmd == 'D') {
                        logger.log(Level.INFO, "Player with log ID " + playerId + " disconnecting");
                        stopClient(playerId);
                    }
                }
            }

            Thread.sleep(1000);
            time++;
        }


    }

}
