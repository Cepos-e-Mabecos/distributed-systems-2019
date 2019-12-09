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

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import places.PlaceManager;

/**
 * 
 * @author <a href="https://brenosalles.com" target="_blank">Breno</a>
 *
 * @since 1.0
 * @version 1.5
 * 
 */
public class RMIServer {
  public static void main(String[] args) throws RemoteException, UnknownHostException {

    String multicastAddress = args[0];
    Integer multicastPort = Integer.parseInt(args[1]);
    String thisReplicaAddress = args[2];
    Integer thisReplicaPort = Integer.parseInt(args[3]);

    startRMIServer(multicastAddress, multicastPort, thisReplicaAddress, thisReplicaPort).start();
  }

  private static PlaceManager startRMIServer(String multicastAddress, Integer multicastPort,
      String thisReplicaAddress, Integer thisReplicaPort)
      throws RemoteException, UnknownHostException {
    Registry r = null;
    PlaceManager replica = null;
    try {
      System.out.println("Creating registry on port: " + thisReplicaPort);
      r = LocateRegistry.createRegistry(thisReplicaPort);
    } catch (RemoteException a) {
      System.out.println(thisReplicaPort + " is already used.");
    } finally {
      System.out.println("Getting registry on port: " + thisReplicaPort);
      r = LocateRegistry.getRegistry(thisReplicaPort);
    }
    replica =
        new PlaceManager(multicastAddress, multicastPort, thisReplicaAddress, thisReplicaPort);
    r.rebind("placelist", replica);
    System.out.println("PlaceManager running on port: " + thisReplicaPort);
    return replica;
  }
}
