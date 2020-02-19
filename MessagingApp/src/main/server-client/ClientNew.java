import java.net.*;
import java.io.*;
import java.util.*;

public class ClientNew {

    public static void main(String[] args)
            throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
        System.out.println("Welcome!");

        // Localhost ip
        InetAdress ip = InetAddress.getLocalHost();
        // System.out.println("Please entre server ip and port: ");
        // Scanner scanner = new Scanner(System.in);
        // String ipPort = scanner.nextLine();

        System.out.println("Connecting to " + ip + " on port " + serverPort);
        Socket socket = new Socket(ip, serverPort);

        final DataInputStream dis = new DataInputStream(s.getInputStream());
        final DataOutputStream dos = new DataOutputStream(s.getOutputStream());
    }
}