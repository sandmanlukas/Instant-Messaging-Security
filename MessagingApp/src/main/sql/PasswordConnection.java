import java.sql.*;
import java.util.Properties;

public class PasswordConnection {

    // connection  to the database that stores the hashed passwords
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

    //method to determine if a user exists in the database
    public boolean userExists (String username){
        // uses prepared statements to protect against sql injections
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

        return false;
    }

    // adds a new user to the database, and hashes the entered password using Argon
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
    //method to confirm a password is correct
    // uses Argon to verify the hash in the database with the given password
    public boolean correctPassword(String userName, String password) {
        try (PreparedStatement ps = conn.prepareStatement (
                "SELECT hash FROM passwordView WHERE userName=?")) {

            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String pswHash = rs.getString("hash");
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


}