package com.ua;

import java.net.SocketException;

public class Main {

    public static void main(String[] args) throws SocketException {
        WRQListener server = new WRQListener();
        Thread master = new Thread(server);
        master.start();
        System.out.println("TFTP Server started...");
    }
}
