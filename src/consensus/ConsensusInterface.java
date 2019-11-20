package consensus;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ConsensusInterface extends Remote {
  Boolean appendRequest(ConsensusAppendRequest request) throws RemoteException;

  Boolean voteRequest(ConsensusVoteRequest request) throws RemoteException;
}
