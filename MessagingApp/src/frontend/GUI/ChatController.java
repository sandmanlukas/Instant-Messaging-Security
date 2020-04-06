import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.io.IOException;

public class ChatController {
    @FXML
    private TextField text;
    @FXML
    private VBox rightVBox;
    @FXML
    private VBox tabVBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TabPane tabPane;

    private static Client controllerClient;
    private SelectionModel<Tab> selectionTab;
    private Tab activeTab;
    private SelectionModel<Tab> groupSelectionTab;

    public ChatController(){

    }

    public static void logout(){
        controllerClient.logOut = true;
        Platform.exit();
        System.exit(0);
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
