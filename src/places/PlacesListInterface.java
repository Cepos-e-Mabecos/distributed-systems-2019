package places;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface PlacesListInterface extends Remote {
  void addPlace(Place place) throws RemoteException;
  ArrayList<Place> allPlaces() throws RemoteException;
  Place getPlace(String postalCode) throws RemoteException;
}
