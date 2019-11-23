package consensus;

import places.PlaceManager;

public interface ConsensusHandlerInterface {
  /**
   * This function should be used to handle the behaviour of a PlaceManager server.
   * 
   * @param replica Contains PlaceManager to be handled.
   * 
   */
  void handler(PlaceManager replica);
}
