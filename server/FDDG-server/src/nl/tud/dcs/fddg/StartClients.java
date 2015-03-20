package nl.tud.dcs.fddg;

import nl.tud.dcs.fddg.client.ClientProcess;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

/**
 * Start one or more clients on the local machine
 * Created by Niels on 16-3-2015.
 */
public class StartClients {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: StartClients <first client ID> <last client ID>");
            System.exit(1);
        }

        // parse arguments
        int firstID = Integer.parseInt(args[0]);
        int lastID = Integer.parseInt(args[1]);

        System.out.println("Starting " + (lastID - firstID + 1) + " clients...");

        // create the client processes, bind them to the registry and start them
        try {
            for (int id = firstID; id <= lastID; id++) {
                ClientProcess client = new ClientProcess();
                new Thread(client).start();
                Thread.sleep(250); // TODO we hardcoded a timeout here between connecting the players
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
