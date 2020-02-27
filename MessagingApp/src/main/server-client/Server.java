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
                //System.out.println(msg.getType());

                switch(msg.getType()) {
                    case "usernameMsg":
                        for(ClientHandler mc : Server.ar) {
                            //System.out.println(mc.name);
                            if (mc.name.equals(msg.getSnd())) {
                                mc.setName((String) msg.getMsg());
                                Message m = new Message("Server", mc.name, "usernameRec", "");
                                mc.dos.writeObject(m);
                                //System.out.println(msg.getType());
                                break;
                            }
                        }
                        break;
                    case "initMsg":
                        for(ClientHandler mc : Server.ar) {
                            if (mc.name.equals(msg.getSnd())) {
                                byte[][] keys = (byte[][]) msg.getMsg();
                                ArrayList<byte[]> arrayKeys = new ArrayList<>();
                                for(int i = 3; i < keys.length; i++) {
                                    arrayKeys.add(keys[i]);
                                }
                                //System.out.println(msg.getType());
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
                    default:
                        String recipient = msg.getRec();
                        System.out.println("Forwarded a message");
                        // search for the recipient in the connected devices list.
                        // ar is the vector storing client of active users
                        for (ClientHandler mc2 : Server.ar) {
                            // if the recipient is found, write on its
                            // output stream
                            if (mc2.name.equals(recipient) && mc2.isloggedin) {

                                mc2.dos.writeObject(msg);
                                break;
                            }
                        }
                        break;
                }

                // if(received.equals("logout")){
                if ((msg.getType()).equals("logout")) {
                    this.isloggedin = false;
                    this.s.close();
                    break;
                }

            } catch (Exception e) {

                e.printStackTrace();
            }

        }
        try {
            // closing resources
            this.dis.close();
            this.dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
