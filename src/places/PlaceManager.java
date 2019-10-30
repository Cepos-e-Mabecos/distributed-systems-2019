package places;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class PlaceManager extends UnicastRemoteObject implements PlacesListInterface {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /*
   * Attributes
   */
  private ArrayList<Place> places = new ArrayList<Place>();

  /*
   * Constructor
   */
  protected PlaceManager() throws RemoteException {
    super(0);
  }

  /*
   * This function adds a place to the arraylist of places.
   * 
   * @param place: contains object of type Place to be added.
   */
  @Override
  public void addPlace(Place place) throws RemoteException {
    places.add(place);
  }

  /*
   * This function returns an arraylist of places.
   * 
   * @return ArrayList<Place>
   */
  @Override
  public ArrayList<Place> allPlaces() throws RemoteException {
    return places;
  }

  /*
   * This function returns an object of type Place based on @param.
   * 
   * @param postalCode: contains string with postalCode to be used.
   * 
   * @return Place
   */
  @Override
  public Place getPlace(String postalCode) throws RemoteException {
    for (Place place : places) {
      if (place.getPostalCode().equals(postalCode)) {
        return place;
      }
    }
    return null;
  }
}
