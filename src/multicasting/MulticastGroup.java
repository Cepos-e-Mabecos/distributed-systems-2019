package multicasting;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastGroup {
  /*
   * Attributes
   */
  private String address;
  private Integer port;

  /*
   * Constructor
   */
  public MulticastGroup(String address, Integer port) {
    this.address = address;
    this.port = port;
  }

  /*
   * Joins a multicast group and starts listening to messages on given address+port
   */
  public void listenMulticastGroup() {
    MulticastSocket mSocket = null;
    InetAddress mcAddress = null;

    try {
      mcAddress = InetAddress.getByName(address);
      mSocket = new MulticastSocket(port);
      System.out.println("Multicast receiver running at " + mSocket.getLocalSocketAddress());

      mSocket.joinGroup(mcAddress);

      byte[] buffer = new byte[1024];

      while (true) {
        DatagramPacket message = new DatagramPacket(buffer, buffer.length);
        mSocket.receive(message);

        System.out.println(new String(message.getData()));
      }

    } catch (IOException e) {
      System.out.println("Error binding address");
    }
  }
}
