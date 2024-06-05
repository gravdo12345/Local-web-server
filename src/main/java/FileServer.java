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
import java.util.Scanner;

public class FileServer {
    private static String baseDir = "E:\\web_server_test"; // точка старту
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new FileHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Server started on port " + PORT);

        // поток, для терміналу
        new Thread(new CommandHandler()).start();
    }

    static class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            String fileName = requestPath.substring(1); // знищуємо лондон в "/"
            File file = new File(baseDir, fileName);

            while (!file.exists()) {
                try {
                    Thread.sleep(1000); // секундочку
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Server interrupted", e);
                }
                file = new File(baseDir, fileName);
            }

            byte[] fileBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            exchange.sendResponseHeaders(200, fileBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();
        }
    }

    static class CommandHandler implements Runnable {
        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Enter new directory: ");
                String newDir = scanner.nextLine();
                if (new File(newDir).isDirectory()) {
                    baseDir = newDir;
                    System.out.println("Directory changed to: " + baseDir);
                } else {
                    System.out.println("Invalid directory. Please try again.");
                }
            }
        }
    }
}
