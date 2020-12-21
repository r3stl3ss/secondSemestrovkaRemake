package ru.itis.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import ru.itis.sockets.ClientSocket;
import ru.itis.sockets.ReceiveMessageTask;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameController implements Initializable {

    private ClientSocket client;
    private ExecutorService service;
    private Boolean isMaster;
    private Boolean isRed;
    private int turnTracker;
    private int redScore;
    private int blueScore;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private GridPane chatGrid;

    @FXML
    private Text chatText;

    @FXML
    private TextField messageInput;

    @FXML
    private TextField numberInput;

    @FXML
    private Button sendButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Text redTeamlist;

    @FXML
    private Text blueTeamlist;

    @FXML
    private GridPane cardsGrid;

    @FXML
    private Button endButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        redScore = 0;
        blueScore = 0;
        turnTracker = 0;
        isRed = true;
        isMaster = false;

        client = new ClientSocket("localhost", 7777);

        ReceiveMessageTask receiveMessageTask = new ReceiveMessageTask(client.getFromServer(), this);
        service = Executors.newFixedThreadPool(1);
        service.execute(receiveMessageTask);

        scrollPane.vvalueProperty().bind(chatGrid.minHeightProperty());
        endButton.setOnAction(event -> client.sendEmpty());
    }

    public void updateChat(String message) {
        chatText.setText(chatText.getText() + "\n" + message);
        chatGrid.setMinHeight(chatText.getLayoutBounds().getHeight());
    }

    public void setLobby() {
        numberInput.setVisible(true);
        numberInput.setDisable(false);
        sendButton.setOnAction(event -> client.sendInitialPrefs(messageInput.getText(), numberInput.getText()));
    }

    public void setMaster(Boolean isRed) {
        this.isRed = isRed;
        isMaster = true;
        numberInput.setVisible(true);
        numberInput.setDisable(false);
        sendButton.setDisable(true);
        sendButton.setOnAction(event -> client.sendHint(messageInput.getText(), numberInput.getText()));
    }

    public void setPlayer() {
        sendButton.setOnAction(event -> client.sendName(messageInput.getText()));
    }

    public void setTeam(Boolean isRed) {
        this.isRed = isRed;
        sendButton.setDisable(true);
        sendButton.setOnAction(event -> client.sendMessage(messageInput.getText()));
    }

    public void writeNames(String redTeam, String blueTeam) {
        redTeamlist.setText(redTeamlist.getText() + "\n" + redTeam);
        blueTeamlist.setText(blueTeamlist.getText() + "\n" + blueTeam);
    }

    public void startGame(String[] cards) {
        for (int i = 0; i < 5; i++) {
            for (int y = 1; y <= 5; y++) {
                String[] card = cards[i*5 + y].split(",");
                Button cardButton = new Button(card[0]);
                cardButton.setPrefSize(110,40);
                cardButton.setId(i*5 + y - 1 + "," + card[1]);
                if (isMaster) {
                    cardButton.setStyle("-fx-border-color: " + card[1]);
                }
                cardButton.setOnAction(event -> client.sendCard(cardButton.getId()));
                cardsGrid.add(cardButton, i, y-1, 1,1);
            }
        }
        switchTurn();
    }

    public void switchTurn() {
        cardsGrid.setDisable(true);
        endButton.setDisable(true);
        endButton.setVisible(false);
        turnTracker++;
        if (turnTracker % 4 == 1) {
            masterTurn(true);
        } else if (turnTracker % 4 == 2) {
            teamTurn(true);
        } else if (turnTracker % 4 == 3) {
            masterTurn(false);
        } else {
            teamTurn(false);
        }
    }

    private void masterTurn(Boolean isRed) {
        String status = "Waiting for ";
        status += isRed ? "red " : "blue ";
        status += "master to give a hint";
        statusLabel.setText(status);
        if (isMaster && this.isRed == isRed) {
            sendButton.setDisable(false);
        } else {
            sendButton.setDisable(true);
        }
    }

    private void teamTurn(Boolean isRed) {
        String status = isRed ? "Red " : "Blue ";
        status += "team's turn";
        statusLabel.setText(status);
        if (!isMaster) {
            sendButton.setDisable(false);
            if (this.isRed == isRed) {
                cardsGrid.setDisable(false);
                endButton.setDisable(false);
                endButton.setVisible(true);
            }
        } else {
            sendButton.setDisable(true);
        }
    }

    public void flipCard(String cardId) {
        String[] card = cardId.split(",");
        Button cardButton = (Button)cardsGrid.getChildren().get(Integer.parseInt(card[0]));
        cardButton.setStyle("-fx-background-color: " + card[1]);
        Boolean isRedTurn = turnTracker % 4 == 2;
        if (card[1].equals("white")) {
            switchTurn();
        } else if (card[1].equals("black")) {
            endGame(!isRedTurn);
        } else if (card[1].equals("red")) {
            redScore++;
            if (redScore == 9) endGame(true);
            else if (!isRedTurn) switchTurn();
        } else {
            blueScore++;
            if (blueScore == 8) endGame(false);
            else if (isRedTurn) switchTurn();
        }
    }

    private void endGame(Boolean isRed) {
        cardsGrid.setDisable(true);
        endButton.setDisable(true);
        endButton.setVisible(false);
        String status = "team won!";
        if (isRed) {
            status = "Red " + status;
            statusLabel.setTextFill(Paint.valueOf("red"));
        } else {
            status = "Blue " + status;
            statusLabel.setTextFill(Paint.valueOf("blue"));
        }
        statusLabel.setText(status);
        service.shutdownNow();
        client.close();
    }

}
