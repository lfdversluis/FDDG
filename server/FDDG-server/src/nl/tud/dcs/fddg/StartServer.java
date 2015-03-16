package nl.tud.dcs.fddg;


import nl.tud.dcs.fddg.server.ServerProcess;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Starts a server with or without a GUI on the local machine
 * Created by Niels on 16-3-2015.
 */
public class StartServer {

    private static final int REGISTRY_PORT = 1099;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: StartServer <server ID> [GUI]");
            System.exit(1);
        }

        // parse arguments
        int serverID = Integer.parseInt(args[0]);
        boolean useGUI = false;
        if(args.length > 1 && args[1].equals("GUI"))
            useGUI = true;

        // make sure the RMI registry is online
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(REGISTRY_PORT);
        } catch (RemoteException e) {
            // registry does not exist yet, so create it
            System.out.println("RMI registry not online, starting it now");
            try {
                registry = LocateRegistry.createRegistry(REGISTRY_PORT);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }

        // create the server process, bind it to the registry and start it
        try {
            ServerProcess server = new ServerProcess(serverID, useGUI);
            registry.bind("FDDGServer/" + serverID, server);
            new Thread(server).start();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }
}
