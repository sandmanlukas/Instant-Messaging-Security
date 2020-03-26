//package Controller;



import java.sql.SQLException;

public class Controller {
    final PasswordConnection c = new PasswordConnection();



    public Controller() throws SQLException, ClassNotFoundException {



    }



    public boolean login(String username, String password) {

        if (c.userExists(username)){
            if(c.correctPassword(username,password)) return true;
            else {
                System.out.println("That username is taken. Try another one.");
                return false;
            }
        }
        else if (username.equals("") || password.equals("")){
            return false;
        }
        else if (!c.userExists(username)){
            c.newUser(username,password);
            System.out.println("User doesn't exists, creates a new user and logs him in.");
            return true;
        }

        return false;
    }


}
