package consensus;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import comunication.ComunicationMessage;
import comunication.FullAddress;
import places.PlaceManager;

public enum ConsensusRole implements ConsensusHandlerInterface {
  FOLLOWER {
    /*
     * Implementation of handler when ConsensusRole.FOLLOWER.
     */
    @Override
    public void handler(PlaceManager replica) {
      while (true) {
        if (System.nanoTime() - replica.getLastTime() > replica.getCurrentTimeout()) {
          // Timeout expired
          break;
        }

        try {
          ComunicationMessage message =
              replica.listenMulticastMessage(replica.getMulticastAddress());

          switch (message.getMessage()) {
            case "CANDIDATE":
              // We received a vote request
              if (replica.getCurrentTerm() > message.getTerm()) {
                // My term is higher, ignore
                continue;
              }

              if (replica.getCandidateAddress() != null) {
                // Already voted for a candidate
                continue;
              }

              if (replica.getCurrentTerm() < message.getTerm()) {
                // New candidate with higher term


                // Reset leader starts
                replica.setLeaderAddress(null);

                // Append new candidate
                replica.setCandidateAddress(message.getFullAddress());

                // Don't update timeout

                // Send vote
                replica.sendMessage(replica.getMulticastAddress(),
                    new ComunicationMessage("VOTE", message.getTerm(), message.getFullAddress()));
              }

              // Don't vote when candidate is same term as my leader
              break;
            case "LEADER":
              // We received a leader heartbeat

              if (replica.getCurrentTerm() > message.getTerm()) {
                // My term is higher, not my leader
                continue;
              }

              if (replica.getCurrentTerm() < message.getTerm()) {
                // New leader with higher term

                // Reset timeout
                replica.newTimeout();

                // Reset candidate stats
                replica.setCandidateAddress(null);

                // Append new leader
                replica.setCurrentTerm(message.getTerm());
                replica.setLeaderAddress(message.getFullAddress());

                // Reset role
                replica.setCurrentRole(ConsensusRole.FOLLOWER);

                continue;
              }

              // My term is the same
              // Reset timeout
              replica.newTimeout();

              // Reset candidate stats
              replica.setCandidateAddress(null);

              // Append new leader
              replica.setCurrentTerm(message.getTerm());
              replica.setLeaderAddress(message.getFullAddress());

              // Reset role
              replica.setCurrentRole(ConsensusRole.FOLLOWER);
              break;
            default:
              /* Intentionally empty. Can be used to error handling */
              continue;
          }
        } catch (ClassNotFoundException | IOException e) {
          System.out.println(e.getMessage());
        }
      }

      // Become candidate
      replica.setLeaderAddress(null);
      replica.setCandidateAddress(null);
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
      // Votes for himself
      Integer numberVotes = 1;

      replica.newTimeout();
      ConsensusVoting voting = new ConsensusVoting(replica);
      Thread t = new Thread(voting);
      t.start();

      ConcurrentHashMap<FullAddress, Date> replicas = replica.getAllReplicas();

      // Happens while voting timeout is active
      while (true) {
        if (numberVotes > (replicas.size() / 2) == true) {
          voting.terminate();
          break;
        }

        if (t.isAlive() == false) {
          // Timeout expired
          break;
        }
        try {
          // Listens for messages
          ComunicationMessage response =
              replica.listenMulticastMessage(replica.getMulticastAddress());
          if (response.getMessage().equals("LEADER") == true) {
            // ANOTHER LEADER, STEP DOWN
            // Back to follower
            replica.newTimeout();
            replica.setCandidateAddress(null);
            replica.setCurrentRole(ConsensusRole.FOLLOWER);
            return;
          }

          if (response.getMessage().equals("VOTE") == false) {
            // Ignore messages that are not VOTE
            continue;
          }

          if (response.getFullAddress().equals(replica.getLocalAddress()) == false) {
            // Not my address, not my VOTE
            continue;
          }

          // My VOTE!
          numberVotes++;
        } catch (IOException | ClassNotFoundException e) {
          System.out.println(e.getMessage());
        }
      }

      try {
        t.join();
      } catch (InterruptedException e) {
        System.out.println(e.getMessage());
      }
      if (numberVotes > (replicas.size() / 2) == true) {
        // I'm a new leader
        replica.setCandidateAddress(null);
        replica.setLeaderAddress(replica.getLocalAddress());
        replica.setCurrentTerm(replica.getCurrentTerm() + 1);
        replica.setCurrentRole(ConsensusRole.LEADER);
      } else {
        // Back to follower
        replica.newTimeout();
        replica.setCandidateAddress(null);
        replica.setCurrentRole(ConsensusRole.FOLLOWER);
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
        replica.sendMessage(replica.getMulticastAddress(),
            new ComunicationMessage("LEADER", replica.getCurrentTerm(), replica.getLocalAddress()));
        Thread.sleep(3000);
      } catch (IOException | InterruptedException e) {
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
