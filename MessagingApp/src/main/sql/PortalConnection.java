import java.sql.*;
import java.util.Properties;

public class PortalConnection {

    static final String DATABASE = "jdbc:postgresql://localhost/kandidatpsw";
    static final String USERNAME = "postgres";
    static final String PASSWORD = "henkehe98";

    private Connection conn;

    public PortalConnection() throws SQLException, ClassNotFoundException {
        this(DATABASE, USERNAME, PASSWORD);
    }

    public PortalConnection(String db, String user, String pwd) throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pwd);
        conn = DriverManager.getConnection(db, props);
    }

    public void newUser(String userName, String hash) {
        try (PreparedStatement ps = conn.prepareStatement (
                "INSERT INTO passwords VALUES (?,?)");) {

            ps.setString(1, userName);
            ps.setString(2, hash);
            ps.executeUpdate();
            System.out.println("success!!");
        }
        catch (SQLException e) {
            System.out.println("fail!");
            System.out.println(e);
        }
    }

    public boolean correctPassword(String userName, String hash) {
        try (PreparedStatement ps = conn.prepareStatement (
                "SELECT hash FROM passwordView WHERE userName=?");) {

            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String pswHash = rs.getString("hash");
                return (pswHash.equals(hash));
            } else {
                return false;
            }
        }
        catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }
}