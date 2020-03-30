//package Controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;



import java.io.IOException;
import java.sql.SQLException;


public class MainController {
    private final PasswordConnection c = new PasswordConnection();
    private UsernameValidator usernameValidator;
    @FXML
    private PasswordField password;
    @FXML
    private TextField username;
    @FXML
    private AnchorPane rootPane;
    private Stage primaryStage;


    public MainController() throws SQLException, ClassNotFoundException {
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

    public void failLogin() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Wrong Password or Username");
        alert.setHeaderText("Wrong password or Username.");
        alert.setContentText("You entered the wrong password or Username. Try again.");
        alert.showAndWait();
    }

    public void illegalUsername(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Illegal Username");
        alert.setHeaderText("Illegal Username");
        alert.setContentText("You entered an illegal username. Allowed symbols are a-z, A-Z, 0-9, _ and -. \nThe password has to be at least 3 characters long and at max 15 characters. ");
        alert.showAndWait();
    }
    public boolean tryLogin() {
        String userInput;
        String passInput;

        //TODO: REMOVE LATE TESTING ONLY
        userInput = username.getText();
        passInput = password.getText();

        usernameValidator = new UsernameValidator();
        if (!usernameValidator.validate(userInput)){
            illegalUsername();
            username.setText("");
            return false;
        }


        //userInput = "lukas";
        //passInput = "test123";

        //sends login input to Controller


        if (login(userInput, passInput)) {
          //  try {
                //new GUIChat(userInput);
                //new ChatController(userInput);
                return true;
           // } catch (IOException e) {
             //   e.printStackTrace();
          //  }
        } else {
            failLogin();
            System.out.println("Login failed, try again.");
            return false;
        }
      //  return false;
    }

    @FXML
    public void enterLogin (KeyEvent event) throws Exception {
        if(event.getCode() == KeyCode.ENTER){
            if (tryLogin()){
                AnchorPane pane = FXMLLoader.load(getClass().getResource("GUIChat.fxml"));
                rootPane.getChildren().setAll(pane);
                new GUIChat(username.getText());


                //primaryStage.close();
            }
        }
    }

    @FXML
    public void mouseLogin() throws Exception {
        if (tryLogin()){
            AnchorPane pane = FXMLLoader.load(getClass().getResource("GUIChat.fxml"));
            rootPane.getChildren().setAll(pane);
            new GUIChat(username.getText());

        }
    }




}
