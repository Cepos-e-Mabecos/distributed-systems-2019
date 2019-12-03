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

package comunication;

import java.io.IOException;

/**
 * 
 * @author <a href="https://brenosalles.com" target="_blank">Breno</a>
 *
 * @since 1.0
 * @version 1.2
 * 
 */
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
   */
  void sendMessage(FullAddress fullAddress, ComunicationHeartbeat message) throws IOException;

  /**
   * This function should be used to retrieve a Multicast Message of a given address and port.
   * 
   * @param fullAddress Contains FullAddress with address and port to listen from.
   * 
   * @return ComunicationMessage This returns the message that was sent to the Multicast Group.
   * 
   * @throws IOException On Input or Output error.
   * 
   * @throws ClassNotFoundException When reading a class outputs error.
   * 
   */
  ComunicationHeartbeat listenMulticastMessage(FullAddress fullAddress)
      throws IOException, ClassNotFoundException;

  /**
   * This function should be used to retrieve an Unicast Message of a given port from the host.
   * 
   * @param port Contains Integer with port to listen from.
   * 
   * @return String This returns the message that was sent to the host.
   * 
   * @throws IOException On Input or Output error.
   * 
   */
  String listenUnicastMessage(Integer port) throws IOException;
}
