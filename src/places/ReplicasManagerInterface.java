package places;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public interface ReplicasManagerInterface extends Remote {
  /**
   * This function can be called remotely to add an address of a PlaceManager address to the class
   * ConcurrentHashMap that contains all addresses of all PlaceManager.
   * 
   * @param replicaAddress Contains String with address (ip+port)
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  void addReplica(String replicaAddress) throws RemoteException;

  /**
   * This function can be called remotely to remove an address of a PlaceManager address from the
   * class ConcurrentHashMap that contains all addresses of all PlaceManager.
   * 
   * @param replicaAddress Contains String with address (ip+port)
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  void removeReplica(String replicaAddress) throws RemoteException;

  /**
   * This function can be called remotely to swap the class ConcurrentHashMap that contains all
   * addresses of all PlaceManager with a new one.
   * 
   * @param replicas Contains ConcurrentHashMap to be used as new ConcurrentHashMap of the class.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  void addAllReplicas(ConcurrentHashMap<String, Date> replicas) throws RemoteException;

  /**
   * This function can be called remotely to clear the class ConcurrentHashMap that contains all
   * addresses of all PlaceManager.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  void removeAllReplicas() throws RemoteException;

  /**
   * This function can be called remotely to clean up old replicas from the class ConcurrentHashMap
   * that contains all addresses of all PlaceManager.
   * 
   * @param maximumReplicaAge Contains the max age of the replica we should consider. Must be in
   * milliseconds.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  void cleanUpReplicas(Integer maximumReplicaAge) throws RemoteException;

  /**
   * This function can be called remotely to retrieve the class ConcurrentHashMap that contains all
   * addresses of all PlaceManager.
   * 
   * @return ConcurrentHashMap This returns all replicas.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  ConcurrentHashMap<String, Date> getAllReplicas() throws RemoteException;
}
