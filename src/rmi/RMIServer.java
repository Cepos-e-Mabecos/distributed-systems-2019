package rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import places.PlaceManager;
import places.PlacesListInterface;

public class RMIServer {
  /*
   * Attributes
   */
  private Integer port;

  /*
   * Constructor
   */

  public RMIServer(Integer port) {
    this.port = port;
  }

  /*
   * Starts a PlaceManager server on class port
   */
  public void startPlaceManagerServer() {
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
