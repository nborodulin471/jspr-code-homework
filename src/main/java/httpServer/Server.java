package httpServer;

import httpServer.model.Request;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.charset.Charset;
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
                threadPool.execute(() -> listen(serverSocket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
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

    public boolean methodIsNotFound(Request request, BufferedOutputStream out) throws IOException {
        if (findHandler(request.getMethod(), request.getPath()) == null) {
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

    private void listen(ServerSocket serverSocket) {
        try (
                final var socket = serverSocket.accept();
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {

            final var request = parseRequest(in);
            if (methodIsNotFound(request, out)) {
                return;
            }
            findHandler(request.getMethod(), request.getPath()).handle(request, out);
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
        }else {
            absolutePath = path;
        }

        String header = in.readLine();
        HashMap<String, String> headers = new HashMap<>();
        while (header.length() > 0) {
            int idx = header.indexOf(":");
            if (idx == -1) {
                throw new IOException("Не корректный параметр : " + header);
            }
            headers.put(header.substring(0, idx), header.substring(idx + 1, header.length()));
            header = in.readLine();
        }

        Optional<String> body = Optional.empty();
        if (!method.equals("GET")) {
            String bodyLine = in.readLine();
            StringBuilder stringBuilder = new StringBuilder();
            while (bodyLine != null) {
                stringBuilder.append(bodyLine).append("\r\n");
                bodyLine = in.readLine();
                body = stringBuilder.length() > 0 ? Optional.of(stringBuilder.toString()) : Optional.empty();
            }
        }

        return Request.builder()
                .method(method)
                .path(absolutePath)
                .headers(headers)
                .parameters(URLEncodedUtils.parse(requestLine, Charset.defaultCharset()))
                .body(body)
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