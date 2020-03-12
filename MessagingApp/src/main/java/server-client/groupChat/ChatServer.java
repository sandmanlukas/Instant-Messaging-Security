import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ChatServer {

    private final int port;
    private final Set<String> userNames = new HashSet<>();
    private final Set<UserThread> userThreads = new HashSet<>();
    public final testServer server;

    public ChatServer(int port) {
        this.port = port;
        server = new testServer();
    }

    public void execute() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port: " + port);

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("New user connected");
                    UserThread newUser = new UserThread(socket, this);
                    userThreads.add(newUser);
                    newUser.start();
                }catch (IOException e){
                    System.out.println("Error in the server: " + e.getMessage());
                    e.printStackTrace();
                    break;
                }

            }
        } catch (IOException e) {
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
            System.out.println("The user " + userName + " has quit the application");

        }
    }

    Set<String> getUserNames() {
        return this.userNames;
    }

    boolean hasUsers() {
        return !this.userNames.isEmpty();
    }

}