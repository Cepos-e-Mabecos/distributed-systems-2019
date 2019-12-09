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

package com.ceposmabecos.comunication;

import java.io.Serializable;

/**
 * 
 * @author <a href="https://brenosalles.com" target="_blank">Breno</a>
 *
 * @since 1.2
 * @version 1.3
 * 
 */
public class ComunicationHeartbeat implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 8366596922474534631L;
  /*
   * Attributes
   */
  private String message;
  private Integer term;
  private FullAddress fullAddress;
  private Object object;

  /*
   * Constructors
   */

  public ComunicationHeartbeat(String message, Integer term, FullAddress fullAddress) {
    this.message = message;
    this.term = term;
    this.fullAddress = fullAddress;
  }

  public ComunicationHeartbeat(String message, Integer term, FullAddress fullAddress,
      Object object) {
    this.message = message;
    this.term = term;
    this.fullAddress = fullAddress;
    this.object = object;
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

  public Object getObject() {
    return object;
  }

  public void setObject(Object object) {
    this.object = object;
  }

  /*
   * String toString
   */
  @Override
  public String toString() {
    return this.getMessage() + "," + this.getTerm() + "," + this.getFullAddress().toString();
  }
}
