package httpServer;

import java.io.IOException;

public class Main {
  public static void main(String[] args) {
    try {
      new Server(9999).start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}


