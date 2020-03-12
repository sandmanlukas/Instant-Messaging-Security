import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient {

    private final int port;
    private final String host;
    private String userName;

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void execute() {
        try {
            Socket socket = new Socket(host, port);
            System.out.println("Connected to the server successfully");
            new ReadThread(socket, this).start();
            new WriteThread(socket, this).start();
        } catch (UnknownHostException e) {
            System.out.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    String getUserName() {
        return this.userName;
    }

    public static void main(String[] args) {
        if (args.length < 2)
            return;
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        ChatClient client = new ChatClient(host, port);
        client.execute();
    }

}