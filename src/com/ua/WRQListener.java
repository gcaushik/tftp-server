package com.ua;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

public class WRQListener implements Runnable {
    private static final int MAX_BUFFER_SIZE = 512;
    private static final int PORT = 1025;
    private static final int WRITE_REQUEST_OPERATION = 2;
    private static final int MIN_PORT_NUMBER = 49152;
    private static final int MAX_PORT_NUMBER = 65536;
    private static final byte[] FILENAME_EXISTS_ERROR = {0x00,0x05,0x00,0x06};
    private static final byte[] FILENAME_EXISTS_ERROR_MESSAGE = new String("File with this name already exists").getBytes(StandardCharsets.UTF_8);
    private static final byte[] INVALID_OPERATION_ERROR = {0x00,0x05,0x00,0x00};
    private static final byte[] INVALID_OPERATION_ERROR_MESSAGE = new String("Invalid operation").getBytes(StandardCharsets.UTF_8);
    private byte[] buffer;
    private DatagramSocket socket;
    private boolean running;

    WRQListener() throws SocketException {
        buffer = new byte[MAX_BUFFER_SIZE];
        socket = new DatagramSocket(PORT);
        running = true;
    }

    @Override
    public void run() {
        try {
            listenForWRQ();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            System.out.println("Closing connection...");
        } finally {
            socket.close();
        }
    }

    private void listenForWRQ() throws IOException {
        while(running) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            ByteBuffer writeRequest = ByteBuffer.wrap(buffer);
            short opcode = writeRequest.getShort();
            if (opcode == WRITE_REQUEST_OPERATION) {
                char c;
                StringBuilder filenameBuilder = new StringBuilder();
                c = writeRequest.getChar();
                while (c != 0) {
                    filenameBuilder.append(c);
                    c = writeRequest.getChar();
                }
                String filename = filenameBuilder.toString();
                File file = new File(filename);
                if (file.createNewFile() == false) {
                    Response.respondWithErrorPacket(FILENAME_EXISTS_ERROR, FILENAME_EXISTS_ERROR_MESSAGE, socket);
                }
                else {
                    int ephemeralPort = ThreadLocalRandom.current().nextInt(MIN_PORT_NUMBER,MAX_PORT_NUMBER);
                    byte[] ackBuffer = {0x00,0x04,0x00,0x00};
                    DatagramSocket ackSocket = new DatagramSocket(ephemeralPort);
                    Response.respondWithAckPacket(ackBuffer, ackSocket);
                    try {
                        startDataTransferThread(ephemeralPort,filename);
                    } catch (SocketException e) {
                        System.out.println("Internal server error, ending connection");
                        running = false;
                    }

                }
            }
            else {
                Response.respondWithErrorPacket(INVALID_OPERATION_ERROR, INVALID_OPERATION_ERROR_MESSAGE, socket);
            }

        }

    }

    private void startDataTransferThread(int ephemeralPort, String filename) throws SocketException {
        DataTransfer dataTransfer = new DataTransfer(ephemeralPort,filename);
        Thread worker = new Thread(dataTransfer);
        worker.start();
    }
}
