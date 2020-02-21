import javafx.util.Pair;
import java.util.HashMap;

public class testServer {

    private final int ip;
    private int activeUsers;
    public HashMap<String, Pair<preKeyBundlePublic,Integer>> clients;

    public testServer(int ip) {
        this.ip = ip;
        activeUsers = 0;
        HashMap<String, Pair<preKeyBundlePublic, Integer>> clients = new HashMap<>();
    }

    public void addClient(String client, preKeyBundlePublic preKeys, int ip) {
        clients.put(client, new Pair(preKeys, ip));
    }

    public Pair<preKeyBundlePublic, Integer> getClient(String client) {
        return clients.get(client);
    }
    
}
