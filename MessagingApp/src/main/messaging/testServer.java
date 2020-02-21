import java.util.HashMap;

public class testServer {

    private int activeUsers;
    public HashMap<String, preKeyBundlePublic> clients;

    public testServer() {
        activeUsers = 0;
        HashMap<String, preKeyBundlePublic> clients = new HashMap<>();
    }

    public void addClient(String client, preKeyBundlePublic preKeys) {
        clients.put(client, preKeys);
        activeUsers++;
    }

    public preKeyBundlePublic getClient(String client) {
        return clients.get(client);
    }

}
