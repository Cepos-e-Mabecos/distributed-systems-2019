/*
 * MIT License
 * 
 * Copyright (c) 2019 Cepos e Mabecos
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package consensus;

import java.io.IOException;
import comunication.ComunicationHeartbeat;
import places.PlaceManager;

/**
 * 
 * @author <a href="https://brenosalles.com" target="_blank">Breno</a>
 *
 * @since 1.0
 * @version 1.3
 * 
 */
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
          // Gets a message
          ComunicationHeartbeat message =
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

                // Append new candidate
                replica.setCandidateAddress(message.getFullAddress());

                // Send vote
                replica.sendMessage(replica.getMulticastAddress(),
                    new ComunicationHeartbeat("VOTE", null, message.getFullAddress()));
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
                replica.newTimeout(5000, 5000);

                // Reset candidate stats
                replica.setCandidateAddress(null);

                // Append new leader
                replica.setCurrentTerm(message.getTerm());
                replica.setLeaderAddress(message.getFullAddress());

                // Appends new places if exits
                if (message.getPlaces() != null) {
                  replica.setAllPlaces(message.getPlaces());
                }

                System.out.println(replica.getLeaderAddress());

                // Reset role
                replica.setCurrentRole(ConsensusRole.FOLLOWER);

                continue;
              }

              // My term is the same
              if (replica.getLeaderAddress().equals(message.getFullAddress()) == true) {
                // It's my leader
                // Reset timeout
                replica.newTimeout(5000, 5000);

                // Append new leader
                replica.setCurrentTerm(message.getTerm());
                replica.setLeaderAddress(message.getFullAddress());

                // Appends new places if exits
                if (message.getPlaces() != null) {
                  replica.setAllPlaces(message.getPlaces());
                }

                // Reset role
                replica.setCurrentRole(ConsensusRole.FOLLOWER);
              }

              // Not my leader
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
      Integer size = replica.getAllReplicas().size();

      replica.newTimeout(2500, 5000);
      ConsensusVoting voting = new ConsensusVoting(replica);
      Thread t = new Thread(voting);
      t.start();

      // Happens while voting timeout is active
      while (true) {
        if (numberVotes > (size / 2) == true) {
          voting.terminate();
          break;
        }

        if (t.isAlive() == false) {
          // Timeout expired
          break;
        }
        try {
          // Listens for messages
          ComunicationHeartbeat response =
              replica.listenMulticastMessage(replica.getMulticastAddress());
          if (response.getMessage().equals("LEADER") == true) {
            // ANOTHER LEADER, STEP DOWN
            voting.terminate();
            t.join();
            // Back to follower
            replica.newTimeout(5000, 10000);
            replica.setCandidateAddress(null);
            replica.setCurrentTerm(response.getTerm());
            replica.setLeaderAddress(response.getFullAddress());
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
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
          System.out.println(e.getMessage());
        }
      }

      try {
        t.join();
      } catch (InterruptedException e) {
        System.out.println(e.getMessage());
      }
      if (numberVotes > (size / 2) == true) {
        // I'm a new leader
        replica.setCandidateAddress(null);
        replica.setLeaderAddress(replica.getLocalAddress());
        replica.setCurrentTerm(replica.getCurrentTerm() + 1);
        replica.setCurrentRole(ConsensusRole.LEADER);
      } else {
        // Back to follower
        replica.newTimeout(5000, 5000);
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
        replica.sendMessage(replica.getMulticastAddress(), new ComunicationHeartbeat("LEADER",
            replica.getCurrentTerm(), replica.getLocalAddress()));
        Thread.sleep(1000);
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
