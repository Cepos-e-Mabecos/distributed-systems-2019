package main;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import places.PlaceManager;

public class RMIServer {
  public static void main(String[] args) throws RemoteException, UnknownHostException {
    String multicastAddress = args[0];
    Integer multicastPort = Integer.parseInt(args[1]);
    Integer thisReplicaPort = Integer.parseInt(args[2]);

    PlaceManager placeList = startRMIServer(thisReplicaPort);

    Integer multicastTimeout = 3000;

    // Handles sending all multicast messages
    new Thread() {
      public void run() {
        while (true) {
          // Sends own IP in Multicast
          try {
            placeList.sendMessage(multicastAddress, multicastPort,
                placeList.getLocalAddress() + ":" + placeList.getLocalPort());
          } catch (IOException e) {
            System.out.println(e.getMessage());
          }

          // Holds for 3 seconds
          try {
            Thread.sleep(multicastTimeout);
          } catch (InterruptedException e) {
            System.out.println(e.getMessage());
          }
        }
      }
    }.start();

    // Handles listening to multicast messages
    new Thread() {
      public void run() {
        // Listens to other IPs in Multicast
        while (true) {
          try {
            String response = placeList.listenMulticastMessage(multicastAddress, multicastPort);
            placeList.addReplica(response);
          } catch (IOException e) {
            System.out.println(e.getMessage());
          }
        }
      }
    }.start();

    // Handles removing all old replicas every 2xtimeout second
    new Thread() {
      public void run() {
        while (true) {
          try {
            placeList.cleanUpReplicas(2 * multicastTimeout);
          } catch (RemoteException e) {
            System.out.println(e.getMessage());
          }
        }
      }
    }.start();

    // Handles consensus
    new Thread() {
      public void run() {
        while (true) {
          placeList.handler(placeList);
        }
      }
    }.start();

    // Prints current replica state every 3s
    new Thread() {
      public void run() {
        while (true) {
          System.out.println(placeList);
          try {
            Thread.sleep(3000);
          } catch (InterruptedException e) {
            System.out.println(e.getMessage());
          }
        }
      }
    }.start();
  }

  private static PlaceManager startRMIServer(Integer serverPort)
      throws RemoteException, UnknownHostException {
    Registry r = null;
    PlaceManager placeList = null;

    try {
      System.out.println("Creating registry on port: " + serverPort);
      r = LocateRegistry.createRegistry(serverPort);
    } catch (RemoteException a) {
      System.out.println(serverPort + " is already used.");
    } finally {
      System.out.println("Getting registry on port: " + serverPort);
      r = LocateRegistry.getRegistry(serverPort);
    }

    placeList = new PlaceManager(serverPort);
    r.rebind("placelist", placeList);

    System.out.println("PlaceManager running on port: " + serverPort);

    return placeList;
  }
}
