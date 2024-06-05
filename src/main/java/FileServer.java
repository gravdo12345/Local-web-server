import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class FileServer {
    private static final String BASE_DIR = "E:\\web_server_test"; // тут будь-ласка вкажи свуй шлях до папочки(тата)
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new FileHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Server started on port " + PORT);
    }

    static class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            String fileName = requestPath.substring(1); // знищуємо "/"
            File file = new File(BASE_DIR, fileName);

            while (!file.exists()) {
                try {
                    Thread.sleep(1000); // чекаємо одну секунду прямо як Діо.
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Server interrupted", e);
                }
                file = new File(BASE_DIR, fileName);
            }

            byte[] fileBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            exchange.sendResponseHeaders(200, fileBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();
        }
    }
}
