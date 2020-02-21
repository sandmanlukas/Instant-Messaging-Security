    import java.io.*;
    import java.util.*;
    import java.net.*;
  
// Server class 
public class Server  
{ 
  
    // Vector to store active clients 
    static Vector<ClientHandler> ar = new Vector<>();

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
            ClientHandler mtch = new ClientHandler(s,"client " + i, dis, dos);
  
            // Create a new Thread with this object. 
            Thread t = new Thread(mtch); 
              
            System.out.println("Adding this client to active client list"); 
  
            // add this client to active clients list 
            ar.add(mtch); 
  
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
class ClientHandler implements Runnable  
{ 
    Scanner scn = new Scanner(System.in); 
    private String name; 
    //final DataInputStream dis;
    //final DataOutputStream dos;
    ObjectOutputStream dos;
    ObjectInputStream dis;
    Socket s; 
    boolean isloggedin; 
      
    // constructor 
    public ClientHandler(Socket s, String name, 
                            ObjectInputStream dis, ObjectOutputStream dos) {
        this.dis = dis; 
        this.dos = dos; 
        this.name = name; 
        this.s = s; 
        this.isloggedin=true; 
    } 
  
    @Override
    public void run() {
        testServer server = new testServer();
        //String received;
        while (true)  
        { 
            try
            { 
                // receive the object
                Message msg = (Message) dis.readObject();
                if (msg.getType() == "initMsg") {
                    server.addClient(msg.getSnd(), (preKeyBundlePublic) msg.getMsg(), "donno");
                }
                System.out.println(msg.getMsg());
                  
                //if(received.equals("logout")){
                if((msg.getType()).equals("logout")) {
                    this.isloggedin=false; 
                    this.s.close(); 
                    break; 
                } 

                String recipient=msg.getRec();
  
                // search for the recipient in the connected devices list. 
                // ar is the vector storing client of active users 
                for (ClientHandler mc : Server.ar)  
                { 
                    // if the recipient is found, write on its 
                    // output stream 
                    if (mc.name.equals(recipient) && mc.isloggedin)
                    { 

                        mc.dos.writeObject(msg);
                        break; 
                    } 
                } 
            } catch (Exception e) {
                  
                e.printStackTrace(); 
            } 
              
        } 
        try
        { 
            // closing resources 
            this.dis.close(); 
            this.dos.close(); 
              
        }catch(IOException e){ 
            e.printStackTrace(); 
        } 
    } 
} 