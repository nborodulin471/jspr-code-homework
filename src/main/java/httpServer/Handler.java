package httpServer;

import httpServer.model.Request;

import java.io.BufferedOutputStream;

@FunctionalInterface
public interface Handler {
    public void handle(Request request, BufferedOutputStream responseStream);
}
