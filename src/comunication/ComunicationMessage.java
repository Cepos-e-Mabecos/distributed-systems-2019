package comunication;

import java.io.Serializable;
import java.util.ArrayList;
import places.Place;

public class ComunicationMessage implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -7581809375014948234L;

  /*
   * Attributes
   */
  private String message;
  private Integer term;
  private FullAddress fullAddress;
  private ArrayList<Place> places;

  /*
   * Constructors
   */

  public ComunicationMessage(String message, Integer term, FullAddress fullAddress) {
    this.message = message;
    this.term = term;
    this.fullAddress = fullAddress;
  }
  
  public ComunicationMessage(String message, Integer term, FullAddress fullAddress, ArrayList<Place> places) {
    this.message = message;
    this.term = term;
    this.fullAddress = fullAddress;
    this.places = places;
  }

  /*
   * Getters & Setters
   */
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Integer getTerm() {
    return term;
  }

  public void setTerm(Integer term) {
    this.term = term;
  }

  public FullAddress getFullAddress() {
    return fullAddress;
  }

  public void setFullAddress(FullAddress fullAddress) {
    this.fullAddress = fullAddress;
  }
  
  public ArrayList<Place> getPlaces() {
    return places;
  }
  
  public void setPlaces(ArrayList<Place> places) {
    this.places = places;
  }

  /*
   * String toString
   */
  @Override
  public String toString() {
    return this.getMessage() + "," + this.getTerm() + "," + this.getFullAddress().toString();
  }
}
