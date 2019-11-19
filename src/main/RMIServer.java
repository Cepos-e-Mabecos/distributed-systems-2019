package main;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import consensus.ConsensusRole;
import places.PlaceManager;

public class RMIServer {
  public static void main(String[] args) {
    Integer multicastPort = Integer.parseInt(args[0]);
    String multicastAddress = args[1];
    Integer serverPort = Integer.parseInt(args[2]);

    PlaceManager placeList = startRMIServer(serverPort);

    // Handles sending all multicast messages
    new Thread() {
      public void run() {
        while (true) {
          // Sends own IP in Multicast
          try {
            placeList.sendMessage(multicastAddress, multicastPort,
                InetAddress.getLocalHost().getHostAddress() + ":" + serverPort);
          } catch (IOException e) {
            System.out.println(e.getMessage());
          }

          // Holds for 10 seconds
          try {
            Thread.sleep(10000);
          } catch (InterruptedException e) {
            System.out.println(e.getMessage());
          }
        }
      }
    }.start();

    // Clears all replicas every 15s
    new Thread() {
      public void run() {
        while (true) {
          try {
            placeList.removeAllReplicas();
          } catch (RemoteException e) {
            System.out.println(e.getMessage());
          }

          // Holds for 15 seconds
          try {
            Thread.sleep(15000);
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
            String replicaAddress =
                placeList.listenMulticastMessage(multicastAddress, multicastPort);
            placeList.addReplica(replicaAddress);
          } catch (IOException e) {
            System.out.println(e.getMessage());
          }
        }
      }
    }.start();

    // Handles consensus
    new Thread() {
      public void run() {
        while (true) {
          ConsensusRole current = placeList.getRole();

          if (current == ConsensusRole.LEADER) {
            System.out.println(current);
            consensusLeader(placeList);

            // Holds for 1 second
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              System.out.println(e.getMessage());
            }

          } else if (current == ConsensusRole.CANDIDATE) {
            System.out.println(current);
            consensusCandidate(placeList);

          } else {
            System.out.println(current);
            consensusFollower(placeList);
          }
        }
      }
    }.start();
  }

  private static PlaceManager startRMIServer(Integer serverPort) {
    Registry r = null;
    PlaceManager placeList = null;

    try {
      System.out.println("Creating registry on port: " + serverPort);
      r = LocateRegistry.createRegistry(serverPort);
    } catch (RemoteException a) {
      System.out.println(serverPort + " is already used.");
      try {
        System.out.println("Getting registry on port: " + serverPort);
        r = LocateRegistry.getRegistry(serverPort);
      } catch (NumberFormatException | RemoteException e) {
        System.out.println("Error getting registry of port: " + serverPort);
      }
    }

    try {
      placeList = new PlaceManager(serverPort);
      r.rebind("placelist", placeList);

      System.out.println("PlaceManager running on port: " + serverPort);
    } catch (Exception e) {
      System.out.println("Error trying to run PlaceManager on port: " + serverPort);
    }

    return placeList;
  }

  private static void consensusLeader(PlaceManager placeList) {
    // Sends Unicast Message to all replicas
    ArrayList<String> replicas = placeList.getReplicas();
    for (String address : replicas) {
      // Splits full address (host:port)
      String[] fullAddress = address.split(":");
      String host = fullAddress[0];
      Integer port = Integer.parseInt(fullAddress[1]);

      // Sends own address labeled as leader
      try {
        placeList.sendMessage(host, port, "leader," + InetAddress.getLocalHost().getHostAddress()
            + ":" + placeList.getServerPort());
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }
    }
  }

  private static void consensusCandidate(PlaceManager placeList) {
    new Thread() {
      public void run() {
        // Sends Unicast Message to all replicas
        ArrayList<String> replicas = placeList.getReplicas();
        for (String address : replicas) {
          // Splits full address (host:port)
          String[] fullAddress = address.split(":");
          String host = fullAddress[0];
          Integer port = Integer.parseInt(fullAddress[1]);

          // Sends own address labeled as candidate
          try {
            placeList.sendMessage(host, port, "candidate,"
                + InetAddress.getLocalHost().getHostAddress() + ":" + placeList.getServerPort());
          } catch (IOException e) {
            System.out.println(e.getMessage());
          }
        }

      }
    }.start();

    Integer numberVotes = 1;
    // While numberVotes is not majority
    ArrayList<String> replicas = placeList.getReplicas();
    while (numberVotes < replicas.size() / 2) {
      try {
        // Gets message and splits it on ","
        String[] message = placeList.listenUnicastMessage(placeList.getServerPort()).split(",");

        if (message[0].equals("leader")) {
          // New leader was found before
          placeList.setRole(ConsensusRole.FOLLOWER);
          return;
        }

        if (message[0].equals("vote")) {
          // New vote received
          numberVotes++;
        }
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }
    }

    // New Leader!
    placeList.setRole(ConsensusRole.LEADER);
  }

  private volatile static Long start;

  private static void consensusFollower(PlaceManager placeList) {
    start = System.nanoTime();
    new Thread() {

      public void run() {
        while ((Long) System.nanoTime() - start < 10000000000L) {
          try {
            // Gets message and splits it on ","
            String[] message = placeList.listenUnicastMessage(placeList.getServerPort()).split(",");

            if (message[0].equals("leader")) {
              // Resets timer
              start = System.nanoTime();
              // New leader was found before
              placeList.setRole(ConsensusRole.FOLLOWER);
            }

            if (message[0].equals("candidate")) {
              // New candidate received
              String[] fullAddress = message[1].split(":");
              String host = fullAddress[0];
              Integer port = Integer.parseInt(fullAddress[1]);
              // Sends vote
              placeList.sendMessage(host, port, "vote,"
                  + InetAddress.getLocalHost().getHostAddress() + ":" + placeList.getServerPort());
            }

            if (message[0].equals("exit")) {
              this.interrupt();
              return;
            }
          } catch (IOException e) {
            System.out.println(e.getMessage());
          }
        }

      }
    }.start();
    // Loop happens if we in the first 10s
    while ((Long) System.nanoTime() - start < 10000000000L) {

    }
    try {
      placeList.sendMessage(InetAddress.getLocalHost().getHostAddress(), placeList.getServerPort(),
          "exit,");
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
    // Becomes candidate
    placeList.setRole(ConsensusRole.CANDIDATE);
  }
}
