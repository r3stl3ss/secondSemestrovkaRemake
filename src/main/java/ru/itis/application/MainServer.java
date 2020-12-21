package ru.itis.application;

import ru.itis.sockets.server.CNServerSocket;

public class MainServer {
    public static void main(String[] args) {
        CNServerSocket serverSocket = new CNServerSocket();
        serverSocket.start(7777);
    }
}
