import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

    public static void main(String[] args) {
        //Try catch for socket
        try (Socket socket = new Socket("localhost", 1234)) {

            //Sscanner for user input
            Scanner sc = new Scanner(System.in);

            //Asking user for username
            System.out.print("What is your username? ");
            String name = sc.nextLine();  

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //Send username to server
            out.println(name);

            //Start a thread to listen for server messages
            Thread listenerThread = new Thread(new Listener(in));
            listenerThread.start();

            //Main loop for sending messages to server
            String line = null;
            while (!"exit".equalsIgnoreCase(line)) {
                line = sc.nextLine();
                //Warn user if they try to send an empty message
                if (line.equalsIgnoreCase("")) {
                    System.out.println("You cannot send an empty message");
                    continue;
                }

                //Send message to server
                out.println(line);
            }

            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Listener class to handle server messages
    private static class Listener implements Runnable {
        private BufferedReader in;

        public Listener(BufferedReader in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    //Print server messages 
                    System.out.println(message);  
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
