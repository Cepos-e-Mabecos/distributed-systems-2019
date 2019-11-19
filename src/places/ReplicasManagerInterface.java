package places;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ReplicasManagerInterface extends Remote {
  void addReplica(String replicaAddress) throws RemoteException;
  void removeReplica(String replicaAddress) throws RemoteException;
  void addAllReplicas(ArrayList<String> replicas) throws RemoteException;
  void removeAllReplicas() throws RemoteException;
}
