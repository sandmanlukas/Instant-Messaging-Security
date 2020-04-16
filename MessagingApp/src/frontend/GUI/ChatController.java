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
    private VBox groupTabVBox;
    @FXML
    private VBox memberVBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private AnchorPane anchorTab;
    private VBox activeVBox;

    private Map<String, Tab> openTabs = new HashMap<>();
    private Map<String, Tab> groupTabs = new HashMap<>();
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

    //TODO: checks so that a message is valid before opening a new tab
    //TODO: maybe add to that \m isn't necessary when in a tab
    //TODO: what should happen in main tab?
    public void openGroupTab(String sender, String groupName, String message) throws IOException {
        if (groupTabs.containsKey(groupName)){
            tabPane.getSelectionModel().select(groupTabs.get(groupName));

            Platform.runLater(() -> {
                if (message != null){
                    groupTabVBox.getChildren().add(createLabel(message,sender));
                }
                //TODO: fix so that it doesn't create a new label if that user is already in group
                Label member = new Label(sender);
                memberVBox.getChildren().add(member);


            });

        }else {
            Tab groupTab = new Tab(groupName);
            groupTabs.put(groupName, groupTab);
            anchorTab = FXMLLoader.load(getClass().getResource("GroupTabs.fxml"));
            groupTabVBox = (VBox) anchorTab.getChildren().get(0);
            memberVBox = (VBox) anchorTab.getChildren().get(1);
            Label newMember = new Label(sender);
            memberVBox.getChildren().add(newMember);
            groupTab.setClosable(true);
            groupTab.setContent(anchorTab);

            Platform.runLater(() -> {
                tabPane.getTabs().add(groupTab);
                //TODO: check so that when a user closes the tab the tab isn't not removed from the hashmap if group.
                //TODO: remove userlabel when tab is closed
                groupTab.setOnClosed(e -> openTabs.remove(groupName));
                selectionTab = tabPane.getSelectionModel();
                selectionTab.select(groupTab);
                //TODO: check this, currently only works the first time

                //groupTabVBox.getChildren().add(createLabel(message,sender));

            });
        }
    }

    public void openTab(String sender, String tabName, String message) throws IOException {
        if (openTabs.containsKey(tabName)){
            tabPane.getSelectionModel().select(openTabs.get(tabName));

            Platform.runLater(() -> {
                tabVBox.getChildren().add(createLabel(message,sender));
            });

        }else {
            Tab tab = new Tab(tabName);
            openTabs.put(tabName, tab);
            anchorTab = FXMLLoader.load(getClass().getResource("Tabs.fxml"));
            tabVBox = (VBox) anchorTab.getChildren().get(0);
            tab.setClosable(true);
            tab.setContent(anchorTab);

            Platform.runLater(() -> {
                tabPane.getTabs().add(tab);
                //TODO: check so that when a user closes the tab the tab isn't not removed from the hashmap if group.
                tab.setOnClosed(e -> openTabs.remove(tabName));
                selectionTab = tabPane.getSelectionModel();
                selectionTab.select(tab);
                //TODO: check this, currently only works the first time

                 tabVBox.getChildren().add(createLabel(message,sender));

            });
        }
    }
    //TODO: could find a better solution
    private Label createLabel(String message, String sender){
        Label msg = new Label("[" +sender + "]: " + message);
        msg.setWrapText(true);
        if (sender.equals(controllerClient.username)){
            msg.setText("[You]: " + message);
            return msg;
        }else if (controllerClient.currentGroupName != null){
            msg.setText("[" + sender +"]: " + message);
            return msg;
        }
        return msg;
    }

    public void sendMessage(){
       // activeTab = tabPane.getSelectionModel().getSelectedItem();
       // activeVBox = (VBox) anchorTab.getChildren().get(0);
        //Text sent = new Text("[You]: " + text.getText());
        //Label msg = new Label("[You]: " + text.getText());
       // msg.setWrapText(true);
       // activeVBox.getChildren().add(msg);
        controllerClient.setForMessage(text.getText());
        text.clear();
    }

    //TODO: still adds stuff in main.
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
        //TODO: add a receiveMessage() method that adds a label to the current tab with newRecieve


        Thread recMessage = new Thread(() -> {
            Runnable updater = () -> {
                if (controllerClient.newReceive) {
                    recieveMessage();
                   // openTab(controllerClient.username,controllerClient.,controllerClient.received);
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

