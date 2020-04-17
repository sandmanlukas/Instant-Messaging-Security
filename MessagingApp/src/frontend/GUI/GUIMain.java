import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;



public class GUIMain extends Application {
    public Parent root;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GUIMain.fxml"));
        root = loader.load();

        primaryStage.setTitle("Welcome to the CHAT!");
        primaryStage.setScene(new Scene(root));
        //cont = new Controller();
        primaryStage.show();
        GUIChat.setStage(primaryStage);
        primaryStage.setOnCloseRequest(e -> primaryStage.close());
    }
}
