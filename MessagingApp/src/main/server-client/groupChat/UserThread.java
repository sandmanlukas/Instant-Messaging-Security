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
                String[] st = clientMessage.split("#");
                String msg = st[0];
                String tempRec = st[1];
                String[] recipients = tempRec.split(" ");

    
                    for(int i = 0;i < recipients.length; i++){
                        if (recipients[i].equals("All") || recipients[i].equals("all")) {
                            serverMessage = "[" + userName + "]: " + msg;
                            server.broadcast(serverMessage, this);
                        } else {
                            serverMessage = "[" + userName + "]: " + msg;
                            server.sendMessageTo(serverMessage, recipients[i]);
                        }
                    }

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