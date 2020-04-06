import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
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


public class ChatController {
    @FXML
    private TextField text;
    @FXML
    private VBox rightVBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TabPane tabPane;
    @FXML
    ChatController chatController;
    private static Client controllerClient;
    private SelectionModel selectionTab;
    private SelectionModel groupSelectionTab;

    public ChatController(){

    }

    public static void logout(){
        controllerClient.logOut = true;
        Platform.exit();
        System.exit(0);
    }
    public TabPane getTabPane(){
        return this.tabPane;
    }
    public void setClient (Client client){
        controllerClient = client;
    }

    public void openTab(String tabName) throws IOException {
        Tab tab = new Tab(tabName);
        tab.setContent(FXMLLoader.load(GUIChat.class.getResource("Tabs.fxml")));
        //tab.setText(tabName);
        //TabPane tabPane = getTabPane();
        Platform.runLater(() -> {
            tabPane.getTabs().add(tab);
            selectionTab = tabPane.getSelectionModel();
            tabPane.setSelectionModel((SingleSelectionModel<Tab>) selectionTab);
            selectionTab.select(tab);
        });

    }



    /*
    public ChatController(String username) throws IOException {
        controllerClient = new Client(username);
        controllerClient.run();
        controllerClient.clientController = chatController;
    }


     */

    public void sendMessage(){
        SingleSelectionModel<Tab> activeTabSelection = tabPane.getSelectionModel();
        Tab activeTab = activeTabSelection.getSelectedItem();
        activeTab.setText("test");
        Text sent = new Text("[You]: " + text.getText());
       // Label msg = new Label(text.getText() + " <");
        //msg.setWrapText(true);
        rightVBox.getChildren().add(sent);
        controllerClient.setForMessage(text.getText());
        text.clear();
    }

    public void recieveMessage(){
        //tabPane.setSelectionModel((SingleSelectionModel<Tab>) selectionTab);
        Text t = new Text(controllerClient.received);
        //Label msgReceived = new Label("> " + t.getText());
        //msgReceived.setWrapText(true);
        rightVBox.getChildren().add(t);
        controllerClient.newReceive = false;
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
    public void writeMessage() {
        if(text.getText().startsWith("\\clear")){
            rightVBox.getChildren().clear();
            text.clear();
        }else{
            sendMessage();
        }
    }


    public void initialize() {
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
