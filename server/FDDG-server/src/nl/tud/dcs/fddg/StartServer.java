package nl.tud.dcs.fddg;


import nl.tud.dcs.fddg.server.ServerProcess;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

/**
 * Starts a server with or without a GUI on the local machine
 * Created by Niels on 16-3-2015.
 */
public class StartServer {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: StartServer <server ID> [GUI]");
            System.exit(1);
        }

        // parse arguments
        int serverID = Integer.parseInt(args[0]);
        boolean useGUI = false;
        if (args.length > 1 && args[1].equals("GUI"))
            useGUI = true;

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
