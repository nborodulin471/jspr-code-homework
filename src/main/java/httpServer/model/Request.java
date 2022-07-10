package httpServer.model;


import lombok.Builder;
import lombok.Getter;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
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
    private final List<NameValuePair> queryParams;

    private Request(String url, Optional<String> body, String method, HashMap<String, String> headers, List<NameValuePair> queryParams) {
        this.path = url;
        this.body = body;
        this.method = method;
        this.headers = headers;
        this.queryParams = queryParams;
    }

    public static List<NameValuePair> parseQueryParams(String query) {
        try {
            return URLEncodedUtils.parse(new URI(query), Charset.defaultCharset());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Optional<String> extractHeader(String header) {
        return Optional.ofNullable(headers.get(header));
    }

    public NameValuePair getQueryParam(String name) {
        return queryParams.stream()
                .filter(pair -> pair.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
