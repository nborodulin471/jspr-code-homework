package httpServer.controller;

import httpServer.Handler;
import httpServer.Server;
import httpServer.dao.Dao;
import httpServer.model.Request;
import lombok.RequiredArgsConstructor;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class Controller {
    public final Server server;

    public void init(){
        //<editor-fold desc="Работа с сообщениями">
        server.addHandler("GET", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                // TODO: handlers code
            }
        });
        server.addHandler("POST", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                // TODO: handlers code
            }
        });
        //</editor-fold>

        //<editor-fold desc="Получение файлов">
        server.addHandler("GET", "/index.html", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                Dao.getFile(server, request.getPath(), responseStream);
            }
        });

        server.addHandler("GET", "/spring.svg", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                Dao.getFile(server, request.getPath(), responseStream);
            }
        });

        server.addHandler("GET", "/spring.png", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                Dao.getFile(server, request.getPath(), responseStream);
            }
        });

        server.addHandler("GET", "/resources.html", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                Dao.getFile(server, request.getPath(), responseStream);
            }
        });

        server.addHandler("GET", "/styles.css", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                Dao.getFile(server, request.getPath(), responseStream);
            }
        });
        server.addHandler("GET", "/app.js", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                Dao.getFile(server, request.getPath(), responseStream);
            }
        });
        server.addHandler("GET", "/styles.css", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                Dao.getFile(server, request.getPath(), responseStream);
            }
        });
        server.addHandler("GET", "/links.html", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                Dao.getFile(server, request.getPath(), responseStream);
            }
        });
        server.addHandler("GET", "/forms.html", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                Dao.getFile(server, request.getPath(), responseStream);
            }
        });
        server.addHandler("GET", "/classic.html", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                try {
                    final var filePath = Path.of(".", "public", request.getPath());
                    final var template = Files.readString(filePath);
                    final var mimeType = Files.probeContentType(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    server.answerOk(responseStream, mimeType, content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        server.addHandler("GET", "/events.html", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                Dao.getFile(server, request.getPath(), responseStream);
            }
        });
        server.addHandler("GET", "/events.js", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                Dao.getFile(server, request.getPath(), responseStream);
            }
        });
        //</editor-fold>
    }
}
