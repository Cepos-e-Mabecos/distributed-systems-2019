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
import java.util.HashMap;

/**
 * 
 * @author <a href="https://brenosalles.com" target="_blank">Breno</a>
 *
 * @since 1.0
 * @version 1.1
 * 
 */
public interface PlacesListInterface extends Remote {
  /**
   * This function should be called remotely to add a Place to the class HashMap of &#60;String, Place&#62;.
   * 
   * @param place Contains object of type {@link com.ceposmabecos.places.Place Place} to be added.
   * 
   * @throws RemoteException When it fails to reach the the host.
   * 
   */
  void addPlace(Place place) throws RemoteException;
  
  /**
   * This function should be called remotely to remove a Place to the class HashMap of &#60;String, Place&#62;.
   * 
   * @param postalCode Contains string with postalCode to be used to remove the Place.
   * 
   * @return {@link com.ceposmabecos.places.Place Place} This returns the deleted Place.
   * 
   * @throws RemoteException When it fails to reach the the host.
   * 
   */
  Place removePlace(String postalCode) throws RemoteException;

  /**
   * This function should be called remotely to retrieve a specific Place from the class HashMap of &#60;String, Place&#62;.
   * 
   * @param postalCode Contains string with postalCode to be used to search the Place.
   * 
   * @return {@link com.ceposmabecos.places.Place} This returns the corresponding Place.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   */
  Place getPlace(String postalCode) throws RemoteException;

  /**
   * This function should be called remotely to retrieve the class HashMap of &#60;String, Place&#62;.
   * 
   * @return HashMap This returns all Place.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  HashMap<String, Place> getAllPlaces() throws RemoteException;

  /**
   * This function should be called remotely to change the class HashMap of &#60;String, Place&#62;.
   * 
   * @param places Contains HashMap of &#60;String, {@link com.ceposmabecos.places.Place Place}&#62;.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  void setAllPlaces(HashMap<String, Place> places) throws RemoteException;
}
