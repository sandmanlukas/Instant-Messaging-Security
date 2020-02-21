import javafx.util.Pair;
import java.util.HashMap;

public class testServer {

    private int activeUsers;
    public HashMap<String, Pair<preKeyBundlePublic,String>> clients;

    public testServer() {
        activeUsers = 0;
        HashMap<String, Pair<preKeyBundlePublic, Integer>> clients = new HashMap<>();
    }

    public void addClient(String client, preKeyBundlePublic preKeys, String ip) {
        clients.put(client, new Pair(preKeys, ip));
        activeUsers++;
    }

    public Pair<preKeyBundlePublic, String> getClient(String client) {
        return clients.get(client);
    }

}
