import com.sun.net.httpserver.HttpServer;
import Handler.RestaurantHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Register handler at /hello
        server.createContext("/restaurants", new RestaurantHandler());

        server.setExecutor(null); // uses default executor
        server.start();
        System.out.println("Server is running at http://localhost:" + port + "/hello");
    }
}

