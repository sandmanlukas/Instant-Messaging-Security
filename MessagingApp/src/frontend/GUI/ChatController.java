import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
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
    @FXML
    private AnchorPane anchorTab;

    private Map<String, Tab> openTabs = new HashMap<>();
    private static Client controllerClient;
    private SelectionModel<Tab> selectionTab;
    public Tab activeTab;
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
        if (openTabs.containsKey(tabName)){
            tabPane.getSelectionModel().select(openTabs.get(tabName));
        }else {
            Tab tab = new Tab(tabName);
            anchorTab = FXMLLoader.load(getClass().getResource("Tabs.fxml"));
            tabVBox = (VBox) anchorTab.getChildren().get(0);
            tab.setClosable(true);
            tab.setContent(anchorTab);

            Platform.runLater(() -> {
                tabPane.getTabs().add(tab);
                openTabs.put(tabName, tab);
                tab.setOnClosed(e -> openTabs.remove(tabName));

                selectionTab = tabPane.getSelectionModel();
                selectionTab.select(tab);
                //activeTab = selectionTab.getSelectedItem();
                //TODO: check createLabel
                //tabVBox.getChildren().add(createLabel("This is a test"));


            });
        }
    }
    //TODO: could find a better solution
    public Label createLabel(String message){
        Label msg = new Label("[You]: " + message);
        msg.setWrapText(true);
        return msg;
    }

    public void sendMessage(){
        //activeTab.setContent(rightVBox);
        Text sent = new Text("[You]: " + text.getText());
        Label msg = new Label("[You]: " + text.getText());
        msg.setWrapText(true);
        tabVBox.getChildren().add(msg);
        controllerClient.setForMessage(text.getText());
        text.clear();
    }

    public void recieveMessage(){

        Label msgRecieved = new Label(controllerClient.received);
        msgRecieved.setWrapText(true);
        rightVBox.getChildren().add(msgRecieved);
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
/*
        try {
            anchorTab = FXMLLoader.load(getClass().getResource("Tabs.fxml"));
            tabVBox = (VBox) anchorTab.getChildren().get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }


 */


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
