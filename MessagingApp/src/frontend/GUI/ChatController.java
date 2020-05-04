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

    // Method to close the application and tell the client a certain user has logged out.
    public static void logout(){
        controllerClient.logOut = true;
        Platform.exit();
        System.exit(0);
    }

    public void setClient (Client client){
        controllerClient = client;
    }

    public void setTestClient (testClient testClient){this.testClient = testClient;}


    //Method that opens a new tab whenever a group is created.
    public void openGroupTab(String sender, String groupName, String message) throws IOException {
        // Checks if a tab with that groupname is already created on a users application.
        if (groupTabs.containsKey(groupName)){

            //If it exists, select it.
            tabPane.getSelectionModel().select(groupTabs.get(groupName));
            activeTab = tabPane.getSelectionModel().getSelectedItem();

            //Handles whenever a tab is closed.
            activeTab.setOnClosed(e -> closeTab(groupName));

            //Ran into some threading issues with out runLater()
            Platform.runLater(() -> {
                // If no message is recieved, a label with the senders username is added to the tab.
                if (message != null){
                    groupTabVBox.getChildren().add(createLabel(message,sender));
                    //Sets the current group of the user.
                    testClient.setCurrentGroup(groupName);
                }
                //If a label with the username doesn't exist, one is created and added.
                if (!userLabels.get(groupName).contains(getLabel(sender + "Label"))){
                    member = new Label(sender);
                    //Sets the id of the label.
                    member.setId(sender + "Label");
                    userLabels.get(groupName).add(member);
                    memberVBox.getChildren().add(member);
                }
            });

            // If a tab doesn't exists, one is created.
        }else {
            Tab groupTab = new Tab(groupName);
            groupTabs.put(groupName, groupTab);
            //Loads the fxml file of the grouptab.
            anchorTab = FXMLLoader.load(getClass().getResource("GroupTabs.fxml"));
            groupTabVBox = (VBox) anchorTab.getChildren().get(0);
            memberVBox = (VBox) anchorTab.getChildren().get(1);
            // Creates a label of the username
            Label newMember = new Label(sender);
            // Sets the id of the label to easily access it later.
            newMember.setId(sender + "Label");
            //Creates an array to handle group members and later use to check so no person is added as a label more than once
            ArrayList<Label> listOfGroupMembers = new ArrayList<>();
            listOfGroupMembers.add(newMember);
            //Each group and its members are stored in a hashmap
            userLabels.put(groupName,listOfGroupMembers);
            memberVBox.getChildren().add(newMember);
            groupTab.setClosable(true);
            groupTab.setContent(anchorTab);

            //Handles whenever a tab is closed.
            groupTab.setOnClosed(e -> closeTab(groupName));


            Platform.runLater(() -> {
                //Get the current tab and select it.
                tabPane.getTabs().add(groupTab);
                selectionTab = tabPane.getSelectionModel();
                selectionTab.select(groupTab);
                testClient.setCurrentGroup(groupName);



            });
        }
    }
    //Method that is called whenever a group tab is closed
    private void closeTab(String groupName) {
            //Removes the group from the maps of groups and labels.
            groupTabs.remove(groupName);
            userLabels.remove(groupName);
            //Iterates over all other group members and sends them a message telling them to remove the user from the group.
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
            //Removes the group from the client.
            testClient.removeGroup(groupName);
    }
    //Method that handles opening of a regular message tab.
    public void openTab(String sender, String tabName, String message) throws IOException {
        // If it already exists, select it.
        if (openTabs.containsKey(tabName)){
            tabPane.getSelectionModel().select(openTabs.get(tabName));


            Platform.runLater(() -> tabVBox.getChildren().add(createLabel(message,sender)));

        }else {
            Tab tab = new Tab(tabName);
            openTabs.put(tabName, tab);
            //Load the fxml file
            anchorTab = FXMLLoader.load(getClass().getResource("Tabs.fxml"));
            tabVBox = (VBox) anchorTab.getChildren().get(0);
            tab.setClosable(true);
            tab.setContent(anchorTab);

            Platform.runLater(() -> {
                tabPane.getTabs().add(tab);
                //If closed, remove the tab from the map
                tab.setOnClosed(e -> openTabs.remove(tabName));
                selectionTab = tabPane.getSelectionModel();
                selectionTab.select(tab);
                 tabVBox.getChildren().add(createLabel(message,sender));

            });
        }
    }
    //Removes a specific label from a group tab
    public void removeLabel(String userToRemove, String groupName){
        Label labelToRemove = getLabel(userToRemove + "Label");
        userLabels.get(groupName).remove(labelToRemove);
        Platform.runLater(() -> memberVBox.getChildren().remove(labelToRemove));
    }

    //Helper method to get a certain label.
    private Label getLabel (String id){
        for (Node node : memberVBox.getChildren()) {
            if (node.getId().equals(id) && node.getId() != null) {
                return (Label) node;
            }
        }
        return null;
    }

    //Method to create messages as labels
    private Label createLabel(String message, String sender){
        Label msg = new Label("[" + sender + "]: " + message);
        msg.setWrapText(true);
        if (sender.equals(controllerClient.username)){
            msg.setText("[You]: " + message);
            return msg;
        }
        else if (controllerClient.currentGroupName != null){
            msg.setText("[" + sender +"]: " + message);
            return msg;
        }
        return msg;
    }

    //Tells the client that a message should be sent and clears the textfield.
    public void sendMessage(){
        controllerClient.setForMessage(text.getText());
        text.clear();
    }

    //Method that creates a label of client messages, often system messages.
    public void recieveMessage(){
        Label msgRecieved = new Label(controllerClient.received);
        msgRecieved.setWrapText(true);
        rightVBox.getChildren().add(msgRecieved);
        controllerClient.newReceive = false;
    }

    //Event handler that sends a message when enter is pressed.
    //If the text starts with \clear, all text is cleared.
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

    //Sends a message when the send button is pressed.
    @FXML
    public void writeMessage() {
        if(text.getText().startsWith("\\clear")){
            rightVBox.getChildren().clear();
            text.clear();
        }else{
            sendMessage();
        }
    }

    //Initializes the controller and starts a new thread that reads the recieved messages from the client.
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //TODO: add a receiveMessage() method that adds a label to the current tab with newRecieve


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

