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

package com.ceposmabecos.places;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 * @author <a href="https://brenosalles.com" target="_blank">Breno</a>
 *
 * @since 1.0
 * @version 1.0
 * 
 */
public interface LogInterface extends Remote {
  /**
   * This function should be called remotely to get a Log.
   * 
   * @param logBumber Contains the number of the log that the caller wants.
   * 
   * @return {@link com.ceposmabecos.places.Log Log} This returns the wanted log.
   * 
   * @throws RemoteException When it fails to reach the the host.
   * 
   */
  Log getLog(Integer logNumber) throws RemoteException;
  
  /**
   * This function should be called remotely to get the last Log.
   *  
   * @return {@link com.ceposmabecos.places.Log Log} This returns the last log.
   * 
   * @throws RemoteException When it fails to reach the the host.
   * 
   */
  Log getLastLog() throws RemoteException;
}
