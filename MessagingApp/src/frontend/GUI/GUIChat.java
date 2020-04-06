import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;


public class GUIChat {
    @FXML
    private static Stage chatStage;

    public static void setStage(Stage primaryStage)  {
        chatStage = primaryStage;
    }

    public GUIChat()  {
      //  chatController = new ChatController(username);
        chatStage.setOnCloseRequest(e -> ChatController.logout());
    }


}
