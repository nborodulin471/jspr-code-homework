package httpServer;

import httpServer.model.Request;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final int TREAD_POOL_COUNT = 64;
    private final ConcurrentHashMap<Map<String, String>, Handler> handlers = new ConcurrentHashMap<>();

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        final var threadPool = Executors.newFixedThreadPool(TREAD_POOL_COUNT);
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.submit(() -> listen(socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void answerOk(BufferedOutputStream out, String mimeType, byte[] content) throws IOException {
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

    private void notFound(BufferedOutputStream out) {
        try {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listen(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            final var request = parseRequest(in);
            Handler handler = findHandler(request.getMethod(), request.getPath());
            if (handler == null) {
                notFound(out);
                return;
            }
            handler.handle(request, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Request parseRequest(BufferedReader in) throws IOException {
        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IOException("Путь не валиден");
        }
        String method = parts[0];
        String path = parts[1];
        String absolutePath = null;
        if (path.indexOf("?") != -1) {
            absolutePath = path.substring(0, path.indexOf("?"));
        } else {
            absolutePath = path;
        }

        String line = in.readLine();
        HashMap<String, String> headers = new HashMap<>();
        while (line.length() > 0) {
            int idx = line.indexOf(":");
            if (idx == -1) {
                throw new IOException("Не корректный параметр : " + line);
            }
            headers.put(line.substring(0, idx), line.substring(idx + 1, line.length()));
            line = in.readLine();
        }

        return Request.builder()
                .method(method)
                .path(absolutePath)
                .headers(headers)
                .queryParams(Request.parseQueryParams(parts[1]))
                .build();
    }

    public Handler findHandler(String method, String path) {
        return handlers.get(Map.of(method, path));
    }

    public void addHandler(String method, String path, Handler handler) {
        if (findHandler(method, path) == null) {
            handlers.put(Map.of(method, path), handler);
        }
    }
}