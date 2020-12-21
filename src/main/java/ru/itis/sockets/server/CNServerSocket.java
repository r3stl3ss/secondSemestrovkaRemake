package ru.itis.sockets.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class CNServerSocket {
    private ServerSocket socket;
    private List<ClientRunnable> clients;
    private List<PrintWriter> broadcast;

    public void start(int port) {

        try {
            socket = new ServerSocket(port);
            Socket firstMaster = socket.accept();

            clients = new ArrayList<>();
            broadcast = new ArrayList<>();

            BufferedReader firstMasterReader = new BufferedReader(new InputStreamReader(firstMaster.getInputStream()));
            PrintWriter firstMasterWriter = new PrintWriter(firstMaster.getOutputStream(), true);
            firstMasterWriter.println("1");

            String[] initialPrefs = firstMasterReader.readLine().split(",");
            Integer numberOfPlayers = Integer.parseInt(initialPrefs[1]);
            CountDownLatch latch = new CountDownLatch(numberOfPlayers - 1);

            ClientRunnable master1 = new ClientRunnable(initialPrefs[0] + "[Red Master]", "Red Master",
                    firstMasterReader, firstMasterWriter, latch);
            firstMasterWriter.println("Red Master");

            Socket secondMaster = socket.accept();
            BufferedReader secondMasterReader = new BufferedReader(new InputStreamReader(secondMaster.getInputStream()));
            PrintWriter secondMasterWriter = new PrintWriter(secondMaster.getOutputStream(), true);
            secondMasterWriter.println("!");
            ClientRunnable master2 = new ClientRunnable("2", "Blue Master",
                    secondMasterReader, secondMasterWriter, latch);

            clients.add(master1); clients.add(master2);

            broadcast.add(firstMasterWriter); broadcast.add(secondMasterWriter);

            for (int i = 2; i < numberOfPlayers; i++) {
                Socket player = socket.accept();
                BufferedReader newReader = new BufferedReader(new InputStreamReader(player.getInputStream()));
                PrintWriter newWriter = new PrintWriter(player.getOutputStream(), true);
                broadcast.add(newWriter);

                String team = i % 2 == 0 ? "Red" : "Blue";
                ClientRunnable playerRunnable = new ClientRunnable(String.valueOf(i), team,
                        newReader, newWriter, latch);

                clients.add(playerRunnable);
                newWriter.println("!");
            }
            for (ClientRunnable client : clients) {
                client.setBroadcast(broadcast);
            }

            System.out.println("Ready!");
            try {
                latch.await();
            } catch (InterruptedException e) {
            }
            System.out.println("Starting game");
            String names = clients.stream().map(x -> x.getName()).reduce((x,y) -> x + "@" + y).orElse("null");
            for (PrintWriter player : broadcast) {
                player.println("names@" + names);
            }

            LinkedList<String> allWords = new LinkedList<>();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(CNServerSocket.class.getResourceAsStream("/txt/allwords.txt")));
            String word = br.readLine();
            while (word != null) {
                allWords.add(word);
                word = br.readLine();
            }

            List<String> colors = new ArrayList<>();
            BufferedReader brColors = new BufferedReader(
                    new InputStreamReader(CNServerSocket.class.getResourceAsStream("/txt/allcolors.txt")));
            String color = brColors.readLine();
            while (color != null) {
                colors.add(color);
                color = brColors.readLine();
            }

            Collections.shuffle(colors);
            Collections.shuffle(allWords);

            String cards = "cards@";
            for (String newColor : colors) {
                cards += allWords.poll() + "," + newColor + "@";
            }
            for (PrintWriter player : broadcast) {
                player.println(cards);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
