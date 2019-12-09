import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.delete;
import spark.Request;
import spark.Response;
import spark.Route;

public class RESTServer {
  public static void main(String[] args) {
    get("/places", (req, res) -> {
      return req.uri();
    });
    
    get("/places/:codPostal", (req, res) -> {
      return req.uri();
    });
    
    post("/places", (req, res) -> {
      return req.uri();
    });
    
    put("/places/:codPostal", (req, res) -> {
      return req.uri();
    });
    
    delete("/places/:codPostal", (req, res) -> {
      return req.uri();
    });
  }
}
