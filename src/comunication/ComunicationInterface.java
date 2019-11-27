package comunication;

import java.io.IOException;

public interface ComunicationInterface {
  /**
   * This function should be used to send an Unicast Message to a given host.
   * 
   * @param fullAddress Contains FullAddress with address of the host.
   * 
   * @param message Contains ComunicationMessage with message to be send to the host.
   * 
   * @throws IOException On Input or Output error.
   * 
   * @see IOException
   * 
   */
  void sendMessage(FullAddress fullAddress, ComunicationMessage message) throws IOException;

  /**
   * This function should be used to retrieve a Multicast Message of a given address and port.
   * 
   * @param fullAddress Contains FullAddress with address and port to listen from.
   * 
   * @return ComunicationMessage This returns the message that was sent to the Multicast Group.
   * 
   * @throws IOException On Input or Output error.
   * 
   * @throws ClassNotFoundException  When reading a class outputs error.
   * 
   * @see IOException
   * 
   */
  ComunicationMessage listenMulticastMessage(FullAddress fullAddress) throws IOException, ClassNotFoundException;

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
