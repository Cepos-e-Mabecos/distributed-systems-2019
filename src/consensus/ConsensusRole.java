package consensus;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import places.PlaceManager;

public enum ConsensusRole implements ConsensusHandlerInterface {
  FOLLOWER {
    /*
     * Implementation of handler when ConsensusRole.FOLLOWER.
     */
    @Override
    public void handler(PlaceManager replica) {
      while (System.nanoTime() - replica.getLastTime() < replica.getCurrentTimeout()) {
        /* Intentionally empty. FollowerTimeout */
      }
      replica.setLeaderAddress(null);
      replica.setLeaderPort(null);
      replica.setCandidateAddress(null);
      replica.setCandidatePort(null);
      replica.setCurrentRole(ConsensusRole.CANDIDATE);
      System.out.println(replica);
    }
  },

  /*
   * Implementation of handler when ConsensusRole.CANDIDATE.
   */
  CANDIDATE {
    @Override
    public void handler(PlaceManager replica) {
      Integer numberVotes = 0;
      replica.newTimeout();

      // Voting timeout
      Thread t = new Thread() {
        public void run() {
          while (System.nanoTime() - replica.getLastTime() < replica.getCurrentTimeout()) {
            /* Intentionally empty. VotingTimeout */
          }
        }
      };
      t.start();

      try {
        ConcurrentHashMap<String, Date> replicas = replica.getAllReplicas();
        Iterator<Entry<String, Date>> it = replicas.entrySet().iterator();

        // Sends vote request while voting doesn't expire
        while (it.hasNext() && t.isAlive()) {
          Entry<String, Date> pair = it.next();

          ConsensusRequestInterface cu =
              (ConsensusRequestInterface) Naming.lookup("rmi://" + pair.getKey() + "/placelist");

          // Asks for a vote
          if (cu.voteRequest(new ConsensusVoteRequest(replica.getCurrentTerm() + 1,
              replica.getLocalAddress(), replica.getLocalPort())) == true) {
            numberVotes++;
          }
        }

        if (numberVotes > replicas.size() / 2) {
          // I'm a new leader
          replica.setCandidateAddress(null);
          replica.setCandidatePort(null);
          replica.setLeaderAddress(replica.getLocalAddress());
          replica.setLeaderPort(replica.getLocalPort());
          replica.setCurrentTerm(replica.getCurrentTerm() + 1);
          replica.setCurrentRole(ConsensusRole.LEADER);
        } else {
          // Back to follower
          replica.newTimeout();
          replica.setCandidateAddress(null);
          replica.setCandidatePort(null);
          replica.setCurrentRole(ConsensusRole.FOLLOWER);
        }
      } catch (RemoteException | MalformedURLException | NotBoundException e) {
        System.out.println(e.getMessage());
      }
    }
  },

  /*
   * Implementation of handler when ConsensusRole.LEADER.
   */
  LEADER {
    @Override
    public void handler(PlaceManager replica) {
      try {
        ConcurrentHashMap<String, Date> replicas = replica.getAllReplicas();

        // Sends heartbeats to all replicas

        for (Entry<String, Date> pair : replicas.entrySet()) {
          if (pair.getKey().equals(replica.getLocalAddress() + ":" + replica.getLocalPort())) {
            // Doesn't send append messages to itself
            continue;
          }

          ConsensusRequestInterface cu =
              (ConsensusRequestInterface) Naming.lookup("rmi://" + pair.getKey() + "/placelist");

          // Sends heartbeat
          cu.appendRequest(new ConsensusAppendRequest(replica.getCurrentTerm(),
              replica.getLocalAddress(), replica.getLocalPort()));
        }
      } catch (RemoteException | MalformedURLException | NotBoundException e) {
        System.out.println(e.getMessage());
      }
    }
  };

  /**
   * The implementation of the handler varies based on ConsensusRole
   */
  @Override
  public abstract void handler(PlaceManager replica);
}
