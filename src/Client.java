import java.io.*;
import java.net.*;
import java.util.*;

// Client class
public class Client {

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 1234)) {

            // One scanner for all user input
            Scanner sc = new Scanner(System.in);

            System.out.print("What is your username? ");
            String name = sc.nextLine();  // use nextLine so spaces are allowed

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            String line = null;
            
            out.println(name);
            
            while (!"exit".equalsIgnoreCase(line)) {

                // Read message from user
                line = sc.nextLine();
                
                if(line.equalsIgnoreCase("")) {
                	System.out.println("You cannot send an empty message");
                	continue;
                }

                // Send name + message in one line
                out.println(line);

                // Read reply from server
                String reply = in.readLine();
                if (reply == null) {  // server closed connection
                    System.out.println("Server closed connection.");
                    break;
                }

                System.out.println("Server replied: " + reply);
            }

            // do NOT sc.close() here when it's wrapping System.in
            sc.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
    }
}
