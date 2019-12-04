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

package consensus;

import java.io.IOException;
import comunication.ComunicationHeartbeat;
import places.PlaceManager;

/**
 * 
 * @author <a href="https://brenosalles.com" target="_blank">Breno</a>
 *
 * @since 1.2
 * @version 1.3
 * 
 */
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
  public void run() {// Announces as Candidate
    try {
      replica.sendMessage(replica.getMulticastAddress(), new ComunicationHeartbeat("CANDIDATE",
          replica.getCurrentTerm() + 1, replica.getLocalAddress()));
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
    while (running == true) {

      if (System.nanoTime() - replica.getLastTime() > replica.getCurrentTimeout()) {
        this.terminate();
      }
    }
  }

}
