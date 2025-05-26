import com.sun.net.httpserver.HttpServer;
import Handler.AuthHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        //server.createContext("/restaurants", new RestaurantHandler());

        try {
            server.createContext("/auth", new AuthHandler());
        } catch ( Exception e) {
            System.err.println("Error creating AuthHandler: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        server.setExecutor(null);
        server.start();
        System.out.println("Server is running on port " + port);
        System.out.println("Auth endpoints available at http://localhost:" + port + "/auth/register and http://localhost:" + port + "/auth/login");
    }
}