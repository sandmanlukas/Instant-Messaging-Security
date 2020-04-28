import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.*;

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
    private TabPane tabPane;
    @FXML
    private AnchorPane anchorTab;

    private final Map<String, Tab> openTabs = new HashMap<>();
    private final Map<String, Tab> groupTabs = new HashMap<>();
    private final Map<String, List<Label>> userLabels = new HashMap<>();
    private static Client controllerClient;
    private testClient testClient;
    private SelectionModel<Tab> selectionTab;
    public Tab activeTab;
    private Label member;

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

    public void setTestClient (testClient testClient){this.testClient = testClient;}



    //TODO: checks so that a message is valid before opening a new tab
    //TODO: maybe add to that \m isn't necessary when in a tab
    //TODO: what should happen in main tab?
    public void openGroupTab(String sender, String groupName, String message) throws IOException {
        if (groupTabs.containsKey(groupName)){

            tabPane.getSelectionModel().select(groupTabs.get(groupName));
            activeTab = tabPane.getSelectionModel().getSelectedItem();


            activeTab.setOnClosed(e -> closeTab(groupName));
            Platform.runLater(() -> {
                if (message != null){
                    groupTabVBox.getChildren().add(createLabel(message,sender));
                    testClient.setCurrentGroup(groupName);
                }

                if (!userLabels.get(groupName).contains(getLabel(sender + "Label"))){
                    member = new Label(sender);
                    //Sets the id of the label.
                    member.setId(sender + "Label");
                    userLabels.get(groupName).add(member);

                    memberVBox.getChildren().add(member);
                }
            });


        }else {
            Tab groupTab = new Tab(groupName);
            groupTabs.put(groupName, groupTab);
            anchorTab = FXMLLoader.load(getClass().getResource("GroupTabs.fxml"));
            groupTabVBox = (VBox) anchorTab.getChildren().get(0);
            memberVBox = (VBox) anchorTab.getChildren().get(1);
            Label newMember = new Label(sender);
            // Sets the id of the label to easily access it later.
            newMember.setId(sender + "Label");
            //Creates an array to handle group members and later use to check so no person is added as a label more than once
            ArrayList<Label> listOfGroupMembers = new ArrayList<>();
            listOfGroupMembers.add(newMember);
            userLabels.put(groupName,listOfGroupMembers);
            memberVBox.getChildren().add(newMember);
            groupTab.setClosable(true);
            groupTab.setContent(anchorTab);

            groupTab.setOnClosed(e -> closeTab(groupName));


            Platform.runLater(() -> {
                tabPane.getTabs().add(groupTab);
                selectionTab = tabPane.getSelectionModel();
                selectionTab.select(groupTab);
                testClient.setCurrentGroup(groupName);



            });
        }
    }

    private void closeTab(String groupName) {
            groupTabs.remove(groupName);
            userLabels.remove(groupName);
            for (Iterator<String> groupMembers = testClient.getGroupMembers(groupName).iterator(); groupMembers.hasNext();) {
                String groupMember = groupMembers.next();
                if (!groupMember.equals(controllerClient.username)) {
                    Message msg = new Message(controllerClient.username, groupMember, "removeUser", groupName);
                    try {
                        controllerClient.objectOutput.writeObject(msg);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                }
            }
            testClient.removeGroup(groupName);
    }

    public void openTab(String sender, String tabName, String message) throws IOException {
        if (openTabs.containsKey(tabName)){
            tabPane.getSelectionModel().select(openTabs.get(tabName));

            Platform.runLater(() -> tabVBox.getChildren().add(createLabel(message,sender)));

        }else {
            Tab tab = new Tab(tabName);
            openTabs.put(tabName, tab);
            anchorTab = FXMLLoader.load(getClass().getResource("Tabs.fxml"));
            tabVBox = (VBox) anchorTab.getChildren().get(0);
            tab.setClosable(true);
            tab.setContent(anchorTab);

            Platform.runLater(() -> {
                tabPane.getTabs().add(tab);
                tab.setOnClosed(e -> openTabs.remove(tabName));
                selectionTab = tabPane.getSelectionModel();
                selectionTab.select(tab);
                 tabVBox.getChildren().add(createLabel(message,sender));

            });
        }
    }

    public void removeLabel(String userToRemove, String groupName){
        Label labelToRemove = getLabel(userToRemove + "Label");
        userLabels.get(groupName).remove(labelToRemove);
        Platform.runLater(() -> memberVBox.getChildren().remove(labelToRemove));
    }

    private Label getLabel (String id){
        for (Node node : memberVBox.getChildren()) {
            if (node.getId().equals(id) && node.getId() != null) {
                return (Label) node;
            }
        }
        return null;
    }

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

