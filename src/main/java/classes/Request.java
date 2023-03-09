package classes;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.http.NameValuePair;

public class Request {
    private final String method;
    private final String path;
    private List<String> headers;
    private final String body;
    private List<NameValuePair> queryParams;

    public Request(String method, String path, List<String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public void setQueryParams(List<NameValuePair> params) {
        this.queryParams = params;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String param) {
        return queryParams.stream()
                .filter(p -> p.getName().equals(param))
                .map(p -> p.getValue())
                .collect(Collectors.joining(", "));
    }
}
