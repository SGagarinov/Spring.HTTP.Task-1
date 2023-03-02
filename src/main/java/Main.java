import classes.Handler;
import classes.Request;
import classes.Server;
import classes.ThreadPool;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server(9999);

        server.addHandler("GET", "/index.html", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                try {
                    ThreadPool.getResponse(request, responseStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        server.addHandler("GET", "/forms.html", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                try {
                    ThreadPool.getResponse(request, responseStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        server.run();
    }
}


