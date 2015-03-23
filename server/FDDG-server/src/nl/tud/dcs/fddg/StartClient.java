package nl.tud.dcs.fddg;

import nl.tud.dcs.fddg.client.ClientProcess;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Scanner;

/**
 * Start one or more clients on the local machine
 * Created by Niels on 16-3-2015.
 */
public class StartClient {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: StartClient <server file>");
            System.exit(1);
        }

        // parse arguments
        String serversFileName = args[0];

        // parse servers file
        Scanner sc = new Scanner(new File(serversFileName));
        int nrOfServers = sc.nextInt();
        sc.nextLine(); //skip whitespace
        String[] serverURLs = new String[nrOfServers];
        for (int i = 0; i < nrOfServers; i++) {
            serverURLs[i] = sc.nextLine();
        }

        System.out.println("Starting a client on host: "+InetAddress.getLocalHost().getHostAddress());

        // create the client process, bind it to the registry and start it
        try {
            ClientProcess client = new ClientProcess();
            client.selectServer(serverURLs, false);
            new Thread(client).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
