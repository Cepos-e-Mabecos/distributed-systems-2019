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

package com.ceposmabecos.frontend;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.ceposmabecos.comunication.ComunicationHeartbeat;
import com.ceposmabecos.comunication.ComunicationInterface;
import com.ceposmabecos.comunication.FullAddress;
import com.ceposmabecos.places.Place;
import com.ceposmabecos.places.PlacesListInterface;
import spark.Request;
import spark.Response;

/**
 * This is the core FrontEnd
 * 
 * @author <a href="https://brenosalles.com" target="_blank">Breno</a>
 *
 * @since 1.0
 * @version 1.2
 * 
 */
public class FrontEnd {
  /*
   * Attributes
   */
  private FullAddress localAddress;
  private FullAddress multicastAddress;
  private FullAddress leaderAddress;
  private ConcurrentHashMap<FullAddress, Date> replicas =
      new ConcurrentHashMap<FullAddress, Date>();
  private final Integer multicastTimeout = 3000;
  private Integer currentTerm = 0;

  /*
   * Constructor
   */
  public FrontEnd(String multicastAddress, Integer multicastPort, String localAddress,
      Integer localPort) {
    this.multicastAddress = new FullAddress(multicastAddress, multicastPort);
    this.localAddress = new FullAddress(localAddress, localPort);
  }

  /*
   * Getters & Setters
   */
  public FullAddress getLocalAddress() {
    return localAddress;
  }

  public void setLocalAddress(FullAddress localAddress) {
    this.localAddress = localAddress;
  }

  public FullAddress getMulticastAddress() {
    return multicastAddress;
  }

  public void setMulticastAddress(FullAddress multicastAddress) {
    this.multicastAddress = multicastAddress;
  }

  public FullAddress getLeaderAddress() {
    return leaderAddress;
  }

  public void setLeaderAddress(FullAddress leaderAddress) {
    this.leaderAddress = leaderAddress;
  }

  public ConcurrentHashMap<FullAddress, Date> getReplicas() {
    return replicas;
  }

  public void setReplicas(ConcurrentHashMap<FullAddress, Date> replicas) {
    this.replicas = replicas;
  }

  private Integer getCurrentTerm() {
    return currentTerm;
  }

  private void setCurrentTerm(Integer currentTerm) {
    this.currentTerm = currentTerm;
  }

  /**
   * This function is used to start this FrontEnd server.
   */
  public void start() {
    port(this.getLocalAddress().getPort());
    receiveOthersMulticastMessage();
    removeOldReplicas();
    get("/places", (req, res) -> {
      System.out.printf("%s %s\n", req.requestMethod(), req.matchedPath());
      return readPlaces(req, res);
    });
    get("/places/:codPostal", (req, res) -> {
      System.out.printf("%s %s\n", req.requestMethod(), req.matchedPath());
      return readPlace(req, res);
    });
    post("/places", (req, res) -> {
      System.out.printf("%s %s\n", req.requestMethod(), req.matchedPath());
      return createPlace(req, res);
    });
    put("/places/:codPostal", (req, res) -> {
      System.out.printf("%s %s\n", req.requestMethod(), req.matchedPath());
      return updatePlace(req, res);
    });
    delete("/places/:codPostal", (req, res) -> {
      System.out.printf("%s %s\n", req.requestMethod(), req.matchedPath());
      return deletePlace(req, res);
    });
  }

  /**
   * This function is used to clean up old replicas from the class ConcurrentHashMap that contains
   * all addresses of all PlaceManager.
   * 
   * @param maximumReplicaAge Contains the max age of the replica we should consider. Must be in
   *        milliseconds.
   * 
   */
  private void cleanUpReplicas(Integer maximumReplicaAge) {
    ConcurrentHashMap<FullAddress, Date> replicas = this.getReplicas();
    Iterator<Entry<FullAddress, Date>> it = replicas.entrySet().iterator();

    while (it.hasNext()) {
      Entry<FullAddress, Date> pair = it.next();
      // If replica is older than random timeout seconds
      if (new Date().getTime() - pair.getValue().getTime() > maximumReplicaAge) {
        it.remove();
      }
    }
  }

  /**
   * This function is used to receive the state of other PlaceManager's
   */
  private void receiveOthersMulticastMessage() {
    // Handles listening to multicast messages
    new Thread() {
      public void run() {
        // Listens to other replicas messages in Multicast
        while (true) {
          try {
            ComunicationHeartbeat message =
                ComunicationInterface.listenMulticastMessage(FrontEnd.this.getMulticastAddress());
            switch (message.getMessage()) {
              case "LEADER":
                if (message.getTerm() > FrontEnd.this.getCurrentTerm()) {
                  FrontEnd.this.getReplicas().put(message.getFullAddress(), new Date());
                  FrontEnd.this.setLeaderAddress(message.getFullAddress());
                  FrontEnd.this.setCurrentTerm(message.getTerm());
                }
                break;
              default:
                FrontEnd.this.getReplicas().put(message.getFullAddress(), new Date());
            }
          } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
          }
        }
      }
    }.start();
  }

  /**
   * This function is used to remove old replicas that potentially died
   */
  private void removeOldReplicas() {
    // Handles removing all old replicas of 2xtimeout second
    new Thread() {
      public void run() {
        while (true) {
          FrontEnd.this.cleanUpReplicas(2 * multicastTimeout);

          try {
            Thread.sleep(multicastTimeout);
          } catch (InterruptedException e) {
            System.out.println(e.getMessage());
          }
        }
      }
    }.start();
  }

  private String readPlaces(Request req, Response res) {
    res.type("application/json");
    PlacesListInterface pl = null;
    Object[] allPlaces = null;
    try {
      pl = (PlacesListInterface) Naming.lookup("rmi://" + this.getRandomNode() + "/placemanager");
      allPlaces = pl.getAllPlaces().values().toArray();
    } catch (MalformedURLException | RemoteException | NotBoundException e) {
      // Error placemanager
      res.status(503);
      return newJSONMessage("Service Unavailable");
    }
    res.status(200);
    return newJSONPlaces(allPlaces);
  }

  private String readPlace(Request req, Response res) {
    res.type("application/json");
    PlacesListInterface pl = null;
    Place place = null;
    try {
      pl = (PlacesListInterface) Naming.lookup("rmi://" + this.getRandomNode() + "/placemanager");
      place = pl.getPlace(req.params(":codPostal"));
    } catch (MalformedURLException | RemoteException | NotBoundException e) {
      // Error placemanager
      res.status(503);
      return newJSONMessage("Service Unavailable");
    }
    if (place == null) {
      // No place found
      res.status(404);
      return newJSONMessage("Not Found");
    }
    res.status(200);
    return newJSONPlace(place);
  }

  private String createPlace(Request req, Response res) {
    res.type("application/json");
    PlacesListInterface pl = null;
    Place place = new Place(req.queryParams("postalCode"), req.queryParams("locality"));
    try {
      pl = (PlacesListInterface) Naming
          .lookup("rmi://" + this.getLeaderAddress() + "/placemanager");
      pl.addPlace(place);
    } catch (MalformedURLException | RemoteException | NotBoundException e) {
      // Error placemanager
      res.status(503);
      return newJSONMessage("Service Unavailable");
    }
    // Everything okay
    res.status(201);
    return newJSONPlace(place);
  }

  private String updatePlace(Request req, Response res) {
    res.type("application/json");
    PlacesListInterface pl = null;
    Place place = new Place(req.queryParams("postalCode"), req.queryParams("locality"));
    try {
      pl = (PlacesListInterface) Naming
          .lookup("rmi://" + this.getLeaderAddress() + "/placemanager");
      pl.addPlace(place);
    } catch (MalformedURLException | RemoteException | NotBoundException e) {
      // Error placemanager
      res.status(503);
      return newJSONMessage("Service Unavailable");
    }
    // Everything okay
    res.status(200);
    return newJSONPlace(place);
  }

  private String deletePlace(Request req, Response res) {
    res.type("application/json");
    PlacesListInterface pl = null;
    Place place = null;
    try {
      pl = (PlacesListInterface) Naming
          .lookup("rmi://" + this.getLeaderAddress() + "/placemanager");
      place = pl.removePlace(req.params(":codPostal"));
    } catch (MalformedURLException | RemoteException | NotBoundException e) {
      // Error placemanager
      res.status(503);
      return newJSONMessage("Service Unavailable");
    }
    if (place == null) {
      // No place found
      res.status(404);
      return newJSONMessage("Not Found");
    }
    res.status(200);
    return newJSONPlace(place);
  }

  private FullAddress getRandomNode() {
    Object[] keys = this.getReplicas().keySet().toArray();
    FullAddress randomAddress = null;
    do {
      randomAddress = (FullAddress) keys[(int) (Math.random() * keys.length)];
      // Repeats until we find one that is not a leader
    } while (this.getLeaderAddress().equals(randomAddress) == true);
    return randomAddress;
  }

  @SuppressWarnings("unchecked")
  private String newJSONMessage(String message) {
    JSONObject obj = new JSONObject();
    obj.put("message", message);
    return obj.toJSONString();
  }

  @SuppressWarnings("unchecked")
  private String newJSONPlace(Place place) {
    JSONObject obj = new JSONObject();
    obj.put("postalCode", place.getPostalCode());
    obj.put("locality", place.getLocality());
    return obj.toJSONString();
  }

  @SuppressWarnings("unchecked")
  private String newJSONPlaces(Object[] places) {
    JSONArray arr = new JSONArray();
    for (Object place : places) {
      JSONObject obj = new JSONObject();
      obj.put("postalCode", ((Place) place).getPostalCode());
      obj.put("locality", ((Place) place).getLocality());
      arr.add(obj);
    }
    return arr.toJSONString();
  }
}
