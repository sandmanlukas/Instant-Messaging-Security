package frontend.GUI;

import Controller.Controller;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class GUIMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        Controller cont = new Controller();

        primaryStage.setTitle("Main page");

        StackPane startPane = new StackPane();

        TextField username = new TextField();
        TextField password = new TextField();

        username.setMaxSize(100,10);
        username.setText("username");
        username.setTranslateX(0.0);
        username.setTranslateY(0.0);

        password.setMaxSize(100,10);
        password.setText("password");
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

        login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                CharSequence userInput;
                CharSequence passInput;
                userInput = username.getCharacters();
                passInput = password.getCharacters();

                //sends login input to Controller
                cont.login(userInput, passInput);

                //TODO open new page if input successful
                mainChatPage(primaryStage);

            }
        });

    }

    //The main page when logged in, where you can chat
    //Panes colored only to make everything visible
    public void mainChatPage(Stage primaryStage){

        //list of active chats, or where to create new conversations
        GridPane chattGrid = new GridPane();
        chattGrid.setScaleX(200);
        chattGrid.setStyle("-fx-border-color: blue;");

        //shows conversations one at the time
        BorderPane conversationGrid = new BorderPane();
        conversationGrid.setScaleX(500);
        conversationGrid.setStyle("-fx-border-color: #A9A9A9;");


        //trying to add text field to write messages at the bottom of the left pane.....

        HBox lowerHBox = new HBox();
        VBox textDispVBox =new VBox();
        VBox chatDisplay = new VBox();


        TextField writeMessage = new TextField();
        writeMessage.setMaxSize(500,100);
        writeMessage.setPromptText("Write message here");
        //conversationGrid.setBottom(writeMessage);

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> {
            //System.out.println(writeMessage.getText());
            Text sent = new Text("> " + writeMessage.getText());
            textDispVBox.getChildren().add(sent);
            writeMessage.clear();
        });
        lowerHBox.setHgrow(writeMessage, Priority.ALWAYS);//Added this line
        lowerHBox.setHgrow(sendButton, Priority.ALWAYS);//Added this line
        lowerHBox.getChildren().addAll(writeMessage,sendButton);
        conversationGrid.getChildren().add(textDispVBox);

        BorderPane border = new BorderPane();
        border.setRight(chattGrid);

        border.setBottom(lowerHBox);
        border.setLeft(textDispVBox);
        //border.setLeft(conversationGrid);
        //border.setBottom(writeMessage);
        //border.setCenter(sendButton);

        primaryStage.setScene((new Scene(border, 700, 500)));
        primaryStage.show();




    }
}

    /*public void mainChatPage(Stage primaryStage){
    //list of active chats, or where to create new conversations
    GridPane chattGrid = new GridPane();
        chattGrid.setScaleX(200);
                chattGrid.setStyle("-fx-border-color: blue;");

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
                border.setRight(chattGrid);
                border.setLeft(conversationGrid);

                primaryStage.setScene((new Scene(border, 700, 500)));
                primaryStage.show();

                }
                }*/