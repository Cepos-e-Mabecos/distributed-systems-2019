package comunication;

import java.io.IOException;

public interface UDPInterface {
  void sendMessage(String address, Integer port, String message) throws IOException;
  void listenNewHosts(String address, Integer port) throws IOException;
}
