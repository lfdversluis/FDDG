package nl.tud;

import nl.tud.client.ClientProcess;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class Main {

    public static final int NUM_SERVERS = 1;
    public static final int SERVER_PORT = 6447;

    public static void main(String[] args) {
        ArrayList<ServerProcess> processes = new ArrayList<ServerProcess>(NUM_SERVERS);

        try {
            java.rmi.registry.LocateRegistry.createRegistry(SERVER_PORT);

            // create the server processes
            for(int i=0; i<NUM_SERVERS; i++) {
                    ServerProcess process = new ServerProcess(i, NUM_SERVERS);
                    processes.add(process);
                    new Thread(process).start();
            }

            // create a client after a small delay
            Thread.sleep(2000);
            ClientProcess client = new ClientProcess(1234);
            new Thread(client).start();

        } catch (RemoteException e){
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}
