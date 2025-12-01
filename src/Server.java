import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

// Server class
class Server {

    private static final int PORT = 1234;
    // A set to hold all client output streams
    private static Set<PrintWriter> clientWriters = new HashSet<>(); 
    // A map to associate output streams with client names
    private static Map<PrintWriter, String> clientNames = new HashMap<>();

    private static LogWriter log;
    public static void main(String[] args)
    {
        log = new LogWriter();
        String msg;
        //Try catch for server socket (Automatically closes server socket)
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            msg = "Server started. Waiting for clients to connect...";
            System.out.println(msg);
            log.writeLog(msg, "SYS");
            
            //Loop for accepting client connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                msg = "New client connected: " + clientSocket.getInetAddress().getHostAddress();
                System.out.println(msg);
                log.writeLog(msg, "SYS");

                //Handle each client in a new thread
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ClientHandler class
    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        //For sending messages to client
        private PrintWriter out;
        //For reading messages from client
        private BufferedReader in;
        //Used to store name of client
        private String name;

        private LogWriter log;

        //Constructor
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            log = new LogWriter();
            String msg;

            try {
                // get the outputstream of client
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                // get the inputstream of client
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                
                //Read the username sent by client
                name = in.readLine();
                //Store user name 
                clientNames.put(out, name); 
                synchronized (clientWriters) {
                    clientWriters.add(out); 
                }

                //Notify all clients that a new user has joined
                msg = name + " has joined the chat.";
                broadcastMessage(msg);
                log.writeLog(msg, "SYS");

                while ((msg = in.readLine()) != null) {
                    if ("exit".equalsIgnoreCase(msg)) {
                        break;
                    }

                    //Broadcast the received message to all users
                    broadcastMessage(name + ": " + msg);
                    log.writeLog(msg, "CHAT");
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //Process for when a user disconnects
                try {
                    if (out != null) {
                        synchronized (clientWriters) {
                            //Remove user from the list
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

        //Broadcast message to all connected users
        private void broadcastMessage(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }

    private static class LogWriter {
        private String fileName;

        public LogWriter()
        {
            this.fileName = generateFileName();
        }

        public void writeLog(String msg, String logType) throws IOException
        {
            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            File logFile = new File(logDir, fileName);

            try(FileWriter writer = new FileWriter(logFile, true)) {
                writer.write("[" + logType + "] " + msg + "\n");
            }
        }

        private String generateFileName() {
            LocalDate currentDate = LocalDate.now();
            LocalTime currentTime = LocalTime.now();
            fileName = "" + currentDate + "-" + currentTime;

            // Shorten length
            int lastColon = fileName.lastIndexOf(':');
            fileName = fileName.substring(0, lastColon);
            fileName = fileName.replace(':', '-');

            fileName = fileName + ".log";

            return fileName;
        }
    }
}
