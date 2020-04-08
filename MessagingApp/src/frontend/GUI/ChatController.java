import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
            tab.setClosable(true);
            tab.setContent(FXMLLoader.load(GUIChat.class.getResource("Tabs.fxml")));
            Platform.runLater(() -> {
                tabPane.getTabs().add(tab);
                openTabs.put(tabName, tab);
                tab.setOnClosed(e -> openTabs.remove(tabName));
                selectionTab = tabPane.getSelectionModel();
                selectionTab.select(tab);
                activeTab = selectionTab.getSelectedItem();
                tabVBox.getChildren().add(new Text("test test test"));

            });
        }
    }



    
    public void sendMessage(){
        //activeTab.setContent(rightVBox);
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
    public void tabListener(){
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observableValue, Tab oldTab, Tab newTab) {
                newTab.setContent(oldTab.getContent());
            }
        });
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
