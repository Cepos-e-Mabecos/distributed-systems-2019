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

package com.ceposmabecos.places;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import com.ceposmabecos.comunication.ComunicationHeartbeat;
import com.ceposmabecos.comunication.ComunicationInterface;

/**
 * 
 * @author <a href="https://brenosalles.com" target="_blank">Breno</a>
 *
 * @since 1.0
 * @version 1.6
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

            // Checks if message contains info
            if (message.getObject() != null) {
              HashMap<Integer, Log> tempLog = (HashMap<Integer, Log>) message.getObject();
              processMessages(replica, tempLog);
            }
            return;
          }

          // My term is the same
          if (replica.getLeaderAddress().equals(message.getFullAddress()) == true) {
            // It's my leader
            // Reset timeout
            replica.newTimeout(5000, 10000);

            // Checks if message contains info
            if (message.getObject() != null) {
              HashMap<Integer, Log> tempLog = (HashMap<Integer, Log>) message.getObject();
              processMessages(replica, tempLog);
            }

            return;
          }

          // Not my leader
          break;
        default:
          /* Intentionally empty. Can be used to error handling */
          return;
      }
    }

    private void processMessages(PlaceManager replica, HashMap<Integer, Log> tempLog)
        throws RemoteException {
      LogInterface li = null;
      PlacesListInterface pi = null;
      try {
        li = (LogInterface) Naming.lookup("rmi://" + replica.getLeaderAddress() + "/placemanager");
        pi = (PlacesListInterface) Naming
            .lookup("rmi://" + replica.getLeaderAddress() + "/placemanager");
      } catch (MalformedURLException | RemoteException | NotBoundException e) {
        System.out.println(e.getMessage());
      }

      // Gets last Log
      Log lastLog = li.getLastLog();
      if (lastLog != null) {
        if (lastLog.getNumber() / 2 >= replica.getLogNumber()) {
          // Replica is too much behind, get snapshot
          replica.setAllPlaces(pi.getAllPlaces());
          replica.setLogNumber(lastLog.getNumber());
          return;
        }
      }

      while (tempLog.size() > 0) {
        Log log = tempLog.get(replica.getLogNumber() + 1);

        if (log == null) {
          // Log doesnt exist
          log = li.getLog(replica.getLogNumber() + 1);
        }

        HashMap<String, Place> places = replica.getPlaces();
        switch (log.getAction()) {
          case CREATE:
            places.put(log.getPlace().getPostalCode(), log.getPlace());
            break;

          case UPDATE:
            if (places.containsKey(log.getPlace().getPostalCode())) {
              places.put(log.getPlace().getPostalCode(), log.getPlace());
            }
            break;

          case DELETE:
            places.remove(log.getPlace().getPostalCode());
            break;

          default:
            /* Error handling if necessary. Intentionally empty */
        }

        // Adds to finalLog
        replica.getFinalLog().put(replica.getLogNumber() + 1,
            tempLog.get(replica.getLogNumber() + 1));

        // Removes from tempLog
        tempLog.remove(replica.getLogNumber() + 1);

        // Updates logNumber
        replica.setLogNumber(replica.getLogNumber() + 1);
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
   * The implementation of the handler varies based on {@link com.ceposmabecos.places.ConsensusRole
   * ConsensusRole}
   * 
   * @param replica Contains {@link com.ceposmabecos.places.PlaceManager PlaceManager} to be
   *        handled.
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
