package nl.tud.dcs.fddg;

import nl.tud.dcs.fddg.client.ClientProcess;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Scanner;

/**
 * Start one or more clients on the local machine
 * Created by Niels on 16-3-2015.
 */
public class StartClients {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: StartClients <first client ID> <last client ID> <server file>");
            System.exit(1);
        }

        // parse arguments
        int firstID = Integer.parseInt(args[0]);
        int lastID = Integer.parseInt(args[1]);
        String serversFileName = args[2];

        // parse servers file
        Scanner sc = new Scanner(new File(serversFileName));
        int nrOfServers = sc.nextInt();
        sc.nextLine(); //skip whitespace
        String[] serverURLs = new String[nrOfServers];
        for (int i = 0; i < nrOfServers; i++) {
            serverURLs[i] = sc.nextLine();
        }

        System.out.println("Starting " + (lastID - firstID + 1) + " clients...");

        // create the client processes, bind them to the registry and start them
        try {
            for (int id = firstID; id <= lastID; id++) {
                ClientProcess client = new ClientProcess();
                client.selectServer(serverURLs);
                new Thread(client).start();
                Thread.sleep(250); // TODO we hardcoded a timeout here between connecting the players
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
