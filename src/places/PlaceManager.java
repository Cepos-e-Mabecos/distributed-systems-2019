package places;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import comunication.ComunicationMessage;
import comunication.FullAddress;
import consensus.ConsensusHandlerInterface;
import consensus.ConsensusRole;

public class PlaceManager extends UnicastRemoteObject implements PlacesListInterface,
    ReplicasManagerInterface, ComunicationInterface, ConsensusHandlerInterface {

  /**
   * 
   */
  private static final long serialVersionUID = 4883086976774125339L;
  /*
   * PlaceManager Attributes
   */
  private ArrayList<Place> places = new ArrayList<Place>();
  private ConcurrentHashMap<FullAddress, Date> replicas =
      new ConcurrentHashMap<FullAddress, Date>();
  private FullAddress multicastAddress;
  private FullAddress localAddress;

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
  private FullAddress leaderAddress;
  private FullAddress candidateAddress;

  /*
   * Constructor
   */
  public PlaceManager(FullAddress multicastAddress, FullAddress localAddress)
      throws RemoteException, UnknownHostException {
    // PlaceManager
    super(0);
    this.multicastAddress = multicastAddress;
    this.localAddress = localAddress;

    // Consensus Server Attributes
    this.currentRole = ConsensusRole.FOLLOWER;
    this.currentTerm = 0;
    this.currentTimeout = (long) (Math.random() * 5000 + 5000) * 1000000;

    // Follower Attributes
    this.lastTime = System.nanoTime();
    this.leaderAddress = null;
    this.candidateAddress = null;
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

  /*
   * String toString
   */
  @Override
  public String toString() {
    return "\nRole: " + this.getCurrentRole() + "\nTerm: " + this.getCurrentTerm()
        + "\nReplica FullAddress: " + this.getLocalAddress().toString()
        + "\nLeader FullAddress: " + (this.getLeaderAddress() == null ? "null" : this.getLeaderAddress().toString());
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
   * @param replicaAddress Contains FullAddress with address (ip+port)
   * 
   */
  @Override
  public synchronized void addReplica(FullAddress replicaAddress) {
    replicas.replace(replicaAddress, new Date());
  }

  /**
   * This function can be called remotely to remove an address of a PlaceManager address from the
   * class ConcurrentHashMap that contains all addresses of all PlaceManager.
   * 
   * @param replicaAddress Contains FullAddress with address (ip+port)
   * 
   */
  @Override
  public synchronized void removeReplica(FullAddress replicaAddress) {
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
  public synchronized void addAllReplicas(ConcurrentHashMap<FullAddress, Date> replicas) {
    this.replicas = replicas;
  }

  /**
   * This function can be called remotely to clear the class ConcurrentHashMap that contains all
   * addresses of all PlaceManager.
   * 
   */
  @Override
  public synchronized void removeAllReplicas() {
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
  public synchronized void cleanUpReplicas(Integer maximumReplicaAge) {
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
  public synchronized ConcurrentHashMap<FullAddress, Date> getAllReplicas() {
    return replicas;
  }

  /**
   * This function can be used to send an Unicast Message to a given host.
   * 
   * @param fullAddress Contains FullAddress with address of the host.
   * 
   * @param message Contains string with message to be send to the host.
   * 
   * @throws IOException On Input or Output error.
   * 
   * @see IOException
   * 
   */
  @Override
  public void sendMessage(FullAddress fullAddress, ComunicationMessage message)
      throws IOException {
    InetAddress host = InetAddress.getByName(fullAddress.getAddress());
    DatagramSocket socket = new DatagramSocket();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(baos);
    out.writeObject(message);

    // Transforms message in bytes
    byte m[] = baos.toByteArray();

    // Creates a datagram based on m[]
    DatagramPacket datagram = new DatagramPacket(m, m.length, host, fullAddress.getPort());

    // Sends datagram
    socket.send(datagram);
    socket.close();
  }

  /**
   * This function can be used to retrieve a Multicast Message of a given address and port.
   * 
   * @param fullAddress Contains FullAddres with address and port to listen from.
   * 
   * @return ComunicationMessage This returns the message that was sent to the Multicast Group.
   * 
   * @throws IOException On Input or Output error.
   * 
   * @throws ClassNotFoundException When reading a class outputs error.
   * 
   * @see IOException
   * 
   */
  @Override
  public ComunicationMessage listenMulticastMessage(FullAddress fullAddress)
      throws IOException, ClassNotFoundException {
    // Joins multicast socket
    MulticastSocket mSocket = new MulticastSocket(fullAddress.getPort());
    InetAddress mcAddress = InetAddress.getByName(fullAddress.getAddress());

    mSocket.setReuseAddress(true);
    mSocket.joinGroup(mcAddress);

    // Waits for new messages
    byte[] buffer = new byte[1024];
    DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);

    mSocket.receive(datagram);

    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
    ObjectInputStream ois = new ObjectInputStream(bais);

    ComunicationMessage message = (ComunicationMessage) ois.readObject();

    // Closes socket
    mSocket.close();

    return message;
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
