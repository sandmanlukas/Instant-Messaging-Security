import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class GUIChat extends GUIMain {

    @FXML
    private Button sendButton;
    @FXML
    private Stage primaryStage;
    @FXML
    private AnchorPane chatPane;
    @FXML
    private Tab mainTab;
    @FXML
    private VBox rightVBox;


    private VBox textDispVBox;
    private VBox recDispVBox;
    @FXML
    private TextField writeMessage;
    private Client client;
    private static Stage chatStage;
    Scene scene;


    public void sendMessage(){
        Text sent = new Text("> " + writeMessage.getText());
        textDispVBox.getChildren().add(sent);
        recDispVBox.getChildren().add(new Text ("")); //Add empty row on the other side
        client.setForMessage(writeMessage.getText());
        writeMessage.clear();
    }

    public static void setStage(Stage primaryStage)  {
        chatStage = primaryStage;
    }



    public GUIChat(String username) throws IOException {
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("GUIChat.fxml"));
        //loader.setController(new ChatController());
        //AnchorPane pane = FXMLLoader.load(getClass().getResource("GUIChat.fxml"));
        //chatPane.getChildren().setAll(pane);

        //ScrollPane scrollPane = new ScrollPane();
        //scrollPane.setContent(pane);
        //mainTab.setContent(scrollPane);

        new ChatController(username, chatStage);


        // root  = loader.load();
        //(ChatController) loader.getController().setPrimaryStage(primaryStage);
//        primaryStage.setScene(new Scene(root));

        //TODO: uncomment

        //client = new Client(username);
        //ChatController.setAndStartClient(client);



        /*
        //list of active chats, or where to create new conversations
        GridPane chatGrid = new GridPane();
        ScrollPane scrollPane = new ScrollPane();

        //chatGrid.getStylesheets().add(getClass().getResource("GUIChat.css").toExternalForm());


        //shows conversations one at the time
        BorderPane conversationGrid = new BorderPane();
        conversationGrid.setScaleX(500);
        conversationGrid.setStyle("-fx-border-color: #A9A9A9;");


        //trying to add text field to write messages at the bottom of the left pane.....

        HBox lowerHBox = new HBox();
        textDispVBox = new VBox(10);
        textDispVBox.getStyleClass().add("GUIChat.css");
        recDispVBox = new VBox(10);
        scrollPane.setContent(textDispVBox);

        writeMessage = new TextField();
        writeMessage.setMaxWidth(Double.MAX_VALUE);
        //writeMessage.setMaxSize(500,100);
        writeMessage.setPromptText("Write message here");
        //conversationGrid.setBottom(writeMessage);


        Button sendButton = new Button("Send");
        sendButton.setMaxWidth(Double.MAX_VALUE);

        sendButton.setPrefWidth(chatGrid.getMaxWidth());


         */
        //TODO: uncomment
        /*

        writeMessage.setOnKeyPressed((keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        }));

        sendButton.setOnAction(e -> sendMessage());

        Text recTest = new Text("Testing " + "<, User: " + username);


         */
        //TODO: uncomment maybe

        /*
        HBox.setHgrow(writeMessage, Priority.ALWAYS);//Added this line
        HBox.setHgrow(sendButton, Priority.ALWAYS);//Added this line
        lowerHBox.getChildren().addAll(writeMessage, sendButton);
        recDispVBox.setAlignment(Pos.TOP_CENTER);

        BorderPane border = new BorderPane();
        border.setRight(chatGrid);

        border.setBottom(lowerHBox);
        border.setLeft(textDispVBox);
        border.setCenter(recDispVBox);
        recDispVBox.getChildren().add(recTest);


         */
        //TODO: uncomment
        /*
        Thread recMsg = new Thread(() -> {
            Runnable updater = () -> {
                if (client.newReceive) {
                    Text t = new Text(client.received);
                    recDispVBox.getChildren().add(t);
                    textDispVBox.getChildren().add(new Text("")); //Add empty row on other side
                    client.newReceive = false;

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
        recMsg.setDaemon(true);
        recMsg.start();



         */
        //TODO: uncomment

        //this.scene = new Scene(border, 700, 500);
        //primaryStage.setScene(this.scene);
        //primaryStage.show();

        //TODO: fix this as well
        chatStage.setOnCloseRequest(e -> client.logOut = true);

    }

}
