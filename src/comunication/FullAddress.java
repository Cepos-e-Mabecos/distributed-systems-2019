package comunication;

import java.io.Serializable;

public class FullAddress implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -2629968170882400707L;

  /*
   * Attributes
   */
  private String address;
  private Integer port;

  /*
   * Constructor
   */
  public FullAddress(String address, Integer port) {
    this.address = address;
    this.port = port;
  }

  /*
   * Getters & Setters
   */
  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  /*
   * String toString
   */
  @Override
  public String toString() {
    return this.getAddress() + ":" + this.getPort();
  }

  /*
   * hashCode
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((address == null) ? 0 : address.hashCode());
    result = prime * result + ((port == null) ? 0 : port.hashCode());
    return result;
  }

  /*
   * Equals
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (this.getClass() != obj.getClass()) {
      return false;
    }

    FullAddress casted = (FullAddress) obj;
    if (this.address == null) {
      if (casted.address != null) {
        return false;
      }
    }

    if (this.port == null) {
      if (casted.port != null) {
        return false;
      }
    }

    if (this.address.equals(casted.address) == false) {
      return false;
    }

    if (this.port == casted.port) {
      return false;
    }
    return true;
  }
}
