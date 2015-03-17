package nl.tud.dcs.fddg;

import nl.tud.dcs.fddg.client.ClientProcess;
import nl.tud.dcs.fddg.server.ServerProcess;
import nl.tud.dcs.fddg.util.RMI_Util;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Start a client on the local machine
 * Created by Niels on 16-3-2015.
 */
public class StartClient {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: StartClient <client ID>");
            System.exit(1);
        }

        // parse arguments
        int clientID = Integer.parseInt(args[0]);

        // create the client process, bind it to the registry and start it
        try {
            ClientProcess client = new ClientProcess(clientID);
            Naming.rebind("FDDGClient/" + clientID, client);
            new Thread(client).start();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
