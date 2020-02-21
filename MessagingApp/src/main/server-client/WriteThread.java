import java.io.*;
import java.net.*;

/**
 * WriteThread
 */
public class WriteThread extends Thread {
    private PrintWriter writer;
    private Socket socket;
    private ChatClient client;

    public WriteThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

        } catch (IOException e) {
            System.out.println("Error occurred while getting output stream: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Console console = System.console();

        String userName = console.readLine("\nEnter your Username: ");
        client.setUserName(userName);
        writer.println(userName);

        String text;

        do {
            text = console.readLine("[" + userName + "]: ");
            writer.println(text);

        } while (!text.equals("bye"));

        try {
            socket.close();
        } catch (IOException ex) {

            System.out.println("Error writing to server: " + ex.getMessage());
        }

    }
}