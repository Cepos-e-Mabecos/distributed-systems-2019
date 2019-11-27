package main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import comunication.ComunicationMessage;
import comunication.FullAddress;
import places.PlaceManager;

public class RMIServer {
  public static void main(String[] args) throws RemoteException, UnknownHostException {

    String multicastAddress = args[0];
    Integer multicastPort = Integer.parseInt(args[1]);
    Integer thisReplicaPort = Integer.parseInt(args[2]);

    PlaceManager placeList = startRMIServer(multicastAddress, multicastPort,
        InetAddress.getLocalHost().getHostAddress(), thisReplicaPort);

    Integer multicastTimeout = 3000;

    // Handles sending all multicast messages
    new Thread() {
      public void run() {
        while (true) {
          // Sends current state to multicast
          try {
            placeList.sendMessage(placeList.getMulticastAddress(), new ComunicationMessage("IP",
                placeList.getCurrentTerm(), placeList.getLocalAddress()));
          } catch (IOException e) {
            System.out.println(e.getMessage());
          }

          // Holds for multicastTimeout seconds
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
        // Listens to other replicas messages in Multicast
        while (true) {
          try {
            ComunicationMessage response =
                placeList.listenMulticastMessage(placeList.getMulticastAddress());
            if (response.getMessage().equals("IP")) {
              placeList.addReplica(response.getFullAddress());
            }
          } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
          }
        }
      }
    }.start();

    // Handles removing all old replicas every 2xtimeout second
    new Thread() {
      public void run() {
        while (true) {
          placeList.cleanUpReplicas(2 * multicastTimeout);
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

  private static PlaceManager startRMIServer(String multicastAddress, Integer multicastPort,
      String serverAddress, Integer serverPort) throws RemoteException, UnknownHostException {
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

    placeList = new PlaceManager(new FullAddress(multicastAddress, multicastPort),
        new FullAddress(serverAddress, serverPort));
    r.rebind("placelist", placeList);

    System.out.println("PlaceManager running on port: " + serverPort);

    return placeList;
  }
}
