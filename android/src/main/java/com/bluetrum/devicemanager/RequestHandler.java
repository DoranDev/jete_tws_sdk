package com.bluetrum.devicemanager;

import androidx.annotation.NonNull;

import com.bluetrum.devicemanager.cmd.Request;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

class RequestHandler {

    private static final String TAG = RequestHandler.class.getSimpleName();

    static final int DEFAULT_PAYLOAD_SIZE = 20; // 基础MTU 23字节，除开包头3字节

    private static final int HEAD_SIZE = 5;

    private final RequestSplitter splitter;

    private byte hostSeqNum;

    RequestHandler() {
        this(new RequestSplitter(DEFAULT_PAYLOAD_SIZE - HEAD_SIZE));
    }

    RequestHandler(@NonNull final RequestSplitter splitter) {
        this.hostSeqNum = 0;
        this.splitter = splitter;
    }

    void reset() {
        hostSeqNum = 0;
    }

    void setMaxPacketSize(int maxPacketSize) {
        splitter.setMaxPayloadSize(maxPacketSize - HEAD_SIZE);
    }

    int getMaxPayloadSize() {
        return splitter.getMaxPayloadSize();
    }

    /* Handler */

    synchronized Deque<byte[]> handleRequest(@NonNull Request request) {
        byte[] commandData = request.getPayload();
        int dataLength = commandData.length;

        Deque<byte[]> commandQueue = new LinkedBlockingDeque<>();

        // 先处理不拆包
        if (dataLength == 0) {
            byte byte3 = 0;

            ByteBuffer bb = ByteBuffer.allocate(HEAD_SIZE);
            bb.put(hostSeqNum);
            bb.put(request.getCommand());
            bb.put(request.getCommandType());
            bb.put(byte3);
            bb.put((byte) 0);
            commandQueue.add(bb.array());

            hostSeqNum = (byte) ((hostSeqNum + 1) & 0xF);
        } else {
            int fragNum = splitter.getFragNum(dataLength);
            for (int i = 0; i < fragNum; i++) {
                byte [] payload = splitter.chunk(commandData, i);
                int payloadLength = payload.length;
                byte byte3 = (byte) ((((fragNum - 1) << 4) & 0xF0) | (i & 0xF));

                ByteBuffer bb = ByteBuffer.allocate(HEAD_SIZE + payloadLength);
                bb.put(hostSeqNum);
                bb.put(request.getCommand());
                bb.put(request.getCommandType());
                bb.put(byte3);
                bb.put((byte) payloadLength);
                bb.put(payload);
                commandQueue.add(bb.array());

                hostSeqNum = (byte) ((hostSeqNum + 1) & 0xF);
            }
        }

        return commandQueue;
    }

}
