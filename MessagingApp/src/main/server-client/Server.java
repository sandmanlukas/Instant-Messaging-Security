import java.io.*;
import java.util.*;
import java.net.*;

// Server class 
public class Server {

    // Vector to store active clients
    public static Vector<ClientHandler> ar = new Vector<>();
    public static testServer server = new testServer();

    // counter for clients 
    static int i = 0;


  
    public static void main(String[] args) throws IOException  
    { 
        // server is listening on port 1234 
        ServerSocket ss = new ServerSocket(1234); 
          
        Socket s;

        // running infinite loop for getting 
        // client request 
        while (true)
        { 
            // Accept the incoming request 
            s = ss.accept(); 
  
            System.out.println("New client request received : " + s); 
              
            // obtain input and output streams 
            ObjectInputStream dis = new ObjectInputStream(s.getInputStream());
            ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());

            System.out.println("Creating a new handler for this client..."); 

            // Create a new handler object for handling this request. 
            ClientHandler mtch = new ClientHandler(s,"client" + i, dis, dos);

            System.out.println("Adding this client to active client list"); 
  
            // add this client to active clients list 
            ar.add(mtch);

            // Create a new Thread with this object.
            Thread t = new Thread(mtch);

            //Sends and initial message to the client in order received its username and its preKeyBundlePublic
            Message m = new Message("Server", "client" + i, "usernameMsg", "");
            dos.writeObject(m);
  
            // start the thread. 
            t.start(); 
  
            // increment i for new client. 
            // i is used for naming only, and can be replaced 
            // by any naming scheme 
            i++;

        } 
    } 
} 

// ClientHandler class
class ClientHandler implements Runnable {
    Scanner scn = new Scanner(System.in);
    private String name;
    ObjectOutputStream dos;
    ObjectInputStream dis;
    Socket s;
    boolean isloggedin;

    // constructor
    public ClientHandler(Socket s, String name, ObjectInputStream dis, ObjectOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
        this.isloggedin = true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {



        // String received;
        while (true) {
            try {
                // receive the object
                Message msg = (Message) dis.readObject();

                switch(msg.getType()) {
                    case "usernameMsg":
                        for(ClientHandler mc : Server.ar) {
                            if (mc.name.equals(msg.getSnd())) {

                                //Sets the users name according to their choice, sends a message to verify the change
                                mc.setName((String) msg.getMsg());
                                Message m = new Message("Server", mc.name, "usernameRec", "");
                                mc.dos.writeObject(m);
                                break;
                            }
                        }
                        break;

                    case "initMsg":
                        for(ClientHandler mc : Server.ar) {
                            if (mc.name.equals(msg.getSnd())) {

                                //retrives the preKeyBundlePublic and formats it
                                byte[][] keys = (byte[][]) msg.getMsg();
                                ArrayList<byte[]> arrayKeys = new ArrayList<>();
                                for(int i = 3; i < keys.length; i++) {
                                    arrayKeys.add(keys[i]);
                                }

                                //Adds the client to a map of clients, then sends an verification message
                                Server.server.addClient(mc.name, new preKeyBundlePublic(keys[0], keys[1], keys[2], arrayKeys));
                                Message m = new Message("Server", mc.name, "initRec", "");
                                mc.dos.writeObject(m);
                                break;
                            }
                        }
                        break;

                    case "publicBundleRequest":
                        for(ClientHandler mc : Server.ar) {
                            if (mc.name.equals(msg.getSnd())) {

                                //retrives the requested preKeyBundlePublic and make it serializable by putting it in a 2D byte array
                                preKeyBundlePublic preKeys = Server.server.getClient(msg.getRec());
                                byte[] publicIdentityKey = preKeys.getPublicIdentityKey();
                                byte[] publicPreKey = preKeys.getPublicPreKey();
                                byte[] signedPublicPreKey = preKeys.getSignedPublicPreKey();
                                byte[][] keys = new byte[3 + preKeys.getPublicOneTimePreKeys().size()][];
                                keys[0] = publicIdentityKey;
                                keys[1] = publicPreKey;
                                keys[2] = signedPublicPreKey;
                                for(int i = 0; i < preKeys.getPublicOneTimePreKeys().size(); i++) {
                                    keys[3 + i] = preKeys.getPublicOneTimePreKey(i);
                                }

                                Message m = new Message(msg.getRec(), mc.name, "publicBundleRequestRec", keys);
                                mc.dos.writeObject(m);
                                break;
                            }
                        }
                        break;

                    case "logout":
                        for (ClientHandler mc : Server.ar) {
                            if (mc.name.equals(msg.getSnd())) {
                                //silently close the connection for the user
                                this.isloggedin = false;
                                this.s.close();
                                break;
                            }
                        }
                        break;
                    default:
                        System.out.println("Forwarded a message");
                        for (ClientHandler mc2 : Server.ar) {

                            //forwards the message to the correct online user
                            if (mc2.name.equals(msg.getRec()) && mc2.isloggedin) {
                                mc2.dos.writeObject(msg);
                                break;
                            }
                        }
                        break;
                }

            } catch (Exception e) {

                e.printStackTrace();
            }

        }
    }
}
