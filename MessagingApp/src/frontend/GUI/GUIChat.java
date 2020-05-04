import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;


public class GUIChat {
    @FXML
    private static Stage chatStage;

    //Setter to set a certain stage.
    public static void setStage(Stage primaryStage)  {
        chatStage = primaryStage;
    }

    // Constructor to load a new chat window. Creates a new client and loads the chatcontroller.
    public GUIChat(String username, AnchorPane rootPane) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GUIChat.fxml"));
        AnchorPane pane = loader.load();
        rootPane.getChildren().setAll(pane);

        Client controllerClient = new Client(username);

        ChatController chatController = loader.getController();
        controllerClient.clientController = chatController;
        chatController.setClient(controllerClient);
        //Starts the client thread.
        controllerClient.run();

        // When closed, logs the user out.
        chatStage.setOnCloseRequest(e -> ChatController.logout());
    }


}
