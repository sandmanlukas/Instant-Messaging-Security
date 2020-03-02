import java.util.HashMap;

public class testServer {

    private int activeUsers;
    public final HashMap<String, preKeyBundlePublic> clients;

    public testServer() {
        activeUsers = 0;
        clients = new HashMap<>();
    }

    public void addClient(String client, preKeyBundlePublic preKeys) {
        clients.put(client, preKeys);
        activeUsers++;
    }

    public preKeyBundlePublic getClient(String client) {
        return clients.get(client);
    }

}
