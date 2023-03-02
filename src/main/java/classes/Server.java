package classes;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);
    private int port;

    private ServerSocket serverSocket;
    private final static Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server(int port) throws IOException {
        this.port = port;
    }

    public void run() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server start");

        try {
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

    public void addHandler(String type, String path, Handler handler) {
        if (handlers.containsKey(type)) {
            handlers.get(type).put(path, handler);
        } else {
            handlers.put(type, new ConcurrentHashMap<>(Map.of(path, handler)));
        }
    }
    public static Map<String, Map<String, Handler>> getHandlers() {
        return handlers;
    }
}
