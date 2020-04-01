import java.sql.*;
import java.util.Properties;

public class PasswordConnection {

    static final String DATABASE = "jdbc:postgresql://localhost/kandidatpsw";
    static final String USERNAME = "postgres";
    static final String PASSWORD = "postgres";

    private Connection conn;

    public PasswordConnection() throws SQLException, ClassNotFoundException {
        this(DATABASE, USERNAME, PASSWORD);
    }

    public PasswordConnection(String db, String user, String pwd) throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pwd);
        conn = DriverManager.getConnection(db, props);
    }

    public boolean userExists (String username){
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT EXISTS(SELECT username FROM passwordview WHERE username=?)")) {
            ps.setString(1,username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()){
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Failure! in userExists");

        return false;
    }

    public void newUser(String userName, String password) {
        try (PreparedStatement ps = conn.prepareStatement (
                "INSERT INTO passwordView VALUES (?,?)")) {

            ps.setString(1, userName);
            ps.setString(2, Argon2Encryption.getArgon(password));
            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //TODO: Fix so this happens client side.
    public boolean correctPassword(String userName, String password) {
        try (PreparedStatement ps = conn.prepareStatement (
                "SELECT hash FROM passwordView WHERE userName=?")) {

            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String pswHash = rs.getString("hash");
                //TODO: return pswHash and verify it client side.
                return Argon2Encryption.verifyArgon(pswHash, password);
            } else {
                return false;
            }
        }
        catch (SQLException e) {
           e.printStackTrace();
        }
       return false;
    }

    public static void main(String[] args){
        try{
            PasswordConnection c = new PasswordConnection();

            c.newUser("lukas", "test123");
            c.newUser("henrik", "hej2314");
            c.newUser("john", "qwerty123");
            c.newUser("hampus", "test123");


            //Argon2Encryption.verifyArgon(correctPassword("lukas"), "lukas");


            c.userExists("lukas");
            c.userExists("viktor");

        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }


    }
}