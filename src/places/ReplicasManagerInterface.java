package places;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public interface ReplicasManagerInterface extends Remote {
  void addReplica(String replicaAddress) throws RemoteException;

  void removeReplica(String replicaAddress) throws RemoteException;

  void addAllReplicas(ConcurrentHashMap<String, Date> replicas) throws RemoteException;

  void removeAllReplicas() throws RemoteException;

  ConcurrentHashMap<String, Date> getAllReplicas() throws RemoteException;
}
