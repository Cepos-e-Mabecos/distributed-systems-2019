package comunication;

import java.io.IOException;

public interface ComunicationInterface {
  void sendMessage(String address, Integer port, String message) throws IOException;
  String listenMulticastMessage(String address, Integer port) throws IOException;
  String listenUnicastMessage(Integer port) throws IOException;
}
