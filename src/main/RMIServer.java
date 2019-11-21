package main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import consensus.ConsensusAppendRequest;
import consensus.ConsensusInterface;
import consensus.ConsensusRole;
import consensus.ConsensusVoteRequest;
import places.PlaceManager;

public class RMIServer {
  public static void main(String[] args) throws RemoteException, UnknownHostException {
    String multicastAddress = args[0];
    Integer multicastPort = Integer.parseInt(args[1]);
    Integer thisReplicaPort = Integer.parseInt(args[2]);

    PlaceManager placeList = startRMIServer(thisReplicaPort);

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
            Thread.sleep(3000);
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

    // Handles removing all old replicas every 1 second
    new Thread() {
      public void run() {
        while (true) {
          try {
            ConcurrentHashMap<String, Date> replicas = placeList.getAllReplicas();
            Iterator<Entry<String, Date>> it = replicas.entrySet().iterator();

            while (it.hasNext()) {
              Entry<String, Date> pair = it.next();
              // If replica is older than random timeout seconds
              if (new Date().getTime() - pair.getValue().getTime() < placeList
                  .getCurrentTimeout()) {
                it.remove();
              }
            }

            Thread.sleep(3000);
          } catch (RemoteException | InterruptedException e) {
            System.out.println(e.getMessage());
          }

        }
      }
    }.start();

    // Handles consensus
    new Thread() {
      public void run() {
        while (true) {
          ConsensusRole currentRole = placeList.getCurrentRole();
          if (currentRole == ConsensusRole.FOLLOWER) {
            handleFollower(placeList);

          } else if (currentRole == ConsensusRole.CANDIDATE) {
            handleCandidate(placeList);

          } else {
            handleLeader(placeList);
          }
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

  private static void handleFollower(PlaceManager placeList) {
    while (System.nanoTime() - placeList.getLastTime() < placeList.getCurrentTimeout()) {
      /* Intentionally empty. Timeout */
    }
    placeList.setLeaderAddress(null);
    placeList.setLeaderPort(null);
    placeList.setCandidateAddress(null);
    placeList.setCandidatePort(null);
    placeList.setCurrentRole(ConsensusRole.CANDIDATE);
    System.out.println(placeList);
  }

  private static void handleCandidate(PlaceManager placeList) {
    Integer numberVotes = 0;
    try {
      ConcurrentHashMap<String, Date> replicas = placeList.getAllReplicas();

      // Sends vote request while voting doesn't expire
      for (Entry<String, Date> pair : replicas.entrySet()) {
        ConsensusInterface cu =
            (ConsensusInterface) Naming.lookup("rmi://" + pair.getKey() + "/placelist");

        // Asks for a vote
        if (cu.voteRequest(new ConsensusVoteRequest(placeList.getCurrentTerm() + 1,
            placeList.getLocalAddress(), placeList.getLocalPort())) == true) {
          numberVotes++;
        }
      }

      if (numberVotes > replicas.size() / 2) {
        // I'm a new leader
        placeList.setCandidateAddress(null);
        placeList.setCandidatePort(null);
        placeList.setLeaderAddress(placeList.getLocalAddress());
        placeList.setLeaderPort(placeList.getLocalPort());
        placeList.setCurrentTerm(placeList.getCurrentTerm() + 1);
        placeList.setCurrentRole(ConsensusRole.LEADER);
      } else {
        // Back to follower
        placeList.newTimeout();
        placeList.setCandidateAddress(null);
        placeList.setCandidatePort(null);
        placeList.setCurrentRole(ConsensusRole.FOLLOWER);
      }
    } catch (RemoteException | MalformedURLException | NotBoundException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void handleLeader(PlaceManager placeList) {
    try {
      ConcurrentHashMap<String, Date> replicas = placeList.getAllReplicas();

      // Sends heartbeats to all replicas

      for (Entry<String, Date> pair : replicas.entrySet()) {
        if (pair.getKey().equals(placeList.getLocalAddress() + ":" + placeList.getLocalPort())) {
          // Doesn't send append messages to itself
          continue;
        }

        ConsensusInterface cu =
            (ConsensusInterface) Naming.lookup("rmi://" + pair.getKey() + "/placelist");

        // Sends heartbeat
        cu.appendRequest(new ConsensusAppendRequest(placeList.getCurrentTerm(),
            placeList.getLocalAddress(), placeList.getLocalPort()));
      }
    } catch (RemoteException | MalformedURLException | NotBoundException e) {
      System.out.println(e.getMessage());
    }
  }
}
