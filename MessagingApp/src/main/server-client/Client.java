import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    final static int ServerPort = 1234;

    public static void main(String args[]) throws IOException, ClassNotFoundException {
        final Scanner scn = new Scanner(System.in);

        // getting localhost ip
        InetAddress ip = InetAddress.getByName("localhost");

        System.out.println("Connecting to " + ip + " on port " + ServerPort);

        // establish the connection
        Socket s = new Socket(ip, ServerPort);

        // obtaining input and out streams
        
        ObjectOutputStream objectOutput = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream objectInput = new ObjectInputStream(s.getInputStream());
        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    // read the message to deliver.
                    String msg = scn.nextLine();
                    StringTokenizer st = new StringTokenizer(msg, "#"); 
                    String msgToSend = st.nextToken(); 
                    String recipient = st.nextToken(); 
                    Message m = new Message(" ",recipient,"message",msgToSend);

                    try {
                        // write on the output stream
                        objectOutput.writeObject(m);
                    } 
                    catch (Exception e){
                        e.printStackTrace();             

                    } 
                    
                }
            }
        });

        // readMessage thread
        Thread readMessage = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    try {
                        // read the message sent to this client
                        Message msg = (Message) objectInput.readObject();
                        System.out.println(msg.getMsg());
                        }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        sendMessage.start();
        readMessage.start();

    }
}