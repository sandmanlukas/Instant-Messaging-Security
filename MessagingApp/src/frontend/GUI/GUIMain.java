import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GUIMain.fxml"));
        Parent root = loader.load();

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Welcome to the CHAT!");
        this.primaryStage.setScene(new Scene(root));
        //cont = new Controller();
        this.primaryStage.show();
        GUIChat.setStage(primaryStage);
        primaryStage.setOnCloseRequest(e -> primaryStage.close());
    }
}
