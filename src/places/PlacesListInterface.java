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

package places;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * 
 * @author <a href="https://brenosalles.com" target="_blank">Breno</a>
 *
 * @since 1.0
 * @version 1.0
 * 
 */
public interface PlacesListInterface extends Remote {
  /**
   * This function should be called remotely to add a Place to the class ArrayList of Places.
   * 
   * @param place Contains object of type Place to be added.
   * 
   * @throws RemoteException When it fails to reach the the host.
   * 
   * @see RemoteException
   * 
   */
  void addPlace(Place place) throws RemoteException;

  /**
   * This function should be called remotely to retrieve a specific Place from the class ArrayList
   * of Place.
   * 
   * @param postalCode Contains string with postalCode to be used to search the Place.
   * 
   * @return Place This returns the corresponding Place.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  Place getPlace(String postalCode) throws RemoteException;

  /**
   * This function should be called remotely to retrieve the class ArrayList of Place.
   * 
   * @return ArrayList This returns all Place.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  ArrayList<Place> getAllPlaces() throws RemoteException;

  /**
   * This function should be called remotely to change the class ArrayList of Place.
   * 
   * @param places Contains ArrayList with all Place.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  void setAllPlaces(ArrayList<Place> places) throws RemoteException;
}
