package httpServer;

import httpServer.controller.Controller;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(9999);
        new Controller(server).init();
        server.start();
    }
}


