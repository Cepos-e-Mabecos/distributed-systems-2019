package places;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

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
   * This function should be called remotely to retrieve the class ArrayList of Place.
   * 
   * @return ArrayList This returns all Place.
   * 
   * @throws RemoteException When it fails to reach the host.
   * 
   * @see RemoteException
   * 
   */
  ArrayList<Place> allPlaces() throws RemoteException;

  /**
   * This function should be called remotely to retrieve a specific Place from the class ArrayList of
   * Place.
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
}
