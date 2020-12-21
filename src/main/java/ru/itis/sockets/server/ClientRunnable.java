package ru.itis.sockets.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ClientRunnable implements Runnable {
    private Thread t;
    private String team;
    private BufferedReader from;
    private PrintWriter self;
    private List<PrintWriter> broadcast;
    private CountDownLatch latch;

    ClientRunnable(String name, String team, BufferedReader from, PrintWriter self, CountDownLatch latch) {
        t = new Thread(this, name);
        this.team = team;
        this.from = from;
        this.self = self;
        this.latch = latch;
        t.start();
    }

    @Override
    public void run() {
        try {
            System.out.println(t.getName() + " connected and running");
            while (true) {
                String message = from.readLine();
                if (message != null) {
                    System.out.println(t.getName() + " said " + message);
                    if (message.startsWith(":")) {
                        for (PrintWriter player : broadcast) {
                            player.println(t.getName() + message);
                        }
                    } else if (message.startsWith("@")) {
                        message = message.substring(message.indexOf("@") + 1);
                        for (PrintWriter player : broadcast) {
                            player.println("@" + t.getName() + ": " + message);
                        }
                    } else if (message.startsWith("/") || message.startsWith("_")) {
                        for (PrintWriter player : broadcast) {
                            player.println(message);
                        }
                    } else if (message.equals("#")) {
                        t.interrupt();
                    } else {
                        setName(message);
                        self.println(team);
                    }
                }
                Thread.sleep(10);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setBroadcast(List<PrintWriter> broadcast) {
        this.broadcast = broadcast;
    }

    public void setName(String name) {
        latch.countDown();
        t.setName(name + "[" + team + "]");
    }

    public String getName() {
        return t.getName();
    }

}
