import java.io.*;
import java.net.*;
import java.util.*;

/**
 * UserThread
 */
public class UserThread extends Thread {
    private Socket socket;
    private ChatServer server;
    private PrintWriter writer;
    private String userName;

    public UserThread(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            userName = reader.readLine();
            server.addUserName(userName);

            String serverMessage = "New user connected: " + userName;
            server.broadcast(serverMessage, this);

            String clientMessage;
            do {
                clientMessage = reader.readLine();
                StringTokenizer st = new StringTokenizer(clientMessage, "#");
                String msg = st.nextToken();
                String recipients = st.nextToken();

                // StringTokenizer recTokens = new StringTokenizer(recipients);
                // while (recTokens.hasMoreTokens())
                // try {
                if (recipients.equals("All") || recipients.equals("all")) {
                    serverMessage = "[" + userName + "]: " + msg;
                    server.broadcast(serverMessage, this);
                } else {
                    serverMessage = "[" + userName + "]: " + msg;
                    server.sendMessageTo(serverMessage, recipients);
                }

                // } catch (NoSuchElementException e) {
                // System.out.println("Hej");
                // }

            } while (!clientMessage.equals("bye"));

            server.removeUser(userName, this);
            socket.close();

            serverMessage = userName + " has quit.";
            server.broadcast(serverMessage, this);

        } catch (IOException e) {
            System.out.println("Error in UserThread: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void printUsers() {
        if (server.hasUsers()) {
            writer.println("Connected users:" + server.getUserNames());
        } else {
            writer.println("No other users connected.");
        }
    }

    public String getUserName() {
        return this.userName;
    }

    void sendMessage(String message) {
        writer.println(message);
    }

}