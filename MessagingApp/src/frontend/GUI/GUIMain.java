import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;


public class GUIMain extends Application {
    private TextField username;
    private TextField writeMessage;
    private PasswordField password;
    private Controller cont;
    private Button sendButton;
    private Stage primaryStage;
    private Scene scene;
    private Stage chatStage;
    private VBox textDispVBox;
    private VBox recDispVBox;
    private HBox lowerHBox;
    private Client client;
    private StackPane startPane;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws SQLException, ClassNotFoundException {

        this.primaryStage = primaryStage;
        primaryStage = new Stage();
        cont = new Controller();

        primaryStage.setTitle("Main page");

        startPane = new StackPane();

        username = new TextField();
        password = new PasswordField();

        username.setMaxSize(100,10);
        username.setPromptText("Username");
        username.setTranslateX(0.0);
        username.setTranslateY(0.0);

        password.setMaxSize(100,10);
        password.setPromptText("Password");
        password.setTranslateX(0.0);
        password.setTranslateY(25.0);

        Button login = new Button("Login");
        login.setTranslateX(0.0);
        login.setTranslateY(50.0);

        startPane.getChildren().add(username);
        startPane.getChildren().add(password);
        startPane.getChildren().add(login);


        this.scene = new Scene(startPane, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.show();


        Stage finalPrimaryStage = primaryStage;
        password.setOnKeyPressed((keyEvent -> {if(keyEvent.getCode() == KeyCode.ENTER){
            tryLogin();
            finalPrimaryStage.close();
        }
        }));

        primaryStage.setOnCloseRequest(e -> {
            finalPrimaryStage.close();
        });

        login.setOnAction(event -> {
            tryLogin();
            finalPrimaryStage.close();
        });

    }
    public void failLogin(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Wrong Password or Username");
        alert.setHeaderText("Wrong password or Username.");
        alert.setContentText("You entered the wrong password or Username. Try again.");
        alert.showAndWait();
    }

    public void tryLogin(){
        String userInput;
        String passInput;

        userInput = username.getText();
        passInput = password.getText();

        //sends login input to Controller


        if (cont.login(userInput, passInput)) {
            try {
                mainChatPage(userInput);
                //GUIChat chat = new GUIChat(primaryStage, userInput);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            failLogin();
            System.out.println("Login failed, try again.");
        }
    }

    public void sendMessage(){
        Text sent = new Text("> " + writeMessage.getText());
        textDispVBox.getChildren().add(sent);
        recDispVBox.getChildren().add(new Text ("")); //Add empty row on the other side
        client.setForMessage(writeMessage.getText());
        writeMessage.clear();
    }

    //The main page when logged in, where you can chat
    //Panes colored only to make everything visible
    public void mainChatPage(String username) throws IOException {

        Stage chatStage;
        chatStage = this.primaryStage;

        client = new Client(username);
        client.run();

        chatStage.setTitle("Chat Page");
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

        Text recTest = new Text( "Testing "+"<, User: " + username);


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
        this.scene = new Scene(border,700,500);
        chatStage.setScene(this.scene);
        chatStage.show();

        chatStage.setOnCloseRequest(e -> {
            client.logOut = true;
        });

    }
}

    /*public void mainChatPage(Stage primaryStage){
    //list of active chats, or where to create new conversations
    GridPane chatGrid = new GridPane();
        chatGrid.setScaleX(200);
                chatGrid.setStyle("-fx-border-color: blue;");

                //shows conversations one at the time
                BorderPane conversationGrid = new BorderPane();
                conversationGrid.setScaleX(500);
                conversationGrid.setStyle("-fx-border-color: #A9A9A9;");


                //trying to add text field to write messages at the bottom of the left pane.....
        /*
        TextField writeMessage = new TextField();
        writeMessage.setMaxSize(500,10);
        writeMessage.setText("Write message here");
        conversationGrid.setBottom(writeMessage);
        */
               /* BorderPane border = new BorderPane();
                border.setRight(chatGrid);
                border.setLeft(conversationGrid);

                primaryStage.setScene((new Scene(border, 700, 500)));
                primaryStage.show();

                }
                }*/