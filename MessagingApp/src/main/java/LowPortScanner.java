import java.io.IOException;
import java.net.*;
import java.io.*;

public class LowPortScanner {

    public static void main(String[] args) {
        String host = "localhost";

        if (args.length > 0) {
            host = args[0];
        }
        for (int i = 1; i < 1024; i++) {
            try {
                Socket s = new Socket(host, i);
                System.out.println("There is a server on port: " + i + " of " + host);
            } catch (UnknownHostException ex) {
                System.err.println(ex);
                break;
            } catch (IOException ex) {
                // Must not be a server on this port
            }
        } // End For

    } // End Main
} // End PortScanner