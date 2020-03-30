//package Controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;


public class MainController {
    private final PasswordConnection c = new PasswordConnection();

    @FXML
    private PasswordField password;
    @FXML
    private TextField username;
    @FXML
    private Stage primaryStage;


    public MainController() throws SQLException, ClassNotFoundException {
    }

    //TODO: REMOVE, TESTING PURPOSES
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

    public void failLogin() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Wrong Password or Username");
        alert.setHeaderText("Wrong password or Username.");
        alert.setContentText("You entered the wrong password or Username. Try again.");
        alert.showAndWait();
    }
    public boolean tryLogin() throws Exception {
        String userInput;
        String passInput;

        //TODO: REMOVE LATE TESTING ONLY
        //userInput = username.getText();
        //passInput = password.getText();

        userInput = "lukas";
        passInput = "test123";

        //sends login input to Controller

        if (login(userInput, passInput)) {
            try {
                //mainChatPage(userInput);
                new GUIChat(primaryStage,userInput);

                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            failLogin();
            System.out.println("Login failed, try again.");
            return false;
        }
        return false;
    }

    public void enterLogin (KeyEvent event) throws Exception {
        if(event.getCode() == KeyCode.ENTER){
            if (tryLogin()) primaryStage.close();
        }
    }

    public void mouseLogin (ActionEvent event) throws Exception {
        if (tryLogin()) primaryStage.close();
    }




}
