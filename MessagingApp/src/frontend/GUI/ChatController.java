import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
import java.util.ArrayList;
import java.util.List;

public class ChatController {
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
    private List<Label> sentMessages = new ArrayList<>();
    private List<Label> receivedMessages = new ArrayList<>();
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
        System.out.println("I'm accessed!");
        controllerClient = new Client(username);
        controllerClient.run();
        this.chatStage = chatStage;
        System.out.println("username: " + controllerClient.username);
        System.out.println("newReceive: " + controllerClient.newReceive);
        startThread();
        //startThread();
        //System.out.println("Controller newReceive: " + controllerClient.newReceive);
    }


    public void sendMessage(){
        Text sent = new Text(text.getText() + " <");
        Label msg = new Label(text.getText() + " <");
        msg.setAlignment(Pos.BOTTOM_RIGHT);
        //senderHBox.setAlignment(Pos.BOTTOM_RIGHT);
        //senderHBox.getChildren().add(sent);
        //senderHBox.getChildren().add(new Text ("\n")); //Add empty row on the other side
        //sentMessages.add(msg);
        rightVBox.getChildren().add(msg);
        controllerClient.setForMessage(text.getText());
        text.clear();
    }
    public void recieveMessage(){
        Text t = new Text(controllerClient.received);
        // System.out.println("message: " + t );
        Label msgReceived = new Label("> " + t.getText());
        //senderHBox.setAlignment(Pos.BOTTOM_LEFT);
        //senderHBox.getChildren().add(t);
        //senderHBox.getChildren().add(new Text("\n")); //Add empty row on other side
        msgReceived.setAlignment(Pos.BOTTOM_LEFT);
        rightVBox.getChildren().add(msgReceived);
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



    //Text recTest = new Text("Testing " + "<, User: " + client.username);

    public void startThread() {
        Thread recMessage = new Thread(() -> {
            Runnable updater = () -> {
                if (controllerClient.newReceive) {
                    /*
                    Text t = new Text(controllerClient.received);
                   // System.out.println("message: " + t );
                    Label msgReceived = new Label("> " + t.getText());
                    //senderHBox.setAlignment(Pos.BOTTOM_LEFT);
                    //senderHBox.getChildren().add(t);
                    //senderHBox.getChildren().add(new Text("\n")); //Add empty row on other side
                    msgReceived.setAlignment(Pos.BOTTOM_LEFT);
                    rightVBox.getChildren().add(msgReceived);
                    controllerClient.newReceive = false;

                     */

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

    //this.scene = new Scene(border, 700, 500);
    //primaryStage.setScene(this.scene);
    //primaryStage.show();

    public void close (WindowEvent event){
        chatStage.setOnCloseRequest(e -> controllerClient.logOut = true);
        //TODO: fix connection reset exception, maybe check so dis.readObject() doesn't read.
        Platform.exit();
        System.exit(0);
    }

}
