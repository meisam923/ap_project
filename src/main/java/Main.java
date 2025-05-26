import com.sun.net.httpserver.HttpServer;
import Handler.AuthHandler;
import util.JpaUtil;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {

        try {
            System.out.println("Main: Triggering JpaUtil static initializer...");
            Class.forName("util.JpaUtil");
            System.out.println("Main: JpaUtil static initializer likely completed.");
            if (JpaUtil.getEntityManagerFactory() == null || !JpaUtil.getEntityManagerFactory().isOpen()) {
                System.err.println("Main: EntityManagerFactory is not available after JpaUtil initialization. Exiting.");
                return;
            }
        } catch (Throwable e) {
            System.err.println("Main: CRITICAL ERROR during JpaUtil/EMF initialization. Server cannot start.");
            e.printStackTrace();
            return;
        }

        // Add a shutdown hook to close EMF when JVM terminates
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Main: Shutdown hook triggered.");
            JpaUtil.closeEntityManagerFactory();
        }));

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        try {
            server.createContext("/auth", new AuthHandler());
        } catch ( Exception e) {
            System.err.println("Main: Error creating AuthHandler: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        server.setExecutor(null);
        server.start();
        System.out.println("Server is running on port " + port);
        System.out.println("Auth endpoints available at http://localhost:" + port + "/auth/register and http://localhost:" + port + "/auth/login");
    }
}