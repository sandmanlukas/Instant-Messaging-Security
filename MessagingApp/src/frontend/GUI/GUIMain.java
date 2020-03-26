import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;


public class GUIMain extends Application {
    private TextField username;
    private PasswordField password;
    private Controller cont;
    private Stage primaryStage;
    private final StackPane root = new StackPane();


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws SQLException, ClassNotFoundException {

        this.primaryStage = primaryStage;
        primaryStage = new Stage();
        cont = new Controller();

        primaryStage.setTitle("Main page");

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
        tryLogin();
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
        //userInput = username.getText();
        //passInput = password.getText();

        userInput = "lukas";
        passInput = "test123";

        //sends login input to Controller

        if (cont.login(userInput, passInput)) {
            try {
                //mainChatPage(userInput);
                GUIChat chat = new GUIChat(primaryStage, userInput);
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
}
