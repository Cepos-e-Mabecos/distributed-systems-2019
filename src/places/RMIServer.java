package places;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
  public static void main(String[] args) {

    Integer port = Integer.parseInt(args[0]);
    Registry r = null;

    try {
      System.out.println("Creating registry on port: " + port);
      r = LocateRegistry.createRegistry(port);
    } catch (RemoteException a) {
      System.out.println(port + " is already used.");
      try {
        System.out.println("Getting registry on port: " + port);
        r = LocateRegistry.getRegistry(port);
      } catch (NumberFormatException | RemoteException e) {
        System.out.println("Error getting registry of port: " + port);
      }
    }

    try {
      PlacesListInterface placeList = new PlaceManager();
      r.rebind("placelist", placeList);

      System.out.println("PlaceManager running on port: " + port);
    } catch (Exception e) {
      System.out.println("Error trying to run PlaceManager on port: " + port);
    }
  }
}
