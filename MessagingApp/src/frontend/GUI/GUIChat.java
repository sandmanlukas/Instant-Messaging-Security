import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class GUIChat {

    private HBox lowerHBox;
    private VBox textDispVBox;
    private VBox recDispVBox;
    private TextField writeMessage;
    private Button sendButton;
    private Client client;
    Scene scene;

    public GUIChat (Stage primaryStage, String username) throws IOException {

        primaryStage.hide();

        client = new Client(username);
        client.run();

        primaryStage.setTitle("Chat Page");
        //list of active chats, or where to create new conversations
        GridPane chatGrid = new GridPane();
        chatGrid.setScaleX(200);
        chatGrid.setStyle("-fx-border-color: purple;");

        //shows conversations one at the time
        BorderPane conversationGrid = new BorderPane();
        conversationGrid.setScaleX(500);
        conversationGrid.setStyle("-fx-border-color: #A9A9A9;");


        //trying to add text field to write messages at the bottom of the left pane.....

        lowerHBox = new HBox();
        HBox textHBox = new HBox();
        textDispVBox =new VBox(10);
        recDispVBox = new VBox();
        VBox chatDisplay = new VBox();


        writeMessage = new TextField();
        writeMessage.setMaxWidth(Double.MAX_VALUE);
        //writeMessage.setMaxSize(500,100);
        writeMessage.setPromptText("Write message here");
        //conversationGrid.setBottom(writeMessage);




        sendButton = new Button("Send");
        sendButton.setMaxWidth(Double.MAX_VALUE);
        //sendButton.setMinWidth(chatGrid.getMinWidth());

        sendButton.setPrefWidth(chatGrid.getMaxWidth());

        writeMessage.setOnKeyPressed((keyEvent -> {if(keyEvent.getCode() == KeyCode.ENTER){
            sendMessage();
        }
        }));

        sendButton.setOnAction(e -> {
            sendMessage();
        });

        Text recTest = new Text( "Testing "+"<");


        HBox.setHgrow(writeMessage, Priority.ALWAYS);//Added this line
        HBox.setHgrow(sendButton, Priority.ALWAYS);//Added this line
        lowerHBox.getChildren().addAll(writeMessage,sendButton);
        //conversationGrid.getChildren().add(textDispVBox);
        //HBox.setHgrow(textDispVBox, Priority.ALWAYS);
        //HBox.setHgrow(recDispVBox, Priority.ALWAYS);
        //textHBox.getChildren().addAll(textDispVBox,recDispVBox); //Adding sent text and recieved in HBox
        recDispVBox.setAlignment(Pos.TOP_CENTER);

        BorderPane border = new BorderPane();
        border.setRight(chatGrid);

        border.setBottom(lowerHBox);
        border.setLeft(textDispVBox);
        border.setCenter(recDispVBox);
        //border.setLeft(textHBox);
        //border.setLeft(conversationGrid);
        //border.setBottom(writeMessage);
        //border.setCenter(sendButton);
        recDispVBox.getChildren().add(recTest);

        Thread recMsg = new Thread(() -> {
            Runnable updater = () -> {

                if (client.newReceive) {
                    System.out.println("Jag har fått ett meddelande " + client.received);
                    Text t = new Text(client.received);
                    recDispVBox.getChildren().add(t);
                    textDispVBox.getChildren().add(new Text("")); //Add empty row on other side
                    client.newReceive = false;

                }

            };

            while(true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(updater);
            }
        });
        recMsg.setDaemon(true);
        recMsg.start();




        /* Thread recMsg = new Thread(()->{
           while (true){
               try {
                   Thread.sleep(100);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
              // System.out.println("client.newRecieve: "+ client.newRecieve);
               if(client.newRecieve==true){
                   System.out.println("Jag har fått ett meddelande");
                   Text t= new Text(client.recieved);
                   recDispVBox.getChildren().add(t);
                   textDispVBox.getChildren().add(new Text("")); //Add empty row on other side
                   client.newRecieve=false;
               }
           }
        });

        recMsg.start();*/
        Scene scene = new Scene(border,700,500);
        primaryStage.setScene(scene);
        primaryStage.show();



    }
    public void sendMessage(){
        Text sent = new Text("> " + writeMessage.getText());
        textDispVBox.getChildren().add(sent);
        recDispVBox.getChildren().add(new Text ("")); //Add empty row on the other side
        client.setForMessage(writeMessage.getText());
        writeMessage.clear();
    }
}

