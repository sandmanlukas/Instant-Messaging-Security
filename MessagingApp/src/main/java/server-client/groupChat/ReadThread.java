
import java.net.*;
import java.io.*;

/**
 * ReadThread
 */
public class ReadThread extends Thread {
    private BufferedReader reader;
    private final ChatClient client;

    public ReadThread(Socket socket, ChatClient client) {
        this.client = client;

        try {
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));

        } catch (IOException e) {
            System.out.println("Error occurred while getting input stream: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {

            try {
                String response = reader.readLine();
                System.out.println(response);

                // prints the users username after the displaying the servers message
                if (client.getUserName() != null) {
                    System.out.println("[" + client.getUserName() + "]: ");

                }

            } catch (IOException e) {
                System.out.println("Error reading from server: " + e.getMessage());
                e.printStackTrace();
                break;
            }

        }
    }

}