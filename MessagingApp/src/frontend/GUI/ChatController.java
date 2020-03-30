import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ChatController {
    @FXML
    private TextField text;
    @FXML
    private AnchorPane chatPane;
    @FXML
    private VBox rightVBox;
    @FXML
    private HBox senderHBox;
    private static Client controllerClient;
    private String username;
    private ChatController controller;



    public static void setAndStartClient(Client client){
        controllerClient = client;
    }




    public ChatController(){
        startThread();
    }



    public void sendMessage(){
        Text sent = new Text(text.getText() + " <");
        senderHBox.getChildren().add(sent);
        senderHBox.getChildren().add(new Text ("")); //Add empty row on the other side
        System.out.println("username is " + controllerClient.username);
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
        System.out.println("Thread started");
        Thread recMessage = new Thread(() -> {
            Runnable updater = () -> {
                if (controllerClient.newReceive) {
                    System.out.println("inside updater");
                    Text t = new Text(controllerClient.received);
                    senderHBox.getChildren().add(t);
                    senderHBox.getChildren().add(new Text("")); //Add empty row on other side
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

        //primaryStage.setOnCloseRequest(e -> client.logOut = true);
}
