package httpServer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final int TREAD_POOL_COUNT = 64;
    private final List<String> VALID_PATHS = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public Server(int port) throws IOException {
        this.port = port;
    }

    public void start() {
        final var threadPool = Executors.newFixedThreadPool(TREAD_POOL_COUNT);
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                var socket = serverSocket.accept();
                threadPool.execute(() -> listen(socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private void listen(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            final var path = calculatePath(in);
            if (methodIsNotFound(path, out)) {
                return;
            }
            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);
            if (methodIsClassic(path)) {
                processClassic(out, mimeType, filePath);
                return;
            }
            answerOk(out, mimeType, Files.readAllBytes(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String calculatePath(BufferedReader in) throws IOException {
        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IOException("Path is not valid");
        }
        return parts[1];
    }

    private boolean methodIsNotFound(String path, BufferedOutputStream out) throws IOException {
        if (!VALID_PATHS.contains(path)) {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
            return true;
        }
        return false;
    }

    private void answerOk(BufferedOutputStream out, String mimeType, byte[] content) throws IOException {
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.write(content);
        out.flush();
    }

    private boolean methodIsClassic(String path) {
        return path.equals("/classic.html");
    }

    private void processClassic(BufferedOutputStream out, String mimeType, Path filePath) throws IOException {
        final var template = Files.readString(filePath);
        final var content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
        ).getBytes();
        answerOk(out, mimeType, content);
    }
}