import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class Client extends JFrame {
    private static final String USERNAME_PLACEHOLDER = "Username";
    private static final String ADDRESS_PLACEHOLDER = "Address";
    private static final String PORT_PLACEHOLDER = "Port";
    private static final double MESSAGE_SCALE_WRAP = 0.60;
    public final int INPUT_PANEL_HEIGHT = 50;
    public final int CONNECTED_USER_WIDTH = 155;
    public final int DEFAULT_WINDOW_WIDTH = 600;
    public final int DEFAULT_WINDOW_HEIGHT = 750;

    private JFrame frame;
    private JTextPane chatArea;
    private JTextField inputField;
    private JTextField usernameField;
    private JTextField addressField;
    private JTextField portField;
    private JButton connectButton;
    private JButton sendButton;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private JScrollPane messagePanelScrollPane;
    private JPanel connectedUsersPanel, messagePanel;
    private ArrayList<String> connectedUsers;

    private StyledDocument doc; 
    private Map<String, Color> userColors = new HashMap<>();
    private Random random = new Random();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }

    public Client() {
        buildGUI();
        connectedUsers = new ArrayList<String>();
    }

    //Building GUI
    private void buildGUI() {

        frame = new JFrame("Chat");
        frame.setSize(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chatArea = new JTextPane();
        chatArea.setEditable(false);
        doc = chatArea.getStyledDocument();
        JScrollPane scrollPane = new JScrollPane(chatArea);

        //Set background color
        frame.getContentPane().setBackground(Utilities.PRIMARY_COLOR);

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

        //Adding custom message panel
        addConnectedUsersComponents();
        addChatComponents();

        //Update the scrollpane on scroll to ensure no glitchy graphics occur
        messagePanelScrollPane.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                revalidate();
                repaint();
            }
        });

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
                    field.setForeground(Color.BLACK);
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
        //Need to check for these special characters used as flags for connecteduserlist
        //Also needs to check for ":" as that is the username split flag
        if (username.contains(",") || username.contains("#") || username.contains(":"))
        {
            JOptionPane.showMessageDialog(frame, "Invalid special character in username.");
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
                        appendMessage(msg + "\n");
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                        revalidate();
                        repaint();
                    }
                } catch (IOException e) {
                    appendMessage("Disconnected from server.\n");
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

    private void sendMessage(String msg) {
        if (!msg.isEmpty()) {
            out.println(msg);
        }
        inputField.setText("");
    }

    // Append message with only username colored
    private void appendMessage(String msg) {
        try {
            int colonIndex = msg.indexOf(":");
            int poundIndex = msg.indexOf("#");
            if (colonIndex != -1) {
                String username = msg.substring(0, colonIndex);
                String text = msg.substring(colonIndex); // includes ": message"
                // Assign a unique color to the username
                Color color = userColors.computeIfAbsent(username,
                        k -> new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));

                // Style username
                SimpleAttributeSet usernameStyle = new SimpleAttributeSet();
                StyleConstants.setForeground(usernameStyle, color);
                doc.insertString(doc.getLength(), username, usernameStyle);

                // Style message text (black)
                SimpleAttributeSet messageStyle = new SimpleAttributeSet();
                StyleConstants.setForeground(messageStyle, Color.GRAY);
                doc.insertString(doc.getLength(), text , messageStyle);

                //Create message component
                messagePanel.add(createChatMessageComponent(text, username, color, false));
                //Refresh canvases
                frame.revalidate();
                frame.repaint();
                messagePanelScrollPane.getVerticalScrollBar().setValue(Integer.MAX_VALUE);

            } else if (poundIndex != -1){
                //Userlist Broadcast
                String connectedUsersString = msg.substring(1);
                String[] stringArray = connectedUsersString.split(",");
                connectedUsers.clear();
                connectedUsers = new ArrayList<>(Arrays.asList(stringArray));
                updateActiveUsers(connectedUsers);
                frame.revalidate();
                frame.repaint();
            }
            else {
                // System message
                appendSystemMessage(msg);
                messagePanel.add(createChatMessageComponent(msg, "System", Utilities.ACCENT_COLOR_PRIMARY, true));
                frame.revalidate();
                frame.repaint();
            }
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void appendSystemMessage(String msg) {
        try {
            SimpleAttributeSet sysStyle = new SimpleAttributeSet();
            StyleConstants.setForeground(sysStyle, Color.GRAY);
            doc.insertString(doc.getLength(), msg + "\n", sysStyle);
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }



    private void addChatComponents()
    {
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setBackground(Utilities.TRANSPARENT_COLOR);

        messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(Utilities.TRANSPARENT_COLOR);

        messagePanelScrollPane = new JScrollPane(messagePanel);
        messagePanelScrollPane.setBackground(Utilities.TRANSPARENT_COLOR);
        messagePanelScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messagePanelScrollPane.getVerticalScrollBar().setUnitIncrement(15);
        messagePanelScrollPane.getViewport().setBackground(Utilities.TRANSPARENT_COLOR);

        //Sets a listener for scrolling updates
        //This is to repaint and prevent visual bugs each scroll tick
        messagePanelScrollPane.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                frame.revalidate();
                frame.repaint();
            }
        });

        //chatPanel.add(messagePanel, BorderLayout.CENTER);
        chatPanel.add(messagePanelScrollPane, BorderLayout.CENTER);

        //Debug welcome message
        //messagePanel.add(createChatMessageComponent(new Message("System", "Welcome to the chat room:")));

        JPanel inputPanel = new JPanel();
        inputPanel.setBorder(Utilities.addPadding(10,10,10,10));
        inputPanel.setLayout(new BorderLayout());
        inputPanel.setBackground(Utilities.TRANSPARENT_COLOR);

        JTextField inputField = new JTextField();

        //Checks for user hitting "enter"
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    sendAndClear(inputField);
                }
            }
        });

        //Setting the format for the input field box
        inputField.setBackground(Utilities.SECONDARY_COLOR);
        inputField.setForeground(Utilities.TEXT_COLOR);
        inputField.setBorder(Utilities.addPadding(0,10,0,10));
        inputField.setFont(Utilities.MESSAGE_FONT);
        inputField.setPreferredSize(new Dimension(inputPanel.getWidth(), INPUT_PANEL_HEIGHT));

        inputPanel.add(inputField,BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        //Checks for user hitting the send button
        sendButton.addActionListener(e -> sendAndClear(inputField));

        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        frame.add(chatPanel, BorderLayout.CENTER);
    }

    private JPanel createChatMessageComponent(String message, String user)
    {
        return createChatMessageComponent(message,user,Utilities.TEXT_COLOR, false);
    }


    private JPanel createChatMessageComponent(String message, String user, Color usernameColor, boolean isSystemMessage){

        //Creating a component for the individual message, stacking with each other component
        JPanel chatMessage = new JPanel();
        chatMessage.setBackground(Utilities.TRANSPARENT_COLOR);
        chatMessage.setLayout(new BoxLayout(chatMessage,BoxLayout.Y_AXIS));
        chatMessage.setBorder(Utilities.addPadding(20,20,10,20));

        //Include a username label if this is NOT a system message
        if (!isSystemMessage) {
            JLabel usernameLabel = new JLabel(user);
            usernameLabel.setFont(Utilities.USERNAME_FONT);
            usernameLabel.setForeground(usernameColor);
            chatMessage.add(usernameLabel);
        }

        //This will be the actual message content, different based on if it is a chat or system message
        JLabel messageLabel = new JLabel();

        //Setting the text to a wrapped version of itself using HTML
        //Removing the ': ' from messages
        String normalizedMessage = message.substring(message.indexOf(":") + 1);
        if (!normalizedMessage.isEmpty())
        {
            messageLabel.setText(wrapText(normalizedMessage, MESSAGE_SCALE_WRAP));
        }
        else {
            messageLabel.setText(wrapText(message, MESSAGE_SCALE_WRAP));
        }

        if (isSystemMessage)
        {
            messageLabel.setFont(Utilities.CONSOLE_FONT);
            messageLabel.setForeground(Utilities.TERTIARY_COLOR);
        }
        else
        {
            messageLabel.setFont(Utilities.MESSAGE_FONT);
            messageLabel.setForeground(Utilities.TEXT_COLOR);
        }

        //Adding this message label to the frame as one object package
        chatMessage.add(messageLabel);

        //Return the entire created object as a frame, likely to be added to a messagepane
        return chatMessage;
    }

    private void addConnectedUsersComponents(){
        connectedUsersPanel = new JPanel();
        connectedUsersPanel.setBorder(Utilities.addPadding(10,10,10,10));
        connectedUsersPanel.setLayout(new BoxLayout(connectedUsersPanel, BoxLayout.Y_AXIS));
        connectedUsersPanel.setBackground(Utilities.SECONDARY_COLOR);
        connectedUsersPanel.setPreferredSize(new Dimension(CONNECTED_USER_WIDTH, getHeight()));

        JLabel connectedUsersLabel = new JLabel("Connected Users");
        connectedUsersLabel.setFont(Utilities.CONNECTED_USERS_FONT);
        connectedUsersLabel.setForeground(Utilities.TERTIARY_COLOR);
        connectedUsersPanel.add(connectedUsersLabel);

        frame.add(connectedUsersPanel, BorderLayout.WEST);
    }

    public void updateActiveUsers(ArrayList<String> users) {
        // Remove the current user list panel (which should be the second component in the panel)
        //The user list panel doesnt get added until after and this is mainly for when the users get updated
        if (connectedUsersPanel.getComponents().length >= 2){
            connectedUsersPanel.remove(1);
        }

        JPanel userListPanel = new JPanel();
        userListPanel.setBackground(Utilities.TRANSPARENT_COLOR);
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));

        for (String user : users){
            JLabel username = new JLabel();
            username.setText(user);
            username.setForeground(Utilities.ACCENT_COLOR_SECONDARY);
            username.setFont(Utilities.CONNECTED_USERS_LIST_FONT);
            userListPanel.add(username);
        }

        connectedUsersPanel.add(userListPanel);
        revalidate();
        repaint();
    }

    //TODO currently doesnt work properly
    //Wrap text using the message panel's width
    private String wrapText(String message, double scaleWrap){
        String wrappedMessage =
                "<html>" +
                        "<body style='width:" + (scaleWrap * frame.getWidth()) +"'px>" +
                            message +
                        "</body>"+
                "</html>";
        return wrappedMessage;
    }

    public void sendAndClear(JTextField inputField)
    {
        String input = inputField.getText();

        //Dont send message if the message is empty, will return early
        if (input.isEmpty()) return;

        //Clear text after sending message with enter
        inputField.setText("");

        //Create new message object and send it to myStompClient
        sendMessage(input);
    }

}
