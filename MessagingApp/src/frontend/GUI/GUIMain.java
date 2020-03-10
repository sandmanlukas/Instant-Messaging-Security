
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
    private PasswordField password;
    private Controller cont;
    private final Stage primaryStage = new Stage();
    private Stage chatStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws SQLException, ClassNotFoundException {

        cont = new Controller();

        primaryStage.setTitle("Main page");

        StackPane startPane = new StackPane();

        username = new TextField();
        password = new PasswordField();

        username.setMaxSize(100,10);
        username.setPromptText("username");
        username.setTranslateX(0.0);
        username.setTranslateY(0.0);

        password.setMaxSize(100,10);
        password.setPromptText("password");
        password.setTranslateX(0.0);
        password.setTranslateY(25.0);

        Button login = new Button("login");
        login.setTranslateX(0.0);
        login.setTranslateY(50.0);

        startPane.getChildren().add(username);
        startPane.getChildren().add(password);
        startPane.getChildren().add(login);


        primaryStage.setScene((new Scene(startPane, 700, 500)));
        primaryStage.show();


        password.setOnKeyPressed((keyEvent -> {if(keyEvent.getCode() == KeyCode.ENTER){
            tryLogin();
        }
        }));

        login.setOnAction(event -> tryLogin());

    }
    public void failLogin(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Wrong Password");
        alert.setHeaderText("Wrong password.");
        alert.setContentText("You entered the wrong password. Try again.");
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
                primaryStage.close();
                mainChatPage(primaryStage, userInput);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            failLogin();
            System.out.println("Login failed, try again.");
        }
    }

    //The main page when logged in, where you can chat
    //Panes colored only to make everything visible
    public void mainChatPage(Stage primaryStage, String username) throws IOException {

        Client client = new Client(username);
        client.run();
        System.out.println(client.username);
        //list of active chats, or where to create new conversations
        GridPane chatGrid = new GridPane();
        chatGrid.setScaleX(200);
        chatGrid.setStyle("-fx-border-color: blue;");

        //shows conversations one at the time
        BorderPane conversationGrid = new BorderPane();
        conversationGrid.setScaleX(500);
        conversationGrid.setStyle("-fx-border-color: #A9A9A9;");


        //trying to add text field to write messages at the bottom of the left pane.....

        HBox lowerHBox = new HBox();
        HBox textHBox = new HBox();
        VBox textDispVBox =new VBox(10);
        VBox recDispVBox = new VBox();
        VBox chatDisplay = new VBox();


        TextField writeMessage = new TextField();
        writeMessage.setMaxSize(500,100);
        writeMessage.setPromptText("Write message here");
        //conversationGrid.setBottom(writeMessage);



        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> {
            Text sent = new Text("> " + writeMessage.getText());
            textDispVBox.getChildren().add(sent);
            recDispVBox.getChildren().add(new Text ("")); //Add empty row on the other side
            client.setForMessage(writeMessage.getText());
            writeMessage.clear();
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

        primaryStage.setScene((new Scene(border, 700, 500)));
        primaryStage.show();



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