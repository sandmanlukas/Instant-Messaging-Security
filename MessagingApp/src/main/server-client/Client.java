import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client {
    final static int ServerPort = 1234;

    public static void main(String args[]) throws IOException, ClassNotFoundException {
        final Scanner scn = new Scanner(System.in);
        Curve curveClass = new Curve();

        testClient client = new testClient("User1", curveClass.generatePreKeyBundle(), "192.168.0.2", 1234 );

        /*// getting localhost ip
        InetAddress ip = client.getIp();

        System.out.println("Connecting to " + ip + " on port " + ServerPort);
        System.out.println("Type in Username: ");

        final String userName = scn.nextLine();

        // establish the connection
        Socket s = client.getSocket();*/

        client.initMessage();

        // obtaining input and out streams

        //ObjectOutputStream objectOutput = new ObjectOutputStream(s.getOutputStream());
        //ObjectInputStream objectInput = new ObjectInputStream(s.getInputStream());
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
                    client.sendMessage(recipient, msgToSend);

                    Message m = new Message(" ",recipient,"message",msgToSend);
                    //Message m = new Message(userName, recipient, "message", msgToSend);

                    try {
                        // write on the output stream
                        client.objectOutput.writeObject(m);
                    }
                    catch (Exception e) {

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
                        Message msg = (Message) client.objectInput.readObject();
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