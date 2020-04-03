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
    private String username;
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
        this.username = username;
        //startThread();
    }


    public void sendMessage(){
        Text sent = new Text("[You]: " + text.getText());
        Label msg = new Label(text.getText() + " <");
        msg.setWrapText(true);
        //msg.setAlignment(Pos.CENTER_RIGHT);
        rightVBox.getChildren().add(sent);
        //rightVBox.getChildren().add(new Text("\n"));
        controllerClient.setForMessage(text.getText());
        text.clear();
    }

    public void recieveMessage(){
        Text t = new Text(controllerClient.received);
        Label msgReceived = new Label("> " + t.getText());
        msgReceived.setWrapText(true);
        rightVBox.getChildren().add(t);
        controllerClient.newReceive = false;
    }

    @FXML
    public void writeMessageEnter(KeyEvent event){
        if (event.getCode() == KeyCode.ENTER){
            if(text.getText().startsWith("\\clear")){
                rightVBox.getChildren().clear();
                text.clear();
            }
            else{
                sendMessage();
            }

        }
    }
    @FXML
    public void writeMessage (){
        if(text.getText().startsWith("\\clear")){
            rightVBox.getChildren().clear();
            text.clear();
        }else{
            sendMessage();
        }
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
