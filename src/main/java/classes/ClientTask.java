package classes;

import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

public class ClientTask implements Runnable{

    private Socket client;
    public static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public ClientTask(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try (final var in = new BufferedInputStream(client.getInputStream());
             final var out = new BufferedOutputStream(client.getOutputStream());) {

            while(true) {
                Request request = createRequest(in, out);

                Handler handler = Server.getHandlers().get(request.getMethod()).get(request.getPath());

                if (handler == null) {
                    Path parent = Path.of(request.getPath()).getParent();
                    handler = Server.getHandlers().get(request.getMethod()).get(parent.toString());
                    if (handler == null) {
                        return;
                    }
                }


                handler.handle(request, out);
                getResponse(request, out);
            }

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Request createRequest(BufferedInputStream in, BufferedOutputStream out) throws IOException, URISyntaxException {
        //лимит на request line + заголовки
        final int limit = 4096;
        in.mark(limit);
        final byte[] buffer = new byte[limit];
        final int read = in.read(buffer);

        final byte[] requestLineDelimiter = new byte[]{'\r', '\n'};
        final int requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            client.close();
        }
        // read only request line for simplicity
        // must be in form GET /path HTTP/1.1
        final String[] requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");

        // читаем request line
        if (requestLine.length != 3) {
            client.close();
        }

        final String method = requestLine[0];

        final String path = requestLine[1].split("\\?")[0];
        if (!requestLine[1].startsWith("/")) {
            client.close();
        }

        //ищем заголовки
        final byte[] headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final int headersStart = requestLineEnd + requestLineDelimiter.length;
        final int headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            client.close();
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        final byte[] headersBytes = in.readNBytes(headersEnd - headersStart);
        final List<String> headers = Arrays.asList(new String(headersBytes).split("\r\n"));


        //для GET тела нет
        String body = null;
        if (!method.equals("GET")) {
            in.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final Optional<String> contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final int length = Integer.parseInt(contentLength.get());
                final byte[] bodyBytes = in.readNBytes(length);
                body = new String(bodyBytes);
            }
            // вычитываем Content-Type, чтобы понять есть ли в body параметры
            final Optional<String> contentType = extractHeader(headers, "Content-Length");
            if (contentType.isPresent()) {
                final String type = contentType.get();
                if (type.equals("application/x-www-form-urlencoded")) {
                    // request.setPostParams(URLEncodedUtils.parse(request.getBody(), StandardCharsets.UTF_8));
                }
            }
        }

        Request request = new Request(method, path, headers, body);
        final URI uri = new URI(path);

        request.setQueryParams(URLEncodedUtils.parse(uri, StandardCharsets.UTF_8));

        System.out.println(request);
        System.out.println(request.getQueryParam("value"));
        out.flush();

        return request;
    }

    // from Google guava with modifications
    private int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    public static void getResponse(Request request, BufferedOutputStream responseStream) throws IOException {

        final var filePath = Path.of(".", "public", request.getPath());
        final var mimeType = Files.probeContentType(filePath);

        // special case for classic

        final var template = Files.readString(filePath);
        final var content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
        ).getBytes();
        responseStream.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        responseStream.write(content);

        final var length = Files.size(filePath);
        responseStream.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, responseStream);
        responseStream.flush();
    }
}
