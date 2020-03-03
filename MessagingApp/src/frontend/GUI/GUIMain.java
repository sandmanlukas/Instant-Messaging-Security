package frontend.GUI;

import Controller.Controller;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
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
                mainChattPage(primaryStage);

            }
        });

    }

    public void mainChattPage(Stage primaryStage){

        GridPane chattGrid = new GridPane();

        primaryStage.setScene((new Scene(chattGrid, 700, 500)));
        primaryStage.show();

    }
}