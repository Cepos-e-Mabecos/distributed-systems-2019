package places;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import comunication.ComunicationInterface;
import consensus.ConsensusRole;

public class PlaceManager extends UnicastRemoteObject
    implements PlacesListInterface, ReplicasManagerInterface, ComunicationInterface {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /*
   * Attributes
   */
  private ArrayList<Place> places = new ArrayList<Place>();
  private ArrayList<String> replicas = new ArrayList<String>();
  private ConsensusRole role;
  private Integer serverPort;

  /*
   * Constructor
   */
  public PlaceManager(Integer serverPort) throws RemoteException {
    super(0);
    this.serverPort = serverPort;
    this.role = ConsensusRole.FOLLOWER;
  }

  // Getters & Setters
  public synchronized ConsensusRole getRole() {
    return role;
  }

  public synchronized void setRole(ConsensusRole role) {
    this.role = role;
  }

  public synchronized ArrayList<String> getReplicas() {
    return replicas;
  }

  public synchronized void setReplicas(ArrayList<String> replicas) {
    this.replicas = replicas;
  }

  public Integer getServerPort() {
    return serverPort;
  }

  public void setServerPort(Integer serverPort) {
    this.serverPort = serverPort;
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
   * Adds a replica address to the arrayList of replicas
   * 
   * @param replicaAddress: contains String with address (ip+port)
   */
  @Override
  public synchronized void addReplica(String replicaAddress) throws RemoteException {
    replicas.add(replicaAddress);
  }

  /*
   * Removes a replica address from the arrayList of replicas
   * 
   * @param replicaAddress: contains String with address (ip+port)
   */
  @Override
  public synchronized void removeReplica(String replicaAddress) throws RemoteException {
    replicas.remove(replicaAddress);
  }

  /*
   * Adds all replicas from an arrayList to the class arrayList of replicas
   * 
   * @param replicas: contains arrayList with all replicas
   */
  @Override
  public synchronized void addAllReplicas(ArrayList<String> replicas) throws RemoteException {
    this.replicas = replicas;
  }

  /*
   * Clears class arrayList of replicas
   * 
   */
  @Override
  public synchronized void removeAllReplicas() throws RemoteException {
    replicas.clear();
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
}
