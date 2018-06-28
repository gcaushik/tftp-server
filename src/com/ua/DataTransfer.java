package com.ua;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DataTransfer implements Runnable {
    private static final int MAX_BUFFER_SIZE = 516;
    private static final int DATA = 3;
    private static final int DATA_OFFSET = 4;
    private static final byte[] WRITING_TO_FILE_ERROR = {0x00,0x05,0x00,0x00};
    private static final byte[] WRITING_TO_FILE_ERROR_MESSAGE = new String("Error writing to file, closing connection").getBytes(StandardCharsets.UTF_8);
    private static final byte[] INVALID_OPERATION_ERROR = {0x00,0x05,0x00,0x00};
    private static final byte[] INVALID_OPERATION_ERROR_MESSAGE = new String("Invalid operation").getBytes(StandardCharsets.UTF_8);
    private byte[] buffer;
    private DatagramSocket socket;
    private boolean running;
    private String filename;
    private int blockNumberCount = 0;

    DataTransfer(int port, String filename) throws SocketException {
        buffer = new byte[MAX_BUFFER_SIZE];
        socket = new DatagramSocket(port);
        running = true;
        this.filename = filename;
    }

    @Override
    public void run() {
        try {
            listenForData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForData() throws IOException {
        while(running) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            ByteBuffer writeRequest = ByteBuffer.wrap(buffer);
            short opcode = writeRequest.getShort();
            short blockNumber = writeRequest.getShort();

            if (opcode == DATA) {
                blockNumberCount++;
                if (blockNumber == blockNumberCount) {
                    FileOutputStream fileOutputStream = new FileOutputStream(filename);
                    try {
                        fileOutputStream.write(buffer,DATA_OFFSET,packet.getLength()-DATA_OFFSET);
                    } catch(Exception e) {
                        Response.respondWithErrorPacket(WRITING_TO_FILE_ERROR, WRITING_TO_FILE_ERROR_MESSAGE, socket);
                        fileOutputStream.close();
                        break;
                    }
                    fileOutputStream.close();
                }
                byte[] ackBuffer = {0x00,0x04,writeRequest.get(),writeRequest.get()};
                Response.respondWithAckPacket(ackBuffer, socket);
                if (packet.getLength() < MAX_BUFFER_SIZE) {
                    break;
                }
            }
            else {
                Response.respondWithErrorPacket(INVALID_OPERATION_ERROR, INVALID_OPERATION_ERROR_MESSAGE, socket);
            }

        }

    }

}
