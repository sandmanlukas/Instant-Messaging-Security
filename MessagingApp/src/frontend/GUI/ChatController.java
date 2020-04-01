import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
    @FXML
    private TextField text;
    @FXML
    private AnchorPane chatPane;
    @FXML
    private VBox rightVBox;
    @FXML
    private HBox senderHBox; //TODO: doesn't work if static
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TabPane tabPane;

    private static Client controllerClient;
    private Stage chatStage;




    public ChatController(){

    }

    public static void logout(){
        controllerClient.logOut = true;
        Platform.exit();
        System.exit(0);
    }


    public ChatController(String username, Stage chatStage) throws IOException {
        controllerClient = new Client(username);
        controllerClient.run();
        this.chatStage = chatStage;
        //startThread();
    }


    public void sendMessage(){
        Label msg = new Label(text.getText() + " <");
        msg.setAlignment(Pos.CENTER_RIGHT);
        //rightVBox.setAlignment(Pos.BOTTOM_LEFT);
        //rightVBox.getChildren().add(msg);

        //senderHBox.getChildren().add(new Text ("\n")); //Add empty row on the other side
        //sentMessages.add(msg);

        senderHBox.setAlignment(Pos.BOTTOM_RIGHT);
        senderHBox.getChildren().add(msg);
        controllerClient.setForMessage(text.getText());
        text.clear();
    }

    public void recieveMessage(){
        Text t = new Text(controllerClient.received);
        System.out.println("message: \n" + t.getText() );
        Label msgReceived = new Label("> " + t.getText());
        msgReceived.setAlignment(Pos.BOTTOM_LEFT);


        senderHBox.setAlignment(Pos.BOTTOM_LEFT);
        senderHBox.getChildren().add(msgReceived);
        //senderHBox.getChildren().add(new Text("\n")); //Add empty row on other side

       // rightVBox.getChildren().add(msgReceived);
        controllerClient.newReceive = false;
    }

    @FXML
    public void writeMessageEnter(KeyEvent event){
        if (event.getCode() == KeyCode.ENTER){
            sendMessage();
        }
    }
    @FXML
    public void writeMessage (){
        sendMessage();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Thread recMessage = new Thread(() -> {
            Runnable updater = () -> {
                if (controllerClient.newReceive) {
                    recieveMessage();
                }
            };
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(updater);
            }
        });
        recMessage.setDaemon(true);
        recMessage.start();
    }
}
