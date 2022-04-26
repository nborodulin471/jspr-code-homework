package httpServer.model;


import lombok.Builder;
import lombok.Getter;
import org.apache.http.NameValuePair;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
public class Request {
    private final String path;
    private final Optional<String> body;
    private final String method;
    private final HashMap<String, String> headers;
    private final List<NameValuePair> parameters;

    private Request(String url, Optional<String> body, String method, HashMap<String, String> headers, List<NameValuePair> parameters) {
        this.path = url;
        this.body = body;
        this.method = method;
        this.headers = headers;
        this.parameters = parameters;
    }

    private Optional<String> extractHeader(String header) {
        return Optional.ofNullable(headers.get(header));
    }

    public NameValuePair getPostParam(String name) {
        return parameters.stream()
                .filter(pair -> pair.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public List<NameValuePair> getPostParams() {
        return parameters;
    }

}
