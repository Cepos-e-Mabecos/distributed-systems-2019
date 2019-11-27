package places;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import comunication.FullAddress;

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
   * milliseconds.
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
