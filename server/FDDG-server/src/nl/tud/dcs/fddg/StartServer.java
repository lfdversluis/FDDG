package nl.tud.dcs.fddg;


import nl.tud.dcs.fddg.server.ServerProcess;

import java.io.File;
import java.util.Scanner;

/**
 * Starts a server with or without a GUI on the local machine
 * Created by Niels on 16-3-2015.
 */
public class StartServer {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: StartServer <servers file> <field file> <server ID> [GUI]");
            System.exit(1);
        }

        // parse arguments
        String serversFileName = args[0];
        String fieldFile = args[1];
        int serverID = Integer.parseInt(args[2]);
        boolean useGUI = false;
        if (args.length > 3 && args[3].equals("GUI"))
            useGUI = true;

        // parse servers file
        Scanner sc = new Scanner(new File(serversFileName));
        int nrOfServers = sc.nextInt();
        sc.nextLine(); //skip whitespace
        String[] serverURLs = new String[nrOfServers];
        for (int i = 0; i < nrOfServers; i++) {
            serverURLs[i] = sc.nextLine();
        }

        // create the server process, bind it to the registry and start it
        ServerProcess server = new ServerProcess(serverID, useGUI, fieldFile);
        server.registerAndConnectToAll(serverURLs);
        new Thread(server).start();
    }
}
