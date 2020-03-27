import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;


public class GUIMain extends Application {
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    private MainController cont;
    private Stage primaryStage;
    //private final StackPane root = new StackPane();


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws SQLException, ClassNotFoundException, IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GUIMain.fxml"));
        loader.setController(new MainController());
        Parent root  = loader.load();

        primaryStage.setTitle("Welcome to the CHAT!");
        primaryStage.setScene(new Scene(root));
        //cont = new Controller();
        primaryStage.show();

        //this.primaryStage = primaryStage;
        //primaryStage = new Stage();
        //cont = new Controller();

        //primaryStage.setTitle("Main page");

        /*
        root.getStylesheets().add(getClass().getResource("GUIMain.css").toExternalForm());
        //root = new StackPane();

        username = new TextField();
        password = new PasswordField();

        username.setMaxSize(100, 10);
        username.setPromptText("Username");
        username.setTranslateX(0.0);
        username.setTranslateY(0.0);

        password.setMaxSize(100, 10);
        password.setPromptText("Password");
        password.setTranslateX(0.0);
        password.setTranslateY(25.0);

        Button login = new Button("Login");
        login.setTranslateX(0.0);
        login.setTranslateY(50.0);

        root.getChildren().add(username);
        root.getChildren().add(password);
        root.getChildren().add(login);


        Scene scene = new Scene(root, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        //TODO: REMOVE LATER, TESTING ONLY
        //tryLogin();
        Stage finalPrimaryStage = primaryStage;
        password.setOnKeyPressed((keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                if (tryLogin()) finalPrimaryStage.close();
            }
        }));

        primaryStage.setOnCloseRequest(e -> finalPrimaryStage.close());

        login.setOnAction(event -> {
            if (tryLogin()) finalPrimaryStage.close();
        });
*/
    }
/*

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

    public boolean tryLogin() {
        String userInput;
        String passInput;

        //TODO: REMOVE LATE TESTING ONLY
        userInput = username.getText();
        passInput = password.getText();

        //userInput = "lukas";
        //passInput = "test123";

        //sends login input to Controller

        if (login(userInput, passInput)) {
            try {
                //mainChatPage(userInput);
                new GUIChat(primaryStage, userInput);
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

 */

}
