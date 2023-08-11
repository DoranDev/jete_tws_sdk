package com.bluetrum.fota;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluetrum.devicemanager.DeviceCommManager;
import com.bluetrum.devicemanager.cmd.Request;
import com.bluetrum.fota.cmd.request.OtaInfoRequest;
import com.bluetrum.utils.CryptoUtils;

import java.nio.ByteBuffer;

public class OtaManager implements DeviceCommManager.NotificationCallback<Byte> {

    private static final String TAG = OtaManager.class.getSimpleName();

    private static final int UNDEFINED_FIRMWARE_VERSION = 0xFFFFFFFF;

    private static final int DEFAULT_TIMEOUT = 10000; // in ms, 10s
    private final Handler timeoutHandler;
    private final Handler notifyHandler;

    protected OtaDataProvider dataProvider;
    protected OtaRequestGenerator requestGenerator;

    protected int firmwareVersion = UNDEFINED_FIRMWARE_VERSION;
    protected byte[] hashData = new byte[0];
    protected int otaDataSize;

    protected boolean allowedUpdate;
    protected boolean isUpdating;
    protected boolean isUpdatePause;

    private final RequestDelegate requestDelegate;
    private final EventListener eventListener;

    private static final int MAX_RETRY_COUNT = 10;
    private int maxRetryCount = MAX_RETRY_COUNT;
    private int retryCount = 0;

    private RetryCallback retryCallback;

    public OtaManager(@NonNull RequestDelegate requestDelegate,
                      @NonNull EventListener eventListener) {
        this.requestDelegate = requestDelegate;
        this.eventListener = eventListener;

        this.timeoutHandler = new Handler(Looper.getMainLooper());
        this.notifyHandler = new Handler(Looper.getMainLooper());
        this.requestGenerator = new OtaRequestGenerator();
    }

    /**
     *  准备升级信息，需要先使用{@link OtaManager#setOtaData}传入OTA数据
     */
    public void prepareToUpdate() {
        if (dataProvider != null) {
            requireOtaInfo();
        }
    }

    /**
     * 开始OTA升级，内部需要allowUpdate，外部需要确保设备准备就绪（TWS连接等）
     */
    public void startOTA() {
        if (isReadyToUpdate()) {
            isUpdating = true;
            notifyOnStart();
            sendBlock();
        }
    }

    /**
     * {@link OtaManager}是否已经准备好升级
     * @return 是否已经准备好升级
     */
    public boolean isReadyToUpdate() {
        return dataProvider != null && allowedUpdate;
    }

    public void setRetryCallback(RetryCallback retryCallback) {
        this.retryCallback = retryCallback;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    private boolean isRetrying() {
        return retryCount != 0;
    }

    private boolean needRetryOnState(int state) {
        // Retry while in these states
        if (state == OtaError.ERROR_CODE_WRONG_SEQ_NUM
                || state == OtaError.ERROR_CODE_WRONG_DATA_LEN) {
            retryCount++;
            return retryCount <= maxRetryCount;
        }
        return false;
    }

    private void retryUpdate() {
        if (retryCallback != null) {
            retryCallback.beforeRetryCallback();
        }
        // Prepare to update, again
        prepareToUpdate();
        if (retryCallback != null) {
            retryCallback.afterRetryCallback();
        }
    }

    /**
     * 重置{@link OtaManager}，设备断开等情况调用
     */
    public void reset() {
//        firmwareVersion = UNDEFINED_FIRMWARE_VERSION;
//        hashData = new byte[0];
//        otaDataSize = 0;
        allowedUpdate = false;
        isUpdating = false;
        isUpdatePause = false;
        cancelTimeout();
    }

    /* 发送状态 */

    protected void notifyAllowUpdate(boolean allowed) {
        notifyHandler.post(() -> eventListener.onOtaAllowUpdate(allowed));
    }

    protected void notifyOnStart() {
        notifyHandler.post(eventListener::onOtaStart);
    }

    protected void notifyOnProgress(int progress) {
        notifyHandler.post(() -> eventListener.onOtaProgress(progress));
    }

    protected void notifyOnFinish() {
        notifyHandler.post(eventListener::onOtaFinish);
    }

    protected void notifyOnPause() {
        notifyHandler.post(eventListener::onOtaPause);
    }

    protected void notifyOnContinue() {
        notifyHandler.post(eventListener::onOtaContinue);
    }

    protected void notifyOnError(int errorCode) {
        notifyHandler.post(() -> eventListener.onOtaError(errorCode));
    }

    /* 发送指令 */

    protected void requireOtaInfo() {
        Request request = new OtaInfoRequest(firmwareVersion, hashData, otaDataSize);
        sendRequest(request, new DeviceCommManager.RequestCallback() {
            @Override
            public void onComplete(@NonNull Request request, @Nullable Object result) {
                if (result != null) {
                    OtaInfo otaInfo = (OtaInfo) result;
                    int startAddress = otaInfo.getStartAddress();
                    int blockSize = otaInfo.getBlockSize();
                    boolean allowUpdate = otaInfo.isAllowUpdate();

//                    Timber.v("startAddr = %s, blockSize = %s, allowUpdate = %s", startAddress, blockSize, allowUpdate);
                    Log.v(TAG, "startAddr = " + startAddress +", blockSize = " + blockSize +", allowUpdate = " + allowUpdate);

                    dataProvider.setStartAddress(startAddress);
                    dataProvider.setBlockSize(blockSize);
//                    otaManager.setPacketSize(commManager.getMaxPayloadSize()); // todo: app layer

                    allowedUpdate = allowUpdate;

                    // If it's retrying, start updating automatically
                    if (isRetrying()) {
                        startOTA();
                    } else if (isUpdatePause) { // 如果是从暂停中恢复，自动开始升级流程
                        isUpdatePause = false;
                        startOTA();
                    } else {
                        notifyAllowUpdate(allowUpdate);
                    }
                }
            }

            @Override
            public void onTimeout(@NonNull Request request) {
                // todo: notify timeout
            }
        });
    }

    protected void sendOtaStart() {
        Request request = requestGenerator.getOtaStartRequest();
        sendRequest(request);
        // 进度
        notifyOnProgress(dataProvider.getProgress());
    }

    protected void sendOtaData() {
        Request request = requestGenerator.getOtaSendDataRequest();
        sendRequest(request);
        // 进度
        notifyOnProgress(dataProvider.getProgress());
    }

    protected boolean canSendNow() {
        return !dataProvider.isBlockSentFinish();
    }

    protected void sendOtaDataOnce() {
        sendOtaData();
    }

    protected void doSendData() {
        while (canSendNow()) {
            sendOtaDataOnce();
        }
        // 发完块的最后一包后，就等待固件回复
        if (dataProvider != null && dataProvider.isBlockSentFinish()) {
            // 等待固件回复
            timeoutHandler.postDelayed(this::handleTimeout, DEFAULT_TIMEOUT);
        }
    }

    protected void sendBlock() {
        sendOtaStart();
        doSendData();
    }

    private void handleTimeout() {
        // 错误通知
//        notifyOnError(OtaError.TIMEOUT_RECEIVE_RESPONSE);
    }

    private void cancelTimeout() {
        timeoutHandler.removeCallbacksAndMessages(null);
    }

    private byte[] getHash(@NonNull byte[] otaData) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        byte[] md5 = CryptoUtils.getMD5(otaData);
        if (md5 != null) {
            bb.put(md5, 0, 4);
        } else {
            bb.putInt(0xFFFFFFFF);
        }
        return bb.array();
    }

    /**
     * 设置OTA文件数据
     * @param otaData OTA数据
     */
    public void setOtaData(@NonNull final byte[] otaData) {
        dataProvider = new OtaDataProvider(otaData);
        requestGenerator.setDataProvider(dataProvider);
        // 计算Hash
        hashData = getHash(otaData);
        otaDataSize = otaData.length;
    }

    /**
     * 是否正在进行升级
     * @return 是否正在升级
     */
    public boolean isUpdating() {
        return isUpdating;
    }

    /**
     * 获取开始升级的地址
     * @return 升级地址
     */
    public int getStartAddress() {
        return dataProvider.getStartAddress();
    }

    /**
     * 设置开始升级的地址
     * @param address 升级地址
     */
    public void setStartAddress(int address) {
        dataProvider.setStartAddress(address);
    }

    /**
     * 设置块大小，固件接收一个块后会进行校验并做下一步处理，随后会回复状态到App。
     * @param size 块大小
     */
    public void setBlockSize(int size) {
        dataProvider.setBlockSize(size);
    }

    /**
     * 设置发送一包的最大大小（被MTU大小限制）。
     * @param size 最大包大小
     */
    public void setPacketSize(int size) {
        dataProvider.setPacketSize(size);
    }

    /**
     * 传入OTA固件版本
     * @param firmwareVersion 固件版本
     */
    public void setFirmwareVersion(int firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    /**
     * 传入固件HASH
     * @param hashData HASH, 4bytes
     */
    public void setHashData(byte[] hashData) {
        this.hashData = hashData;
    }

    /* OTA State */

    @Override
    public void onReceiveNotification(Byte state) {
        Log.v(TAG, "OTA state = " + state);
        switch (state) {
            case OtaState.OTA_STATE_OK:
                cancelTimeout();
                if (!isUpdatePause && !dataProvider.isAllDataSent()) {
                    sendBlock();
                }
                break;
            case OtaState.OTA_STATE_FINISH:
                cancelTimeout();
                // DONE
                notifyOnFinish();
                break;
            case OtaState.OTA_STATE_PAUSE:
                cancelTimeout();
                // Pause
                allowedUpdate = false; // 清掉，不然重新开始的时候会接着之前状态发送数据
                isUpdating = false;
                isUpdatePause = true;
                notifyOnPause();
                break;
            case OtaState.OTA_STATE_CONTINUE:
                // Resume
                notifyOnContinue();
                // Continue to update
                prepareToUpdate();
                break;
            default:
                isUpdating = false;
                reset();
                // Check if retry is needed
                if (needRetryOnState(state)) {
                    retryUpdate();
                } else {
                    notifyOnError(state);
                }
        }
    }

    /* Request Delegate */

    private void sendRequest(@NonNull Request request) {
        if (requestDelegate != null) {
            requestDelegate.sendRequest(request);
        }
    }

    private void sendRequest(@NonNull Request request,
                             @NonNull DeviceCommManager.RequestCallback requestCallback) {
        if (requestDelegate != null) {
            requestDelegate.sendRequest(request, requestCallback);
        }
    }

    public interface RequestDelegate {
        void sendRequest(@NonNull Request request);
        void sendRequest(@NonNull Request request,
                         @NonNull DeviceCommManager.RequestCallback requestCallback);
    }

    /* Event Listener */

    /**
     * OTA事件监听器
     */
    public interface EventListener {

        /* OTA状态 */

        /**
         * 通知设备是否允许升级
         * @param allowed 是否已允许
         */
        void onOtaAllowUpdate(boolean allowed);

        /**
         * OTA已开始
         */
        void onOtaStart();

        /**
         * OTA进度
         * @param progress 进度
         */
        void onOtaProgress(int progress);

        /**
         * OTA已完成
         */
        void onOtaFinish();

        /**
         * OTA已暂停
         */
        void onOtaPause();

        /**
         * OTA已继续
         */
        void onOtaContinue();

        /**
         * OTA遇到错误
         * @param errorCode 错误代码，参考OTA文档
         */
        void onOtaError(int errorCode);

    }

    public interface RetryCallback {
        /**
         * Before retrying next update
         */
        default void beforeRetryCallback() {}

        /**
         * After started retry next update
         */
        default void afterRetryCallback() {}
    }
}
