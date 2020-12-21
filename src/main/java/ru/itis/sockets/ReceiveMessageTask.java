package ru.itis.sockets;

import javafx.application.Platform;
import javafx.concurrent.Task;
import ru.itis.controllers.GameController;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;

public class ReceiveMessageTask extends Task<Void> {
    private BufferedReader fromServer;
    private GameController controller;

    public ReceiveMessageTask(BufferedReader fromServer, GameController controller) {
        this.fromServer = fromServer;
        this.controller = controller;
    }

    @Override
    protected Void call() throws Exception {
        while (true) {
            try {
                String messageFromServer = fromServer.readLine();
                System.out.println(messageFromServer);
                if (messageFromServer != null) {
                    if (messageFromServer.equals("1")) {
                        Platform.runLater(() -> controller.setLobby());
                    } else if (messageFromServer.equals("!")) {
                        Platform.runLater(() -> controller.setPlayer());
                    } else if (messageFromServer.startsWith("Red") || messageFromServer.startsWith("Blue")) {
                        if (messageFromServer.equals("Blue Master")) {
                            Platform.runLater(() -> controller.setMaster(false));
                        } else if (messageFromServer.equals("Red Master")) {
                            Platform.runLater(() -> controller.setMaster(true));
                        } else if (messageFromServer.equals("Red")) {
                            Platform.runLater(() -> controller.setTeam(true));
                        } else {
                            Platform.runLater(() -> controller.setTeam(false));
                        }
                    } else if (messageFromServer.startsWith("names")) {
                        String[] teams = messageFromServer.split("@");
                        String redTeam = Arrays.stream(teams).filter(x -> x.contains("Red"))
                                .reduce((x,y) -> x + "\n" + y).orElse("nobody");
                        String blueTeam = Arrays.stream(teams).filter(x -> x.contains("Blue"))
                                .reduce((x,y) -> x + "\n" + y).orElse("nobody");
                        Platform.runLater(() -> controller.writeNames(redTeam, blueTeam));
                    } else if (messageFromServer.startsWith("cards")) {
                        String[] cards = messageFromServer.split("@");
                        Platform.runLater(() -> controller.startGame(cards));
                    } else if (messageFromServer.startsWith("@")) {
                        Platform.runLater(() -> controller.updateChat(
                                messageFromServer.substring(messageFromServer.indexOf("@") + 1)));
                        Platform.runLater(() -> controller.switchTurn());
                    } else if (messageFromServer.startsWith("/")) {
                        Platform.runLater(() -> controller.switchTurn());
                    } else if (messageFromServer.startsWith("_")) {
                        Platform.runLater(() -> controller.flipCard(
                                messageFromServer.substring(messageFromServer.indexOf("_") + 1)));
                    } else {
                        Platform.runLater(() -> controller.updateChat(messageFromServer));
                    }
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
