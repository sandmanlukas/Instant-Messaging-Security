//package Controller;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;


import java.sql.SQLException;


public class MainController {
    //Connection to the database, where the hashed passwords are stored.
    private final PasswordConnection c = new PasswordConnection();
    @FXML
    private PasswordField password;
    @FXML
    private TextField username;
    @FXML
    public AnchorPane rootPane;


    public MainController() throws SQLException, ClassNotFoundException {
    }

    //Method to check if a password is correct
    public boolean login(String username, String password) {

        //Checks if a user exists in the database.
        if (c.userExists(username)){
            //If the password of the user matches the one stored in the database, return true. Else return false.
            return c.correctPassword(username, password);
        }
        // If any of the textfields are empty, return false.
        else if (username.isEmpty() || password.isEmpty()){
            return false;
        }
        //If the user doesn't exist in the database, create a new user and return true.
        else if (!c.userExists(username)){
            c.newUser(username, password);
            return true;
        }

        //Otherwise, return false.
        return false;
    }

    //Method to show an alert if a login is failed.
    public void failLogin() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Wrong Password or Username");
        alert.setHeaderText("Wrong password or Username.");
        alert.setContentText("You entered the wrong password or Username. Try again.");
        alert.showAndWait();
    }
    //Method to show an alert if a username is illegal.
    public void illegalUsername(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Illegal Username");
        alert.setHeaderText("Illegal Username");
        alert.setContentText("You entered an illegal username. Allowed symbols are a-z, A-Z, 0-9, _ and -. \nThe password has to be at least 3 characters long and at max 15 characters. ");
        alert.showAndWait();
    }

    //Method that is called when a user tries to log in.
    public boolean tryLogin() {
        String userInput;
        String passInput;

        // Gets the text from the username- and passwordfields.
        userInput = username.getText();
        passInput = password.getText();

        //Checks if the username is valid according to the given rules.
        //If not return false and show an alert.
        UsernameValidator usernameValidator = new UsernameValidator();
        if (!usernameValidator.validate(userInput)){
            illegalUsername();
            username.setText("");
            return false;
        }


        //Checks if the entered username and password are correct.
        // Shows an alert otherwise.
        if (login(userInput, passInput)) {
                return true;
        }
        else {
            failLogin();
            return false;
        }

    }

    //Calls startClient() when enter is pressed.
    @FXML
    public void enterLogin (KeyEvent event) throws Exception {
        if(event.getCode() == KeyCode.ENTER){
            startClient();
        }
    }

    //Calls startClient() when the login button is pressed.
    @FXML
    public void mouseLogin() throws Exception {
        startClient();
    }

    //If tryLogin() succeeds a chat window is created and opened.
    private void startClient() throws java.io.IOException {
        if (tryLogin()){
            new GUIChat(username.getText(), rootPane);

        }
    }


}
