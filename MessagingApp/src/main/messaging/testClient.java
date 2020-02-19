import java.util.HashMap;

public class testClient {

    Curve curveClass = new Curve();

    private final String username;
    private final int ip;
    private HashMap<String, Session> map;

    testClient(String username, int ip) {
        this.username = username;
        this.ip = ip;
        map = new HashMap<>();
    }

    public int getIp() {
        return ip;
    }

    public String getUsername() {
        return username;
    }

    public void addSession(Session session) {
        map.put(session.getBob(), session);
    }

    public Session getSession(String bob) {
        Session session = map.get(bob);
        return session;
    }
}
