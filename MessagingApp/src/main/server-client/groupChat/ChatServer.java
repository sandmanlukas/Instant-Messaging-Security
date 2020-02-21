import java.util.*;
import java.net.*;
import java.io.*;

public class ChatServer {

    private int port;
    private Set<String> userNames = new HashSet<String>();
    private Set<UserThread> userThreads = new HashSet<UserThread>();

    public ChatServer(int port) {
        this.port = port;
    }

    public void execute() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port: " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");
                UserThread newUser = new UserThread(socket, this);
                userThreads.add(newUser);
                newUser.start();
            }
        } catch (IOException e) {
            System.out.println("Error in the server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // int port = Integer.parseInt(args[0])
        int port = 4444;
        ChatServer server = new ChatServer(port);
        server.execute();
    }

    void sendMessageTo(String msg, String userName) {
        for (UserThread aUser : userThreads) {
            if (aUser.getUserName().equals(userName)) {
                aUser.sendMessage(msg);
            }
        }
    }

    void broadcast(String message, UserThread excludedUser) {
        for (UserThread aUser : userThreads) {
            if (aUser != excludedUser) {
                aUser.sendMessage(message);
            }
        }
    }

    public void addUserName(String userName) {
        userNames.add(userName);
    }

    void removeUser(String userName, UserThread aUser) {
        boolean removed = userNames.remove(userName);
        if (removed) {
            userThreads.remove(aUser);
            System.out.println("The user " + userName + " quitted");

        }
    }

    Set<String> getUserNames() {
        return this.userNames;
    }

    boolean hasUsers() {
        return !this.userNames.isEmpty();
    }

}