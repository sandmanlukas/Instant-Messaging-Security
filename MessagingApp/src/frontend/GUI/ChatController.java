import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
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
import java.util.regex.Pattern;

public class ChatController implements Initializable {
    @FXML
    private TextField text;
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

    public void openTab(){
        Tab tab = new Tab();
        tab.setText("New Tab");
        tabPane.getTabs().add(tab);
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(tab);
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

    public void messageSwitch(){
        String message = text.getText();
        switch (message){
            case "\\c":

        }
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
        String message = text.getText();
        String test = "\\clear";
        String test1 = "clear";
       // String command = message.substring(message.indexOf("\\" + 1));
        //System.out.println(command);
        if(text.getText().startsWith("\\clear")){
            rightVBox.getChildren().clear();
            text.clear();
        }else{
            //openTab();
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
