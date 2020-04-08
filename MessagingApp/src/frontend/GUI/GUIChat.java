import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;


public class GUIChat {
    @FXML
    private static Stage chatStage;
    @FXML
    private AnchorPane pane;

    public static void setStage(Stage primaryStage)  {
        chatStage = primaryStage;
    }

    public GUIChat(String username, AnchorPane rootPane) throws IOException, SQLException, ClassNotFoundException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GUIChat.fxml"));
        pane = loader.load();
        rootPane.getChildren().setAll(pane);

        Client controllerClient = new Client(username);

        ChatController chatController = loader.getController();
        controllerClient.clientController = chatController;
        chatController.setClient(controllerClient);
        controllerClient.run();

        chatStage.setOnCloseRequest(e -> ChatController.logout());
    }


}
