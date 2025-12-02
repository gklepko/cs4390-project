import java.io.*;
import java.net.*;
import java.util.*;

// Server class
public class Server {
    private static final int PORT = 1234;
    // A set to hold all client output streams
    static Set<PrintWriter> clientWriters = new HashSet<>();
    // A map to associate output streams with client names
    static Map<PrintWriter, String> clientNames = new HashMap<>();

    private static LogWriter log;

    public static void main(String[] args) {
        log = new LogWriter();
        String msg;

        // Try-with-resources for server socket (automatically closes server socket)
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            msg = "Server started. Waiting for clients to connect...";
            System.out.println(msg);
            log.writeLog(msg, "SYS");

            // Loop for accepting client connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                msg = "New client connected: " + clientSocket.getInetAddress().getHostAddress();
                System.out.println(msg);
                log.writeLog(msg, "SYS");

                // Handle each client in a new thread
                new ClientHandler(clientSocket, clientWriters, clientNames).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
