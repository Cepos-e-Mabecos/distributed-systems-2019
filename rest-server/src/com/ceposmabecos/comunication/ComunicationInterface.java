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

package com.ceposmabecos.comunication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * 
 * @author <a href="https://brenosalles.com" target="_blank">Breno</a>
 *
 * @since 1.0
 * @version 1.4
 * 
 */
public interface ComunicationInterface {
  /**
   * This function should be used to send an Unicast Message to a given host.
   * 
   * @param fullAddress Contains {@link com.ceposmabecos.comunication.FullAddress FullAddress} with
   *        address of the host.
   * 
   * @param message Contains {@link com.ceposmabecos.comunication.ComunicationHeartbeat message} to
   *        be send.
   * 
   * @throws IOException On Input or Output error.
   * 
   */
  static void sendMessage(FullAddress fullAddress, ComunicationHeartbeat message)
      throws IOException {
    DatagramSocket socket = new DatagramSocket();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(baos);
    out.writeObject(message);

    // Transforms message in bytes
    byte m[] = baos.toByteArray();

    // Creates a datagram based on m[]
    DatagramPacket datagram = new DatagramPacket(m, m.length,
        InetAddress.getByName(fullAddress.getAddress()), fullAddress.getPort());

    // Sends datagram
    socket.send(datagram);
    socket.close();
  }

  /**
   * This function should be used to retrieve a Multicast Message of a given address and port.
   * 
   * @param fullAddress Contains {@link com.ceposmabecos.comunication.FullAddress FullAddress} with
   *        address and port to listen from.
   * 
   * @return {@link com.ceposmabecos.comunication.ComunicationHeartbeat ComunicationHeartbeat} This
   *         returns the message that was sent to the Multicast Group.
   * 
   * @throws IOException On Input or Output error.
   * 
   * @throws ClassNotFoundException When reading a class outputs error.
   * 
   */
  static ComunicationHeartbeat listenMulticastMessage(FullAddress fullAddress)
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

    ComunicationHeartbeat message = (ComunicationHeartbeat) ois.readObject();

    // Closes socket
    mSocket.close();

    return message;
  }

  /**
   * This function should be used to retrieve an Unicast Message of a given port from the host.
   * 
   * @param port Contains Integer with port to listen from.
   * 
   * @return {@link com.ceposmabecos.comunication.ComunicationHeartbeat ComunicationHeartbeat} This
   *         returns the message that was sent to the host.
   * 
   * @throws IOException On Input or Output error.
   * 
   * @throws ClassNotFoundException
   * 
   */
  default ComunicationHeartbeat listenUnicastMessage(Integer port)
      throws IOException, ClassNotFoundException {
    // Creates a "listen" socket on given port
    DatagramSocket socket = new DatagramSocket(port);

    byte[] buffer = new byte[1024];
    // Prepares datagram packet to be written on given buffer
    DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);

    // Receives datagram
    socket.receive(datagram);

    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
    ObjectInputStream ois = new ObjectInputStream(bais);

    ComunicationHeartbeat message = (ComunicationHeartbeat) ois.readObject();

    // Closes socket
    socket.close();

    return message;
  }
}
