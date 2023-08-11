package com.bluetrum.devicemanager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.bluetrum.utils.ParserUtils;

import java.nio.ByteBuffer;

class ResponseHandler {

    private static final String TAG = ResponseHandler.class.getSimpleName();

    private static final String THREAD_NAME_HANDLE_FRAME = "THREAD_NAME_HANDLE_FRAME";

    static final int RESPONSE_TYPE_RESPONSE         = 0x00;
    static final int RESPONSE_TYPE_NOTIFICATION     = 0x01;
    static final int RESPONSE_TYPE_ERROR            = 0x02;

    private static final int HEAD_SIZE = 5;

    private final ResponseMerger merger;
    private final Handler payloadHandler;
    private final Handler frameHandler;

    private byte expectedSeqNum = 0;

    ResponseHandler(final Handler payloadHandler) {
        this.merger = new ResponseMerger(payloadHandler);
        this.payloadHandler = payloadHandler;

        HandlerThread handleFrameThread = new HandlerThread(THREAD_NAME_HANDLE_FRAME);
        handleFrameThread.start();
        this.frameHandler = new Handler(handleFrameThread.getLooper());
    }

    void handleFrameData(byte[] frameData) {
        frameHandler.post(() -> {
            ByteBuffer bb = ByteBuffer.wrap(frameData);
            // 检查包结构，按照结构拆包
            // （如果不符合条件，先不记录SeqNum）
            while (true) {
                if (bb.remaining() >= HEAD_SIZE) {
                    byte byte0 = bb.get();
                    byte seqNum = (byte) (byte0 & 0x0F);
                    boolean encrypted = ParserUtils.getBitValue(byte0, 7) == 1;

                    if (seqNum != expectedSeqNum) {
                        Log.w(TAG, "Frame seq mismatch: Expected " + expectedSeqNum + " but got " + seqNum);
                        sendErrorMessage(DeviceCommManager.RESPONSE_ERROR_WRONG_SEQ_NUMBER);
                    }
                    expectedSeqNum = (byte) ((seqNum + 1) & 0xF);

                    byte cmd = bb.get();
                    byte cmdType = bb.get();
                    byte byte3 = bb.get();
                    byte frameSeq = (byte) (byte3 & 0xF);
                    byte totalFrame = (byte) (((byte3 >> 4) & 0xF) + 1);
                    int frameLen = bb.get() & 0xFF;

                    // Check payload length
                    if (bb.remaining() >= frameLen) {
                        byte[] payload = new byte[frameLen];
                        bb.get(payload);
                        merger.merge(cmd, cmdType, payload, totalFrame, frameSeq);
                        // done
                        if (bb.remaining() == 0) {
                            break;
                        }
                    } else {
                        Log.w(TAG, "The length of payload mismatch: Expected " + frameLen + " but got " + bb.remaining());
                        sendErrorMessage(DeviceCommManager.RESPONSE_ERROR_BAD_PAYLOAD_LENGTH);
                        break;
                    }
                } else {
                    Log.w(TAG, "The length of received data is too short, " + bb.remaining());
                    sendErrorMessage(DeviceCommManager.RESPONSE_ERROR_PAYLOAD_TOO_SMALL);
                    break;
                }
            }
        });
    }

    void reset() {
        expectedSeqNum = 0;
        merger.reset();
    }

    /* Message */

    private void sendErrorMessage(final int errorCode) {
        Message message = payloadHandler.obtainMessage(ResponseHandler.RESPONSE_TYPE_ERROR);
        message.arg1 = errorCode;
        payloadHandler.sendMessage(message);
    }

}
