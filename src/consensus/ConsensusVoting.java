package consensus;

import java.io.IOException;
import comunication.ComunicationMessage;
import places.PlaceManager;

public class ConsensusVoting implements Runnable {
  private volatile Boolean running = true;
  private PlaceManager replica;

  public ConsensusVoting(PlaceManager replica) {
    this.replica = replica;
  }

  public synchronized void terminate() {
    running = false;
  }

  @Override
  public void run() {
    while (running == true) {
      // Announces as Candidate
      try {
        replica.sendMessage(replica.getMulticastAddress(), new ComunicationMessage("CANDIDATE",
            replica.getCurrentTerm() + 1, replica.getLocalAddress()));
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }

      if (System.nanoTime() - replica.getLastTime() > replica.getCurrentTimeout()) {
        running = false;
      }
    }
  }

}
