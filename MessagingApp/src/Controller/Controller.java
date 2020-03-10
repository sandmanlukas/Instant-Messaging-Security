//package Controller;

import java.sql.SQLException;

public class Controller {
    PortalConnection c = new PortalConnection();

    public Controller() throws SQLException, ClassNotFoundException {



    }

        //might be boolean to see if inlog was successful
    public boolean login(String username, String password) throws SQLException, ClassNotFoundException {

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
