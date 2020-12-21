package ru.itis.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Socket;

public class ClientSocket {
    private Socket client;

    private PrintWriter toServer;
    private BufferedReader fromServer;

    public ClientSocket(String host, int port) {
        try {
            client = new Socket(host, port);
            toServer = new PrintWriter(client.getOutputStream(), true);
            fromServer = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void sendMessage(String message) {
        toServer.println(": " + message);
    }

    public void sendInitialPrefs(String message, String number) {
        toServer.println(message + "," + number);
    }

    public void sendHint(String hint, String number) {
        toServer.println("@" + hint + " - " + number);
    }

    public void sendCard(String cardId) {
        toServer.println("_" + cardId);
    }

    public void sendEmpty() {
        toServer.println("/");
    }

    public void sendName(String message) {
        toServer.println(message);
    }

    public BufferedReader getFromServer() {
        return fromServer;
    }

    public void close() {
        try {
            toServer.println("#");
            client.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
