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
import java.util.HashMap;
import comunication.ComunicationInterface;
import consensus.ConsensusAppendRequest;
import consensus.ConsensusInterface;
import consensus.ConsensusRole;
import consensus.ConsensusVoteRequest;

public class PlaceManager extends UnicastRemoteObject implements PlacesListInterface,
    ReplicasManagerInterface, ComunicationInterface, ConsensusInterface {

  /**
   * 
   */
  private static final long serialVersionUID = 4883086976774125339L;
  /*
   * PlaceManager Attributes
   */
  private ArrayList<Place> places = new ArrayList<Place>();
  private HashMap<String, Date> replicas = new HashMap<String, Date>();
  private String localAddress;
  private Integer localPort;

  // Consensus Server Attributes
  private ConsensusRole currentRole;
  private Integer currentTerm;

  // Follower Attributes
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

  // Getters & Setters PlaceManager
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

  // Getters & Setters ConsensusServer
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

  // String toString
  public String toString() {
    return "Role: " + this.getCurrentRole() + "\nTerm: " + this.getCurrentTerm() + "\nFullAddress: "
        + this.getLocalAddress() + ":" + this.getLocalPort();
  }

  /*
   * This function adds a place to the arraylist of places.
   * 
   * @param place: contains object of type Place to be added.
   */
  @Override
  public void addPlace(Place place) throws RemoteException {
    places.add(place);
  }

  /*
   * This function returns an arraylist of places.
   * 
   * @return ArrayList<Place>
   */
  @Override
  public ArrayList<Place> allPlaces() throws RemoteException {
    return places;
  }

  /*
   * This function returns an object of type Place based on @param.
   * 
   * @param postalCode: contains string with postalCode to be used.
   * 
   * @return Place
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

  /*
   * Adds a replica address to the HashMap of replicas
   * 
   * @param replicaAddress: contains String with address (ip+port)
   */
  @Override
  public synchronized void addReplica(String replicaAddress) throws RemoteException {
    replicas.put(replicaAddress, new Date());
  }

  /*
   * Removes a replica address from the HashMap of replicas
   * 
   * @param replicaAddress: contains String with address (ip+port)
   */
  @Override
  public synchronized void removeReplica(String replicaAddress) throws RemoteException {
    replicas.remove(replicaAddress);
  }

  /*
   * Adds all replicas from an HashMap to the class HashMap of replicas
   * 
   * @param replicas: contains HashMap with all replicas
   */
  @Override
  public synchronized void addAllReplicas(HashMap<String, Date> replicas) throws RemoteException {
    this.replicas = replicas;
  }

  /*
   * Clears class HashMap of replicas
   * 
   */
  @Override
  public synchronized void removeAllReplicas() throws RemoteException {
    replicas.clear();
  }

  /*
   * Serves to return all replicas from the class HashMap
   * 
   * @return: an HashMap of replicas
   * 
   */
  @Override
  public synchronized HashMap<String, Date> getAllReplicas() throws RemoteException {
    return replicas;
  }

  /*
   * Sends a udp message
   * 
   * @param address: contains String with address
   * 
   * @param port: contains Integer with port
   * 
   * @param message: contains string with message to be send
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

  /*
   * Listens to MulticastMessages on given address and port. that message.
   * 
   * @param address: contains String with address
   * 
   * @param port: contains Integer with port
   * 
   * @return: String with message
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

  /*
   * Listens to UnicastMessages on given address and port. When a message is received, processes
   * that message.
   * 
   * @param address: contains String with address
   * 
   * @param port: contains Integer with port
   * 
   * @return: String with message
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

  // Implementations
  @Override
  public Boolean appendRequest(ConsensusAppendRequest request) throws RemoteException {
    if (this.getCurrentTerm() > request.getLeaderTerm()) {
      // My term is higher
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
    if (this.getLeaderAddress().equals(request.getLeaderAddress()) == false) {
      // Not my leader address, ignore
      return false;
    }

    if (this.getLeaderPort() != request.getLeaderPort()) {
      // Not my leader port, ignore
      return false;
    }
*/
    // It's my actual leader, refresh
    this.newTimeout();

    // Reset role
    this.setCurrentRole(ConsensusRole.FOLLOWER);

    return true;

  }

  @Override
  public Boolean voteRequest(ConsensusVoteRequest request) throws RemoteException {
    if (this.getCurrentTerm() > request.getCandidateTerm()) {
      // My term is higher
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

  public void newTimeout() {
    this.setLastTime(System.nanoTime());
    this.setCurrentTimeout((long) (Math.random() * 5000 + 5000) * 1000000);
  }
}
