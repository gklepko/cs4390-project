import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class Client {
    private static final String USERNAME_PLACEHOLDER = "Username";
    private static final String ADDRESS_PLACEHOLDER = "Address";
    private static final String PORT_PLACEHOLDER = "Port";

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
        usernameField = new JTextField();
        addressField = new JTextField();
        portField = new JTextField();

        // Add placeholder behavior
        addPlaceholder(usernameField, USERNAME_PLACEHOLDER);
        addPlaceholder(addressField, ADDRESS_PLACEHOLDER);
        addPlaceholder(portField, PORT_PLACEHOLDER);

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

    // Adds placeholder behavior into a JTextField
    private void addPlaceholder(JTextField field, String placeholder) {
        field.setForeground(Color.GRAY);
        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if(field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
    }

    // Returns "" if the field still contains the placeholder, otherwise trimmed text
    private String getTextOrEmpty(JTextField field, String placeholder) {
        String text = field.getText().trim();
        return text.equals(placeholder) ? "" : text;
    }

    // For connecting client to server
    private void connectToServer() {
        String username = getTextOrEmpty(usernameField, USERNAME_PLACEHOLDER);
        String address = getTextOrEmpty(addressField, ADDRESS_PLACEHOLDER);
        String port = getTextOrEmpty(portField, PORT_PLACEHOLDER);

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
