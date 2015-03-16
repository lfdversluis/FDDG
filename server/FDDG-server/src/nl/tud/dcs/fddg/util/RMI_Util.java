package nl.tud.dcs.fddg.util;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Class with utility functions for setting up the RMI registry
 * Created by Niels on 16-3-2015.
 */
public class RMI_Util {

    private static final int REGISTRY_PORT = 1099;

    /**
     * Tries to get the local RMI registry. When this fails, the registry is created and started
     *
     * @return a reference to the local RMI registry
     */
    public static Registry getLocalRegistry() {
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(REGISTRY_PORT);
        } catch (RemoteException e) {
            // registry does not exist yet, so create it
            System.out.println("Local RMI registry not online, starting it now");
            try {
                registry = LocateRegistry.createRegistry(REGISTRY_PORT);
            } catch (RemoteException e1) {
                e1.printStackTrace();
                System.exit(1);
            }
        }
        return registry;
    }

}
