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

package com.ceposmabecos.consensus;

import java.io.IOException;
import java.util.HashMap;
import com.ceposmabecos.comunication.ComunicationHeartbeat;
import com.ceposmabecos.comunication.ComunicationInterface;
import com.ceposmabecos.places.Place;
import com.ceposmabecos.places.PlaceManager;

/**
 * 
 * @author <a href="https://brenosalles.com" target="_blank">Breno</a>
 *
 * @since 1.0
 * @version 1.5
 * 
 */
public enum ConsensusRole implements ConsensusHandlerInterface {
  FOLLOWER {
    /*
     * Implementation of handler when ConsensusRole.FOLLOWER.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void handler(PlaceManager replica) throws ClassNotFoundException, IOException {
      if (System.nanoTime() - replica.getLastTime() > replica.getCurrentTimeout()) {
        // Timeout expired
        // Become candidate
        replica.setLeaderAddress(null);
        replica.setCandidateAddress(null);
        replica.setCurrentRole(ConsensusRole.CANDIDATE);
        System.out.println(replica);
        return;
      }

      // Gets a message
      ComunicationHeartbeat message =
          ComunicationInterface.listenMulticastMessage(replica.getMulticastAddress());

      switch (message.getMessage()) {
        case "CANDIDATE":
          // We received a vote request
          if (replica.getCurrentTerm() >= message.getTerm()) {
            // My term is higher, ignore
            return;
          }

          if (replica.getCandidateAddress() != null) {
            // Already voted for a candidate this term
            return;
          }

          // New candidate with higher term

          // Append new candidate
          replica.setCandidateAddress(message.getFullAddress());

          // Send vote
          ComunicationInterface.sendMessage(replica.getMulticastAddress(),
              new ComunicationHeartbeat("VOTE", null, message.getFullAddress()));
          break;
        case "LEADER":
          // We received a leader heartbeat

          if (replica.getCurrentTerm() > message.getTerm()) {
            // My term is higher, not my leader
            return;
          }

          if (replica.getCurrentTerm() < message.getTerm()) {
            // New leader with higher term

            // Reset timeout
            replica.newTimeout(5000, 10000);

            // Reset candidate stats
            replica.setCandidateAddress(null);

            // Append new leader
            replica.setCurrentTerm(message.getTerm());
            replica.setLeaderAddress(message.getFullAddress());

            // Appends new places if exits
            if (message.getObject() != null) {
              replica.setAllPlaces((HashMap<String, Place>) message.getObject());
            }
            return;
          }

          // My term is the same
          if (replica.getLeaderAddress().equals(message.getFullAddress()) == true) {
            // It's my leader
            // Reset timeout
            replica.newTimeout(5000, 10000);

            // Appends new places if exits
            if (message.getObject() != null) {
              replica.setAllPlaces((HashMap<String, Place>) message.getObject());
            }
          }

          // Not my leader
          break;
        default:
          /* Intentionally empty. Can be used to error handling */
          return;
      }
    }
  },

  /*
   * Implementation of handler when ConsensusRole.CANDIDATE.
   */
  CANDIDATE {
    @Override
    public void handler(PlaceManager replica)
        throws ClassNotFoundException, IOException, InterruptedException {
      // Votes for himself
      Integer numberVotes = 1;

      ConsensusVoting voting = new ConsensusVoting(replica);
      Thread t = new Thread(voting);
      t.start();

      // Happens while voting timeout is active
      while (true) {
        if (numberVotes > (replica.getAllReplicas().size() / 2) == true) {
          voting.terminate();
          break;
        }

        if (t.isAlive() == false) {
          // Timeout expired
          break;
        }

        // Listens for messages
        ComunicationHeartbeat response =
            ComunicationInterface.listenMulticastMessage(replica.getMulticastAddress());
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
      }

      t.join();
      if (numberVotes > (replica.getAllReplicas().size() / 2) == true) {
        // I'm a new leader
        replica.setCandidateAddress(null);
        replica.setLeaderAddress(replica.getLocalAddress());
        replica.setCurrentTerm(replica.getCurrentTerm() + 1);
        replica.setCurrentRole(ConsensusRole.LEADER);
        return;
      }

      // Back to follower
      replica.newTimeout(5000, 10000);
      replica.setCandidateAddress(null);
      replica.setCurrentRole(ConsensusRole.FOLLOWER);
    }
  },

  /*
   * Implementation of handler when ConsensusRole.LEADER.
   */
  LEADER {
    @Override
    public void handler(PlaceManager replica) throws ClassNotFoundException, IOException {
      ComunicationHeartbeat message =
          ComunicationInterface.listenMulticastMessage(replica.getMulticastAddress());
      if (message.getMessage().equals("LEADER") != true) {
        // We only care about possible new leaders
        return;
      }

      if (replica.getCurrentTerm() >= message.getTerm()) {
        // This leader is lower than me
        return;
      }

      // New leader with higher term. Step down
      replica.newTimeout(5000, 10000);
      replica.setCandidateAddress(null);
      replica.setLeaderAddress(message.getFullAddress());
      replica.setCurrentTerm(message.getTerm());
      replica.setCurrentRole(ConsensusRole.FOLLOWER);
    }
  };

  /**
   * The implementation of the handler varies based on ConsensusRole
   * 
   * @param replica Contains {@link com.ceposmabecos.places.PlaceManager PlaceManager} to be handled.
   * 
   * @throws IOException On Input or Output error.
   * 
   * @throws ClassNotFoundException When reading a class outputs error.
   * 
   * @throws InterruptedException When it fails to wait for the thread.
   * 
   */
  @Override
  public abstract void handler(PlaceManager replica)
      throws ClassNotFoundException, IOException, InterruptedException;
}
