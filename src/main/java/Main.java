import classes.Handler;
import classes.Request;
import classes.Server;
import classes.ClientTask;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server(9999);

        for (String validPath : ClientTask.validPaths) {

            server.addHandler("GET", validPath, (request, responseStream) -> {
                try {
                    ClientTask.getResponse(request, responseStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        }

        server.addHandler("POST", "/resources.html", (request, responseStream) -> {
            try {
                ClientTask.getResponse(request, responseStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.run();
    }
}


