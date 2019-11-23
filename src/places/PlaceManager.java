package places;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import comunication.ComunicationInterface;
import consensus.ConsensusAppendRequest;
import consensus.ConsensusHandlerInterface;
import consensus.ConsensusRequestInterface;
import consensus.ConsensusRole;
import consensus.ConsensusVoteRequest;

public class PlaceManager extends UnicastRemoteObject
    implements PlacesListInterface, ReplicasManagerInterface, ComunicationInterface,
    ConsensusRequestInterface, ConsensusHandlerInterface {

  /**
   * 
   */
  private static final long serialVersionUID = 4883086976774125339L;
  /*
   * PlaceManager Attributes
   */
  private ArrayList<Place> places = new ArrayList<Place>();
  private ConcurrentHashMap<String, Date> replicas = new ConcurrentHashMap<String, Date>();
  private String localAddress;
  private Integer localPort;

  /*
   * Consensus Server Attributes
   */
  private ConsensusRole currentRole;
  private Integer currentTerm;

  /*
   * Follower Attributes
   */
  private Long lastTime;
  private Long currentTimeout;
  private String leaderAddress;
  private Integer leaderPort;
  private String candidateAddress;
  private Integer candidatePort;

  /*
   * Constructor
   */
  public PlaceManager(Integer port) throws RemoteException, UnknownHostException {
    // PlaceManager
    super(0);
    this.localAddress = InetAddress.getLocalHost().getHostAddress();
    this.localPort = port;

    // Consensus Server Attributes
    this.currentRole = ConsensusRole.FOLLOWER;
    this.currentTerm = 0;
    this.currentTimeout = (long) (Math.random() * 5000 + 5000) * 1000000;

    // Follower Attributes
    this.lastTime = System.nanoTime();
    this.leaderAddress = null;
    this.leaderPort = null;
    this.candidateAddress = null;
    this.candidatePort = null;
  }

  /*
   * Getters & Setters PlaceManager
   */
  public String getLocalAddress() {
    return localAddress;
  }

  public void setLocalAddress(String localAddress) {
    this.localAddress = localAddress;
  }

  public Integer getLocalPort() {
    return localPort;
  }

  public void setLocalPort(Integer port) {
    this.localPort = port;
  }

  /*
   * Getters & Setters ConsensusServer
   */
  public synchronized ConsensusRole getCurrentRole() {
    return currentRole;
  }

  public synchronized void setCurrentRole(ConsensusRole currentRole) {
    this.currentRole = currentRole;
  }

  public synchronized Integer getCurrentTerm() {
    return currentTerm;
  }

  public synchronized void setCurrentTerm(Integer term) {
    this.currentTerm = term;
  }

  public synchronized Long getCurrentTimeout() {
    return currentTimeout;
  }

  public synchronized void setCurrentTimeout(Long timeout) {
    this.currentTimeout = timeout;
  }

  public synchronized Long getLastTime() {
    return lastTime;
  }

  public synchronized void setLastTime(Long time) {
    this.lastTime = time;
  }

  public synchronized String getLeaderAddress() {
    return leaderAddress;
  }

  public synchronized void setLeaderAddress(String leaderAddress) {
    this.leaderAddress = leaderAddress;
  }

  public synchronized Integer getLeaderPort() {
    return leaderPort;
  }

  public synchronized void setLeaderPort(Integer leaderPort) {
    this.leaderPort = leaderPort;
  }

  public synchronized String getCandidateAddress() {
    return candidateAddress;
  }

  public synchronized void setCandidateAddress(String candidateAddress) {
    this.candidateAddress = candidateAddress;
  }

  public synchronized Integer getCandidatePort() {
    return candidatePort;
  }

  public synchronized void setCandidatePort(Integer candidatePort) {
    this.candidatePort = candidatePort;
  }

  /*
   * String toString
   */
  @Override
  public String toString() {
    return "\nRole: " + this.getCurrentRole() + "\nTerm: " + this.getCurrentTerm()
        + "\nReplica FullAddress: " + this.getLocalAddress() + ":" + this.getLocalPort()
        + "\nLeader FullAddress: " + this.getLeaderAddress() + ":" + this.getLeaderPort();
  }

  /**
   * This function can be called remotely to add a Place to the class ArrayList of Places.
   * 
   * @param place Contains object of type Place to be added.
   * 
   * @throws RemoteException When it fails to reach the the host.
   * 
   * @see RemoteException
   * 
   */
  @Override
  public void addPlace(Place place) throws RemoteException {
    places.add(place);
  }

  /**
   * This function can be called remotely to retrieve the class ArrayList of Place.
   * 
   * @return ArrayList This returns all Place.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  @Override
  public ArrayList<Place> allPlaces() throws RemoteException {
    return places;
  }

  /**
   * This function can be called remotely to retrieve a specific Place from the class ArrayList of
   * Place.
   * 
   * @param postalCode Contains string with postalCode to be used to search the Place.
   * 
   * @return Place This returns the corresponding Place.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  @Override
  public Place getPlace(String postalCode) throws RemoteException {
    for (Place place : places) {
      if (place.getPostalCode().equals(postalCode)) {
        return place;
      }
    }
    return null;
  }

  /**
   * This function can be called remotely to add an address of a PlaceManager address to the class
   * ConcurrentHashMap that contains all addresses of all PlaceManager.
   * 
   * @param replicaAddress Contains String with address (ip+port)
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  @Override
  public synchronized void addReplica(String replicaAddress) throws RemoteException {
    replicas.put(replicaAddress, new Date());
  }

  /**
   * This function can be called remotely to remove an address of a PlaceManager address from the
   * class ConcurrentHashMap that contains all addresses of all PlaceManager.
   * 
   * @param replicaAddress Contains String with address (ip+port)
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  @Override
  public synchronized void removeReplica(String replicaAddress) throws RemoteException {
    replicas.remove(replicaAddress);
  }

  /**
   * This function can be called remotely to swap the class ConcurrentHashMap that contains all
   * addresses of all PlaceManager with a new one.
   * 
   * @param replicas Contains ConcurrentHashMap to be used as new ConcurrentHashMap of the class.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  @Override
  public synchronized void addAllReplicas(ConcurrentHashMap<String, Date> replicas)
      throws RemoteException {
    this.replicas = replicas;
  }

  /**
   * This function can be called remotely to clear the class ConcurrentHashMap that contains all
   * addresses of all PlaceManager.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  @Override
  public synchronized void removeAllReplicas() throws RemoteException {
    replicas.clear();
  }

  /**
   * This function can be called remotely to clean up old replicas from the class ConcurrentHashMap
   * that contains all addresses of all PlaceManager.
   * 
   * @param maximumReplicaAge Contains the max age of the replica we should consider. Must be in
   * milliseconds.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  @Override
  public synchronized void cleanUpReplicas(Integer maximumReplicaAge) throws RemoteException {
    try {
      ConcurrentHashMap<String, Date> replicas = this.getAllReplicas();
      Iterator<Entry<String, Date>> it = replicas.entrySet().iterator();

      while (it.hasNext()) {
        Entry<String, Date> pair = it.next();
        // If replica is older than random timeout seconds
        if (new Date().getTime() - pair.getValue().getTime() > maximumReplicaAge) {
          it.remove();
        }
      }

    } catch (RemoteException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * This function can be called remotely to retrieve the class ConcurrentHashMap that contains all
   * addresses of all PlaceManager.
   * 
   * @return ConcurrentHashMap This returns all replicas.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  @Override
  public synchronized ConcurrentHashMap<String, Date> getAllReplicas() throws RemoteException {
    return replicas;
  }

  /**
   * This function can be used to send an Unicast Message to a given host.
   * 
   * @param address Contains String with address of the host.
   * 
   * @param port Contains Integer with port of the host.
   * 
   * @param message Contains string with message to be send to the host.
   * 
   * @throws IOException On Input or Output error.
   * 
   * @see IOException
   * 
   */
  @Override
  public void sendMessage(String address, Integer port, String message) throws IOException {
    // Transforms message in bytes
    byte m[] = message.getBytes();

    DatagramSocket socket = new DatagramSocket();
    InetAddress host = InetAddress.getByName(address);

    // Creates a datagram based on m[]
    DatagramPacket datagram = new DatagramPacket(m, m.length, host, port);

    // Sends datagram
    socket.send(datagram);
    socket.close();
  }

  /**
   * This function can be used to retrieve a Multicast Message of a given address and port.
   * 
   * @param address Contains String with address to listen from.
   * 
   * @param port Contains Integer with port to listen from.
   * 
   * @return String This returns the message that was sent to the Multicast Group.
   * 
   * @throws IOException On Input or Output error.
   * 
   * @see IOException
   * 
   */
  @Override
  public String listenMulticastMessage(String address, Integer port) throws IOException {
    // Joins multicast socket
    MulticastSocket mSocket = new MulticastSocket(port);
    InetAddress mcAddress = InetAddress.getByName(address);

    mSocket.setReuseAddress(true);
    mSocket.joinGroup(mcAddress);

    // Waits for new messages
    byte[] buffer = new byte[1024];
    DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);

    mSocket.receive(datagram);

    // Closes socket
    mSocket.close();

    return new String(datagram.getData(), 0, datagram.getLength());
  }

  /**
   * This function can be used to retrieve an Unicast Message of a given port from the host.
   * 
   * @param port Contains Integer with port to listen from.
   * 
   * @return String This returns the message that was sent to the host.
   * 
   * @throws IOException On Input or Output error.
   * 
   * @see IOException
   * 
   */
  @Override
  public String listenUnicastMessage(Integer port) throws IOException {
    // Creates a "listen" socket on given port
    DatagramSocket socket = new DatagramSocket(port);

    byte[] buffer = new byte[1024];
    // Prepares datagram packet to be written on given buffer
    DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);

    // Receives datagram
    socket.receive(datagram);

    // Closes socket
    socket.close();

    return new String(datagram.getData(), 0, datagram.getLength());
  }

  /**
   * This function can be called remotely to make an RPC AppendRequest. Normally, the leaders invoke
   * this method on all replicas.
   * 
   * @param request Contains the ConsensusAppendRequest with all information from the leader.
   * 
   * @return Boolean Returns true if it accepted leader RPC, false otherwise.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  @Override
  public Boolean appendRequest(ConsensusAppendRequest request) throws RemoteException {
    if (this.getCurrentTerm() > request.getLeaderTerm()) {
      // My term is higher, not my leader
      return false;
    }

    if (this.getCurrentTerm() < request.getLeaderTerm()) {
      // Reset timeout
      this.newTimeout();

      // New leader with higher term

      // Reset candidate stats
      this.setCandidateAddress(null);
      this.setCandidatePort(null);

      // Append new leader
      this.setCurrentTerm(request.getLeaderTerm());
      this.setLeaderAddress(request.getLeaderAddress());
      this.setLeaderPort(request.getLeaderPort());

      // Reset role
      this.setCurrentRole(ConsensusRole.FOLLOWER);

      return true;
    }

    // My term is the same
    if (this.getLeaderAddress() == null || this.getLeaderPort() == null) {
      // Reset timeout
      this.newTimeout();

      // I don't have leader. Give me!

      // Reset candidate stats
      this.setCandidateAddress(null);
      this.setCandidatePort(null);

      // Append new leader
      this.setCurrentTerm(request.getLeaderTerm());
      this.setLeaderAddress(request.getLeaderAddress());
      this.setLeaderPort(request.getLeaderPort());

      // Reset role
      this.setCurrentRole(ConsensusRole.FOLLOWER);

      return true;
    }
    /*
     * if (this.getLeaderAddress().equals(request.getLeaderAddress()) == false) { // Not my leader
     * address, ignore return false; }
     * 
     * if (this.getLeaderPort() != request.getLeaderPort()) { // Not my leader port, ignore return
     * false; }
     */
    // It's my actual leader, refresh
    this.newTimeout();

    // Reset role
    this.setCurrentRole(ConsensusRole.FOLLOWER);

    return true;

  }

  /**
   * This function can be called remotely to make an RPC VoteRequest. Normally, the candidates
   * invoke this method on all replicas.
   * 
   * @param request Contains the ConsensusVoteRequest with all information from the candidate.
   * 
   * @return Boolean Returns true if it accepted candidate RPC, false otherwise.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  @Override
  public Boolean voteRequest(ConsensusVoteRequest request) throws RemoteException {
    if (this.getCurrentTerm() > request.getCandidateTerm()) {
      // My term is higher, ignore
      return false;
    }

    if (this.getCurrentTerm() < request.getCandidateTerm()) {
      // New candidate with higher term

      // Reset leader starts
      this.setLeaderAddress(null);
      this.setLeaderPort(null);

      // Append new candidate
      this.setCandidateAddress(request.getCandidateAddress());
      this.setCandidatePort(request.getCandidatePort());

      // Don't update timeout
      return true;
    }

    // My term is the same, I have a leader
    return false;
  }

  /**
   * This function is used to handle the behaviour of this PlaceManager server.
   * 
   * @param replica Contains replica to be managed.
   * 
   * @see ConsensusRole
   * 
   */
  @Override
  public void handler(PlaceManager replica) {
    this.getCurrentRole().handler(replica);
  }

  /**
   * This function is used to generate a new Timeout on this PlaceManager server.
   * 
   */
  public void newTimeout() {
    this.setLastTime(System.nanoTime());
    this.setCurrentTimeout((long) (Math.random() * 5000 + 5000) * 1000000);
  }
}
