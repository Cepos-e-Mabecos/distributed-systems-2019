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

package places;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import comunication.FullAddress;

/**
 * 
 * @author <a href="https://brenosalles.com" target="_blank">Breno</a>
 *
 * @since 1.1
 * @version 1.3
 * 
 */
public interface ReplicasManagerInterface {
  /**
   * This function can be called remotely to add an address of a PlaceManager address to the class
   * ConcurrentHashMap that contains all addresses of all PlaceManager.
   * 
   * @param replicaAddress Contains FullAddress with address (ip+port)
   * 
   */
  void addReplica(FullAddress replicaAddress);

  /**
   * This function can be called remotely to remove an address of a PlaceManager address from the
   * class ConcurrentHashMap that contains all addresses of all PlaceManager.
   * 
   * @param replicaAddress Contains FullAddress with address (ip+port)
   * 
   */
  void removeReplica(FullAddress replicaAddress);

  /**
   * This function can be called remotely to swap the class ConcurrentHashMap that contains all
   * addresses of all PlaceManager with a new one.
   * 
   * @param replicas Contains ConcurrentHashMap to be used as new ConcurrentHashMap of the class.
   * 
   */
  void addAllReplicas(ConcurrentHashMap<FullAddress, Date> replicas);

  /**
   * This function can be called remotely to clear the class ConcurrentHashMap that contains all
   * addresses of all PlaceManager.
   * 
   */
  void removeAllReplicas();

  /**
   * This function can be called remotely to clean up old replicas from the class ConcurrentHashMap
   * that contains all addresses of all PlaceManager.
   * 
   * @param maximumReplicaAge Contains the max age of the replica we should consider. Must be in
   *        milliseconds.
   * 
   */
  void cleanUpReplicas(Integer maximumReplicaAge);

  /**
   * This function can be called remotely to retrieve the class ConcurrentHashMap that contains all
   * addresses of all PlaceManager.
   * 
   * @return ConcurrentHashMap This returns all replicas.
   * 
   */
  ConcurrentHashMap<FullAddress, Date> getAllReplicas();
}
