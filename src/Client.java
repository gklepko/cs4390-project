import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class Client {

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JTextField usernameField;
    private JTextField addressField;
    private JTextField portField;
    private JButton connectButton;
    private JButton sendButton;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }

    public Client() {
        buildGUI();
    }

    //Building GUI
    private void buildGUI() {
        frame = new JFrame("Chat");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(chatArea);

        //Top panel for username, ip, port
        JPanel topPanel = new JPanel(new GridLayout(1,4));
        usernameField = new JTextField("Username");
        addressField = new JTextField("Address");
        portField = new JTextField("Port");
        connectButton = new JButton("Connect");
        topPanel.add(usernameField);
        topPanel.add(addressField);
        topPanel.add(portField);
        topPanel.add(connectButton);

        //Bottom panel for messages
        JPanel bottomPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.setEnabled(false);

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        //Layout
        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        //Action listeners
        connectButton.addActionListener(e -> connectToServer());
        inputField.addActionListener(e -> sendMessage());
        sendButton.addActionListener(e -> sendMessage());

        frame.setVisible(true);
    }

    //For connecting client to server
    private void connectToServer() {
        String username = usernameField.getText().trim();
        String address = addressField.getText().trim();
        String port = portField.getText().trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Enter a username.");
            return;
        }
        if (address.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Enter a valid IP address.");
        }
        if(port.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Enter a valid port.");
        }

        try {
            int portNum = Integer.parseInt(port);
            socket = new Socket(address, portNum);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(username);

            usernameField.setEditable(false);
            connectButton.setEnabled(false);
            sendButton.setEnabled(true);

            //Start listener thread
            Thread listener = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        chatArea.append(msg + "\n");
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    }
                } catch (IOException e) {
                    chatArea.append("Disconnected from server.\n");
                }
            });

            listener.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Cannot connect to server.");
        }
    }

    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            out.println(msg);
        }
        inputField.setText("");
    }
}
