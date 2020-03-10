//package Controller;

import java.sql.SQLException;

public class Controller {
    final PortalConnection c = new PortalConnection();

    public Controller() throws SQLException, ClassNotFoundException {



    }

    public boolean login(String username, String password) {

        if (c.userExists(username)){
            System.out.println("User already exists, tries to log in.");
            System.out.println("pass: " + password);
           return c.correctPassword(username, password);
        }
        else if (!c.userExists(username)){
            c.newUser(username,password);
            System.out.println("User doesn't exists, creates a new user and logs him in.");
            return true;
        }

        //TODO call method in model/main to log in
        return false;
    }
}
