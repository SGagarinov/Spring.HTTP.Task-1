package classes;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);
    private int port;

    public Server(int port) throws IOException {
        this.port = port;
    }

    public void run() {
        System.out.println("Server start");

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            while (!serverSocket.isClosed()) {
                Socket client = serverSocket.accept();
                System.out.println("New Client");
                newClient(client);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void newClient(Socket client) {
        threadPool.execute(new ThreadPool(client));
    }
}
