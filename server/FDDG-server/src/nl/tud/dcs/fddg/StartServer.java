package nl.tud.dcs.fddg;


import nl.tud.dcs.fddg.server.ServerProcess;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;

/**
 * Starts a server with or without a GUI on the local machine
 * Created by Niels on 16-3-2015.
 */
public class StartServer {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: StartServer <servers file> <server ID> [GUI]");
            System.exit(1);
        }

        // parse arguments
        String serversFileName = args[0];
        int serverID = Integer.parseInt(args[1]);
        boolean useGUI = false;
        if (args.length > 2 && args[2].equals("GUI"))
            useGUI = true;

        // parse servers file
        String[] serverURLs = Files.readAllLines(Paths.get(serversFileName)).toArray()

        // create the server process, bind it to the registry and start it
        try {
            ServerProcess server = new ServerProcess(serverID, useGUI);
            Naming.rebind("FDDGServer/" + serverID, server);
            new Thread(server).start();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
