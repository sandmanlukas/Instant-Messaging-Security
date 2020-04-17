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
    private final PasswordConnection c = new PasswordConnection();
    @FXML
    private PasswordField password;
    @FXML
    private TextField username;
    @FXML
    public AnchorPane rootPane;


    public MainController() throws SQLException, ClassNotFoundException {
    }

    public boolean login(String username, String password) {

        if (c.userExists(username)){
            if(c.correctPassword(username, password)) return true;
            else {
                System.out.println("That username is taken. Try another one.");
                return false;
            }
        }
        else if (username.isEmpty() || password.isEmpty()){
            return false;
        }
        else if (!c.userExists(username)){
            c.newUser(username, password);
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

        userInput = username.getText();
        passInput = password.getText();

        UsernameValidator usernameValidator = new UsernameValidator();
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
            startClient();
        }
    }

    @FXML
    public void mouseLogin() throws Exception {
        startClient();
    }

    private void startClient() throws java.io.IOException {
        if (tryLogin()){
            //FXMLLoader loader = new FXMLLoader(getClass().getResource("GUIChat.fxml"));
            //AnchorPane pane = loader.load();
            //rootPane.getChildren().setAll(pane);
            //Client controllerClient = new Client(username.getText());
            //TODO: to access setOnCloseRequest, must be a better way
            new GUIChat(username.getText(), rootPane);

            //ChatController chatController = loader.getController();
            //controllerClient.clientController = chatController;
            //chatController.setClient(controllerClient);
            //chatController.initialize();

            //controllerClient.run();
            //new ChatController(username.getText(), primaryStage);

            //new GUIChat(username.getText());

        }
    }


}
