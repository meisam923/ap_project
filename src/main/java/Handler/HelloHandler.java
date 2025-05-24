package Handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class HelloHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException, IOException {
        Map<String, Object> res = new HashMap<>();
        int code;
        if (exchange.getRequestMethod().equals("GET") &&  exchange.getRequestURI().getPath().equals("/hello")) {
            res.put("status", "success");
            res.put("message", "hello world");
            code = 200;
        }
        else {
            res.put("status", "error");
            res.put("message", "Missing name");
            code=404;

        }
        String json = new ObjectMapper().writeValueAsString(res);
        exchange.sendResponseHeaders(code, json.length());
        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }
}
