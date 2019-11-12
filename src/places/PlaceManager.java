package places;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import comunication.UDPInterface;

public class PlaceManager extends UnicastRemoteObject implements PlacesListInterface, UDPInterface {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /*
   * Attributes
   */
  private ArrayList<Place> places = new ArrayList<Place>();
  private ArrayList<String> replicas = new ArrayList<String>();
  private Integer serverPort;

  /*
   * Constructor
   */
  public PlaceManager(Integer serverPort) throws RemoteException {
    super(0);
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
   * Sends a multicast message
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
   * Listens to MulticastMessages on given address and port. When a message is received, processes
   * that message and sends a message with their own ip. Every
   * 
   * @param address: contains String with address
   * 
   * @param port: contains Integer with port
   * 
   */
  @Override
  public void listenNewHosts(String address, Integer port) throws IOException {
    while (true) {
      // Joins multicast socket
      MulticastSocket mSocket = new MulticastSocket(port);
      InetAddress mcAddress = InetAddress.getByName(address);
      mSocket.joinGroup(mcAddress);
      
      // Waits for new messages
      byte[] buffer = new byte[1024];
      DatagramPacket message = new DatagramPacket(buffer, buffer.length);
      mSocket.receive(message);

      // Processes new messages
      String response = new String(message.getData());
      processMessages(response);
      
      // Closes socket
      mSocket.close();
    }
  }

  private void processMessages(String message) {
    if (replicas.contains(message)) {
      return;
    }

    System.out.println("Adicionei " + message);
    replicas.add(message);
  }
}
