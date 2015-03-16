package nl.tud.dcs.fddg;


import nl.tud.dcs.fddg.server.ServerProcess;
import nl.tud.dcs.fddg.util.RMI_Util;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Starts a server with or without a GUI on the local machine
 * Created by Niels on 16-3-2015.
 */
public class StartServer {

    public static void main(String[] args)  {
        if (args.length < 1) {
            System.err.println("Usage: StartServer <server ID> [GUI]");
            System.exit(1);
        }

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        // parse arguments
        int serverID = Integer.parseInt(args[0]);
        boolean useGUI = false;
        if(args.length > 1 && args[1].equals("GUI"))
            useGUI = true;

        // make sure the RMI registry is online
//        try {
//            LocateRegistry.createRegistry(1099);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//        RMI_Util.getLocalRegistry();

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
