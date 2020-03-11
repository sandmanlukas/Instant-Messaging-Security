//package Controller;

import javafx.stage.Stage;
import javafx.stage.Window;

import java.sql.SQLException;

public class Controller {
    final PortalConnection c = new PortalConnection();



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
        else if (!c.userExists(username)){
            c.newUser(username,password);
            System.out.println("User doesn't exists, creates a new user and logs him in.");
            return true;
        }

        return false;
    }
}
