import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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

public class ChatController {
    @FXML
    private TextField text;
    @FXML
    private AnchorPane chatPane;
    @FXML
    private VBox rightVBox;
    @FXML
    private HBox senderHBox; //TODO: doesn't work if static
    private static Client controllerClient;
    private Stage chatStage;



    public static void setAndStartClient(Client client) throws IOException {
        //controllerClient = client;
        //controllerClient.run();
        //tartThread();
    }


    public ChatController(){

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
        senderHBox.setAlignment(Pos.BOTTOM_RIGHT);
        senderHBox.getChildren().add(sent);
        senderHBox.getChildren().add(new Text ("\n")); //Add empty row on the other side
        controllerClient.setForMessage(text.getText());
        text.clear();
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
                    Text t = new Text(controllerClient.received);
                    System.out.println("message: " + controllerClient.received );
                    senderHBox.setAlignment(Pos.BOTTOM_LEFT);
                    senderHBox.getChildren().add(t);
                    senderHBox.getChildren().add(new Text("\n")); //Add empty row on other side
                    controllerClient.newReceive = false;

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
        Platform.exit();
        System.exit(0);
    }

}
