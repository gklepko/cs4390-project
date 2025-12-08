import java.io.*;
import java.net.*;
import java.util.*;

// ClientHandler class
public class ClientHandler extends Thread {
    private final Socket clientSocket;
    // Shared collections from the server
    private final Set<PrintWriter> clientWriters;
    private final Map<PrintWriter, String> clientNames;

    // For sending messages to client
    private PrintWriter out;
    // For reading messages from client
    private BufferedReader in;
    // Used to store name of client
    private String name;

    private LogWriter log;

    // Constructor
    public ClientHandler(Socket socket, Set<PrintWriter> clientWriters, Map<PrintWriter, String> clientNames) {
        this.clientSocket = socket;
        this.clientWriters = clientWriters;
        this.clientNames = clientNames;
    }

    @Override
    public void run() {
        log = new LogWriter();
        String msg;

        try {
            // Get the output stream of client
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            // Get the input stream of client
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Read the username sent by client
            name = in.readLine();
            // Store user name
            clientNames.put(out, name);
            synchronized (clientWriters) {
                clientWriters.add(out);
            }

            // Notify all clients that a new user has joined
            msg = name + " has joined the chat.";
            broadcastMessage(msg);
            log.writeLog(msg, "SYS");

            while ((msg = in.readLine()) != null) {
                if ("exit".equalsIgnoreCase(msg)) {
                    break;
                }

                // Broadcast the received message to all users
                broadcastMessage(name + ": " + msg);
                log.writeLog(msg, "CHAT");
            }

        } catch (IOException e) {
            e.printStackTrace(); //test comment
        } finally {
            // Process for when a user disconnects
            try {
                if (out != null) {
                    synchronized (clientWriters) {
                        // Remove user from the list
                        clientWriters.remove(out);
                    }
                }
                if (name != null) {
                    msg = name + " has left the chat.";
                    broadcastMessage(msg);
                    log.writeLog(msg, "SYS");
                }
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Broadcast message to all connected users
    private void broadcastMessage(String message) {
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
    }
}
