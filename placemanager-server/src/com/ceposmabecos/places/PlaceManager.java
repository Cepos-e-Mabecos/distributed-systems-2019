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
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import com.ceposmabecos.comunication.ComunicationHeartbeat;
import com.ceposmabecos.comunication.ComunicationInterface;
import com.ceposmabecos.comunication.FullAddress;

/**
 * This is the core PlaceManager
 * 
 * @author <a href="https://brenosalles.com" target="_blank">Breno</a>
 *
 * @since 1.0
 * @version 1.8
 * 
 */
public class PlaceManager extends UnicastRemoteObject
    implements PlacesListInterface, ReplicasManagerInterface, LogInterface {
  private static final long serialVersionUID = 3401280478997971431L;

  /*
   * PlaceManager Attributes
   */
  private HashMap<String, Place> places = new HashMap<String, Place>();
  private ConcurrentHashMap<FullAddress, Date> replicas =
      new ConcurrentHashMap<FullAddress, Date>();
  private FullAddress multicastAddress;
  private FullAddress localAddress;
  
  /*
   * Log Attributes
   */
  private HashMap<Integer, Log> finalLog = new HashMap<Integer, Log>();
  private HashMap<Integer, Log> tempLog = new HashMap<Integer, Log>();
  private Integer logNumber = 0;

  /*
   * Consensus Server Attributes
   */
  private ConsensusRole currentRole = ConsensusRole.FOLLOWER;
  private Integer currentTerm = 0;
  private final Integer multicastTimeout = 3000;
  private Long lastTime = System.nanoTime();
  private Long currentTimeout = (long) (Math.random() * 5000 + 5000) * 1000000;

  /*
   * Follower Attributes
   */
  private FullAddress leaderAddress;
  private FullAddress candidateAddress;

  /*
   * Constructor
   */
  public PlaceManager(String multicastAddress, Integer multicastPort, String localAddress,
      Integer localPort) throws RemoteException, UnknownHostException {
    // PlaceManager
    super(0);
    this.multicastAddress = new FullAddress(multicastAddress, multicastPort);
    this.localAddress = new FullAddress(localAddress, localPort);
  }

  /*
   * Getters & Setters
   */
  public synchronized FullAddress getMulticastAddress() {
    return multicastAddress;
  }

  public synchronized void setMulticastAddress(FullAddress multicastAddress) {
    this.multicastAddress = multicastAddress;
  }

  public synchronized FullAddress getLocalAddress() {
    return localAddress;
  }

  public synchronized void setLocalAddress(FullAddress localAddress) {
    this.localAddress = localAddress;
  }

  public synchronized ConsensusRole getCurrentRole() {
    return currentRole;
  }

  public synchronized void setCurrentRole(ConsensusRole currentRole) {
    this.currentRole = currentRole;
  }

  public synchronized Integer getCurrentTerm() {
    return currentTerm;
  }

  public synchronized void setCurrentTerm(Integer currentTerm) {
    this.currentTerm = currentTerm;
  }

  public synchronized Long getLastTime() {
    return lastTime;
  }

  public synchronized void setLastTime(Long lastTime) {
    this.lastTime = lastTime;
  }

  public synchronized Long getCurrentTimeout() {
    return currentTimeout;
  }

  public synchronized void setCurrentTimeout(Long currentTimeout) {
    this.currentTimeout = currentTimeout;
  }

  public synchronized FullAddress getLeaderAddress() {
    return leaderAddress;
  }

  public synchronized void setLeaderAddress(FullAddress leaderAddress) {
    this.leaderAddress = leaderAddress;
  }

  public synchronized FullAddress getCandidateAddress() {
    return candidateAddress;
  }

  public synchronized void setCandidateAddress(FullAddress candidateAddress) {
    this.candidateAddress = candidateAddress;
  }
  
  public synchronized HashMap<Integer, Log> getFinalLog() {
    return finalLog;
  }
  
  public synchronized void setFinalLog(HashMap<Integer, Log> finalLog) {
    this.finalLog = finalLog;
  }
  
  public synchronized HashMap<Integer, Log> getTempLog() {
    return tempLog;
  }
  
  public synchronized void setTempLog(HashMap<Integer, Log> tempLog) {
    this.tempLog = tempLog;
  }
  
  public synchronized Integer getLogNumber() {
    return logNumber;
  }
  
  public synchronized void setLogNumber(Integer logNumber) {
    this.logNumber = logNumber;
  }

  public synchronized HashMap<String, Place> getPlaces() {
    return places;
  }

  public synchronized void setPlaces(HashMap<String, Place> places) {
    this.places = places;
  }

  public synchronized ConcurrentHashMap<FullAddress, Date> getReplicas() {
    return replicas;
  }

  public synchronized void setReplicas(ConcurrentHashMap<FullAddress, Date> replicas) {
    this.replicas = replicas;
  }

  /*
   * String toString
   */
  @Override
  public String toString() {
    return "\nRole: " + this.getCurrentRole() + "\nTerm: " + this.getCurrentTerm()
        + "\nReplica FullAddress: " + this.getLocalAddress().toString() + "\nLeader FullAddress: "
        + (this.getLeaderAddress() == null ? "null" : this.getLeaderAddress().toString());
  }


  /**
   * This function should be called remotely to add a Place to the class HashMap of &#60;String, Place&#62;.
   * 
   * @param place Contains object of type {@link com.ceposmabecos.places.Place Place} to be added.
   * 
   * @throws RemoteException When it fails to reach the the host.
   * 
   */
  @Override
  public void addPlace(Place place) throws RemoteException {
    places.put(place.getPostalCode(), place);
    tempLog.put(++logNumber, new Log(logNumber, LogAction.CREATE, place));
  }

  /**
   * This function should be called remotely to remove a Place to the class HashMap of &#60;String, Place&#62;.
   * 
   * @param postalCode Contains string with postalCode to be used to remove the Place.
   * 
   * @return {@link com.ceposmabecos.places.Place Place} This returns the deleted Place.
   * 
   * @throws RemoteException When it fails to reach the the host.
   * 
   */
  @Override
  public Place removePlace(String postalCode) throws RemoteException {
    tempLog.put(++logNumber, new Log(logNumber, LogAction.DELETE, places.get(postalCode)));
    return places.remove(postalCode);
  }

  /**
   * This function should be called remotely to retrieve a specific Place from the class HashMap of &#60;String, Place&#62;.
   * 
   * @param postalCode Contains string with postalCode to be used to search the Place.
   * 
   * @return {@link com.ceposmabecos.places.Place} This returns the corresponding Place.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   */
  @Override
  public Place getPlace(String postalCode) throws RemoteException {
    return places.get(postalCode);
  }

  /**
   * This function should be called remotely to retrieve the class HashMap of &#60;String, Place&#62;.
   * 
   * @return HashMap This returns all Place.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  @Override
  public HashMap<String, Place> getAllPlaces() throws RemoteException {
    return places;
  }

  /**
   * This function should be called remotely to change the class HashMap of &#60;String, Place&#62;.
   * 
   * @param places Contains HashMap of &#60;String, {@link com.ceposmabecos.places.Place Place}&#62;.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  @Override
  public void setAllPlaces(HashMap<String, Place> places) throws RemoteException {
    this.places = places;
  }

  /**
   * This function can be called remotely to add an address of a PlaceManager address to the class
   * ConcurrentHashMap that contains all addresses of all PlaceManager.
   * 
   * @param replicaAddress Contains {@link com.ceposmabecos.comunication.FullAddress FullAddress} with address (ip+port)
   * 
   */
  @Override
  public void addReplica(FullAddress replicaAddress) {
    replicas.put(replicaAddress, new Date());
  }

  /**
   * This function can be called remotely to remove an address of a PlaceManager address from the
   * class ConcurrentHashMap that contains all addresses of all PlaceManager.
   * 
   * @param replicaAddress Contains {@link com.ceposmabecos.comunication.FullAddress FullAddress} with address (ip+port)
   * 
   */
  @Override
  public void removeReplica(FullAddress replicaAddress) {
    replicas.remove(replicaAddress);
  }

  /**
   * This function can be called remotely to swap the class ConcurrentHashMap that contains all
   * addresses of all PlaceManager with a new one.
   * 
   * @param replicas Contains ConcurrentHashMap to be used as new ConcurrentHashMap of the class.
   * 
   */
  @Override
  public void addAllReplicas(ConcurrentHashMap<FullAddress, Date> replicas) {
    this.replicas = replicas;
  }

  /**
   * This function can be called remotely to clear the class ConcurrentHashMap that contains all
   * addresses of all PlaceManager.
   * 
   */
  @Override
  public void removeAllReplicas() {
    replicas.clear();
  }

  /**
   * This function can be called remotely to clean up old replicas from the class ConcurrentHashMap
   * that contains all addresses of all PlaceManager.
   * 
   * @param maximumReplicaAge Contains the max age of the replica we should consider. Must be in
   *        milliseconds.
   * 
   */
  @Override
  public void cleanUpReplicas(Integer maximumReplicaAge) {
    ConcurrentHashMap<FullAddress, Date> replicas = this.getAllReplicas();
    Iterator<Entry<FullAddress, Date>> it = replicas.entrySet().iterator();

    while (it.hasNext()) {
      Entry<FullAddress, Date> pair = it.next();
      // If replica is older than random timeout seconds
      if (new Date().getTime() - pair.getValue().getTime() > maximumReplicaAge) {
        it.remove();
      }
    }
  }

  /**
   * This function can be called remotely to retrieve the class ConcurrentHashMap that contains all
   * addresses of all PlaceManager.
   * 
   * @return ConcurrentHashMap This returns all replicas.
   * 
   */
  @Override
  public ConcurrentHashMap<FullAddress, Date> getAllReplicas() {
    return replicas;
  }
  
  /**
   * This function can be called remotely to get a Log.
   * 
   * @param logBumber Contains the number of the log that the caller wants.
   * 
   * @return {@link com.ceposmabecos.places.Log Log} This returns the wanted log.
   * 
   * @throws RemoteException When it fails to reach the the host.
   * 
   */
  @Override
  public Log getLog(Integer logNumber) throws RemoteException {
    return this.getFinalLog().get(logNumber);
  }
  
  /**
   * This function can be called remotely to get the last Log.

   * @return {@link com.ceposmabecos.places.Log Log} This returns the last log.
   * 
   * @throws RemoteException When it fails to reach the the host.
   * 
   */
  @Override
  public Log getLastLog() throws RemoteException {
    return this.getFinalLog().get(logNumber);
  }

  /**
   * This function should be used to handle the behaviour of a PlaceManager server.
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
  public void handler(PlaceManager replica)
      throws ClassNotFoundException, IOException, InterruptedException {
    this.getCurrentRole().handler(replica);
  }


  /**
   * This function is used to generate a new Timeout on this PlaceManager server. It will generate a
   * random time between min and max.
   * 
   * @param min Contains Integer with min value (in milliseconds)
   * 
   * @param max Contains Integer with max value (in milliseconds)
   * 
   */
  public void newTimeout(Integer min, Integer max) {
    this.setLastTime(System.nanoTime());
    this.setCurrentTimeout((long) (Math.random() * min + max - min) * 1000000);
  }

  /**
   * This function is used to start this PlaceManager server.
   */
  public void start() {
    sendSelfMulticastMessage();

    receiveOthersMulticastMessage();

    removeOldReplicas();

    consensusStart();
  }

  /**
   * This function is used to send the current state of this PlaceManager
   */
  private void sendSelfMulticastMessage() {
    // Handles sending all multicast messages
    new Thread() {
      public void run() {
        while (true) {
          // Sends current state to multicast
          try {
            switch (getCurrentRole()) {
              case CANDIDATE:
                ComunicationInterface.sendMessage(PlaceManager.this.getMulticastAddress(),
                    new ComunicationHeartbeat(PlaceManager.this.getCurrentRole().toString(),
                        PlaceManager.this.getCurrentTerm() + 1,
                        PlaceManager.this.getLocalAddress()));
                break;
              case LEADER:
                HashMap<Integer, Log> tempLog = PlaceManager.this.getTempLog();
                
                ComunicationInterface.sendMessage(PlaceManager.this.getMulticastAddress(),
                    new ComunicationHeartbeat(PlaceManager.this.getCurrentRole().toString(),
                        PlaceManager.this.getCurrentTerm(), PlaceManager.this.getLocalAddress(),
                        tempLog));
                
                // Commits tempLog
                PlaceManager.this.getFinalLog().putAll(tempLog);
                
                // Clears tempLog
                tempLog.clear();
                break;
              default:
                ComunicationInterface.sendMessage(PlaceManager.this.getMulticastAddress(),
                    new ComunicationHeartbeat(PlaceManager.this.getCurrentRole().toString(),
                        PlaceManager.this.getCurrentTerm(), PlaceManager.this.getLocalAddress()));
            }
          } catch (IOException e) {
            System.out.println(e.getMessage());
          }
          System.out.println(PlaceManager.this);
          // Holds for multicastTimeout seconds
          try {
            Thread.sleep(multicastTimeout);
          } catch (InterruptedException e) {
            System.out.println(e.getMessage());
          }
        }
      }
    }.start();
  }

  /**
   * This function is used to receive the state of other PlaceManager's
   */
  private void receiveOthersMulticastMessage() {
    // Handles listening to multicast messages
    new Thread() {
      public void run() {
        // Listens to other replicas messages in Multicast
        while (true) {
          try {
            ComunicationHeartbeat message =
                ComunicationInterface.listenMulticastMessage(getMulticastAddress());
            addReplica(message.getFullAddress());
          } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
          }
        }
      }
    }.start();
  }

  /**
   * This function is used to remove old replicas that potentially died
   */
  private void removeOldReplicas() {
    // Handles removing all old replicas of 2xtimeout second
    new Thread() {
      public void run() {
        while (true) {
          PlaceManager.this.cleanUpReplicas(2 * multicastTimeout);

          try {
            Thread.sleep(multicastTimeout);
          } catch (InterruptedException e) {
            System.out.println(e.getMessage());
          }
        }
      }
    }.start();
  }

  /**
   * This function is used to start handling the consensus of this server
   */
  private void consensusStart() {
    // Handles consensus
    new Thread() {
      public void run() {
        while (true) {
          try {
            PlaceManager.this.handler(PlaceManager.this);
          } catch (ClassNotFoundException | IOException | InterruptedException e) {
            System.out.println(e.getMessage());
          }
        }
      }
    }.start();
  }
}
