package places;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;

public interface ReplicasManagerInterface extends Remote {
  void addReplica(String replicaAddress) throws RemoteException;

  void removeReplica(String replicaAddress) throws RemoteException;

  void addAllReplicas(HashMap<String, Date> replicas) throws RemoteException;

  void removeAllReplicas() throws RemoteException;
  
  HashMap<String, Date> getAllReplicas() throws RemoteException;
}
