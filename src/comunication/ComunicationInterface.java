package comunication;

import java.io.IOException;

public interface ComunicationInterface {
  /**
   * This function should be used to send an Unicast Message to a given host.
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
  void sendMessage(String address, Integer port, String message) throws IOException;

  /**
   * This function should be used to retrieve a Multicast Message of a given address and port.
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
  String listenMulticastMessage(String address, Integer port) throws IOException;

  /**
   * This function should be used to retrieve an Unicast Message of a given port from the host.
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
  String listenUnicastMessage(Integer port) throws IOException;
}
