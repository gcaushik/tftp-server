package com.ua;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Response {

    public static void respondWithErrorPacket(byte[] opErrCode, byte[] errMsg, DatagramSocket socket) throws IOException {
        byte[] nullChar = {0x00};
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(opErrCode);
        outputStream.write(errMsg);
        outputStream.write(nullChar);
        byte[] errBuffer = outputStream.toByteArray();
        DatagramPacket errorPacket = new DatagramPacket(errBuffer, errBuffer.length);
        socket.send(errorPacket);
    }

    public static void respondWithAckPacket(byte[] ackBuffer, DatagramSocket socket) throws IOException {
        DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);
        socket.send(ackPacket);
    }
}
