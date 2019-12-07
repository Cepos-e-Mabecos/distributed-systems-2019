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

package comunication;

import java.io.Serializable;

/**
 * 
 * @author <a href="https://brenosalles.com" target="_blank">Breno</a>
 *
 * @since 1.1
 * @version 1.3
 * 
 */
public class FullAddress implements Serializable {
  private static final long serialVersionUID = -8778446896284234376L;

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
   * HashCode
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
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FullAddress other = (FullAddress) obj;
    if (address == null) {
      if (other.address != null)
        return false;
    } else if (address.equals(other.address) == false)
      return false;
    if (port == null) {
      if (other.port != null)
        return false;
    } else if (port.equals(other.port) == false)
      return false;
    return true;
  }
}
