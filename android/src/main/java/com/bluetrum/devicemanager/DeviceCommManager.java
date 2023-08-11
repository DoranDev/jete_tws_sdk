package com.bluetrum.devicemanager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluetrum.devicemanager.cmd.Command;
import com.bluetrum.devicemanager.cmd.Notification;
import com.bluetrum.devicemanager.cmd.Request;
import com.bluetrum.devicemanager.cmd.Response;
import com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler;
import com.bluetrum.utils.ThreadUtils;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 设备通信管理器
 * 负责发送请求和接收响应。发送请求时，如果请求过长，
 * 则会根据MTU进行分包；接收响应时，将自动进行组包。
 * 接收到数据后，可以根据注册的数据处理程序来处理响应和上报的数据。
 */
public class DeviceCommManager {

    // 处理Response产生的错误代码
    // 除了RESPONSE_ERROR_WRONG_SEQ_NUMBER有可能还会继续处理回复，其他均不会
    public static final int RESPONSE_ERROR_WRONG_SEQ_NUMBER         = 0x00; // 序号错误，继续处理
    public static final int RESPONSE_ERROR_PAYLOAD_TOO_SMALL        = 0x01;
    public static final int RESPONSE_ERROR_BAD_PAYLOAD_LENGTH       = 0x02;
    public static final int RESPONSE_ERROR_SEQ_NUMBER_NOT_FROM_ZERO = 0x03;
    public static final int RESPONSE_ERROR_PACKET_INFO_MISMATCH     = 0x04;
    public static final int RESPONSE_ERROR_WRONG_FRAME_SEQ_NUMBER   = 0x05;

    private static final String TAG = DeviceCommManager.class.getSimpleName();

    private static final String THREAD_NAME_RECEIVE_RESPONSE = "THREAD_NAME_RECEIVE_RESPONSE";
    private static final String THREAD_NAME_TIMEOUT = "THREAD_NAME_TIMEOUT";

    private final RequestHandler requestHandler;
    private final ResponseHandler responseHandler;

    private DeviceCommDelegate commDelegate;
    private DeviceResponseErrorHandler responseErrorHandler;

    private final Deque<Request> requestQueue = new LinkedBlockingDeque<>();
    // If the request is present indicates it has been sent and is waiting for response
    private Request currentRequest = null;

    private final Map<Request, RequestCallback> requestCallbackMap = new LinkedHashMap<>();
    private final Map<Request, Runnable> timeoutMap = new HashMap<>();

    private final Handler timeoutHandler;

    // 处理消息回复
    private final Map<Byte, Class<? extends PayloadHandler<?>>> responseCallableCreatorMap = new HashMap<>();

    // 处理通知
    private final Map<Byte, Class<? extends PayloadHandler<?>>> notificationCallableCreatorMap = new HashMap<>();
    private final Map<Byte, NotificationCallback<?>> notificationCallbackMap = new HashMap<>();

    // 处理设备信息等上报型信息
    private final Map<Byte, Class<? extends PayloadHandler<?>>> deviceInfoCallableCreatorMap = new HashMap<>();
    private final Map<Byte, DeviceInfoCallback<?>> deviceInfoCallbackMap = new HashMap<>();

    public DeviceCommManager() {
        // 发送请求的Handler
        requestHandler = new RequestHandler();

        // 接收回复的Handler
        HandlerThread responseThread = new HandlerThread(THREAD_NAME_RECEIVE_RESPONSE);
        responseThread.start();
        Handler.Callback responseHandlerCallback = this::handleResponseMessage;
        Handler responsePayloadHandler = new Handler(responseThread.getLooper(), responseHandlerCallback);
        responseHandler = new ResponseHandler(responsePayloadHandler);

        HandlerThread timeoutThread = new HandlerThread(THREAD_NAME_TIMEOUT);
        timeoutThread.start();
        timeoutHandler = new Handler(timeoutThread.getLooper());
    }

    public void setMaxPacketSize(int maxPacketSize) {
        requestHandler.setMaxPacketSize(maxPacketSize);
    }

    public int getMaxPayloadSize() {
        return requestHandler.getMaxPayloadSize();
    }

    /**
     * 发送请求，不带回调。
     * @param request 请求
     */
    public void sendRequest(@NonNull final Request request) {
        sendRequest(request, null);
    }

    /**
     * 发送请求，带结果或者超时的回调。
     * @param request 请求
     * @param requestCallback 回调
     */
    public void sendRequest(@NonNull final Request request,
                            @Nullable final RequestCallback requestCallback) {
        if (requestCallback != null) {
            requestCallbackMap.put(request, requestCallback);
            // 超时
            Runnable timeoutRunnable = () -> {
                currentRequest = null;
                // 超时的同时发出下一个请求
                nextRequest();

                requestCallbackMap.remove(request);
                timeoutMap.remove(request);
                // 主线程调用
                ThreadUtils.postOnMainThread(() -> {
                    requestCallback.onTimeout(request);
                });
            };
            timeoutMap.put(request, timeoutRunnable);
            timeoutHandler.postDelayed(timeoutRunnable, request.getTimeout());
        }
        requestQueue.add(request);
        nextRequest();
    }

    /**
     * 取消请求
     * @param request 指定的请求，必须和{@link DeviceCommManager#sendRequest(Request)}
     *                和{@link DeviceCommManager#sendRequest(Request, RequestCallback)}的为同一个。
     * @return 如果请求还没有发出，取消成功，返回true；如果请求已经发出，取消失败，返回false。
     */
    public boolean cancelRequest(@NonNull final Request request) {
        if (requestQueue.remove(request)) {
            timeoutMap.remove(request);
            requestCallbackMap.remove(request);
            return true;
        }
        return false;
    }

    public void cancelAllRequests() {
        Request request;
        while ((request = requestQueue.pollFirst()) != null) {
            timeoutMap.remove(request);
            requestCallbackMap.remove(request);
        }
    }

    private void nextRequest() {
        if (currentRequest != null) {
            return;
        }
        Request request = requestQueue.pollFirst();
        if (request != null) {
            if (request.withResponse()) {
                currentRequest = request;
            }

            Deque<byte[]> requestDataQueue = requestHandler.handleRequest(request);
            byte[] requestData;
            while ((requestData = requestDataQueue.pollFirst()) != null) {
                if (commDelegate != null) {
                    commDelegate.sendRequestData(requestData);
                }
            }
        }
    }

    public void handleData(byte[] data) {
        responseHandler.handleFrameData(data);
    }

    public void reset() {
        cancelAllRequests();
        currentRequest = null;
        requestHandler.reset();
        responseHandler.reset();
    }

    /* 设备信息回调 */

    protected void processDeviceInfo(byte type, byte[] data) {

        Class<? extends PayloadHandler<?>> callableCreator;
        Object resultCallback;
        synchronized (this) {
            callableCreator = deviceInfoCallableCreatorMap.get(type);
            resultCallback = deviceInfoCallbackMap.get(type);
        }
        if (callableCreator != null && resultCallback != null) {
            try {
                Constructor<? extends PayloadHandler<?>> constructor = callableCreator.getDeclaredConstructor(byte[].class);
                PayloadHandler<?> callable = constructor.newInstance((Object) data);
                Future<?> parseFuture = ThreadUtils.postOnBackgroundThread(() -> {
                    try {
                        Future<?> future = ThreadUtils.postOnBackgroundThread(callable);
                        Object result = future.get();
                        DeviceInfoCallback<Object> callback = (DeviceInfoCallback<Object>) resultCallback;
                        ThreadUtils.postOnMainThread(() -> callback.onReceiveInfo(result));
                    } catch (Exception e) {
                        Log.w(TAG, "Device info parse error", e);
                    }
                });
                parseFuture.get();
            } catch (Exception e) {
                Log.w(TAG, "DeviceInfoCallback<?> error", e);
            }
        }
    }

    protected void processDeviceInfoData(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);

        while (bb.remaining() >= 2) {
            byte infoType = bb.get();
            byte infoLen = bb.get();

            if (infoLen <= bb.remaining()) {
                byte[] infoData = new byte[infoLen];
                bb.get(infoData);

                processDeviceInfo(infoType, infoData);
            }
        }
    }

    public <T> void registerDeviceInfoCallback(final byte deviceInfoType,
                                               @NonNull final Class<? extends PayloadHandler<T>> callableCreator,
                                               @NonNull final DeviceInfoCallback<T> callback) {
        synchronized (this) {
            deviceInfoCallableCreatorMap.put(deviceInfoType, callableCreator);
            deviceInfoCallbackMap.put(deviceInfoType, callback);
        }
    }

    public void unregisterDeviceInfoCallback(final byte deviceInfoType) {
        synchronized (this) {
            deviceInfoCallableCreatorMap.remove(deviceInfoType);
            deviceInfoCallbackMap.remove(deviceInfoType);
        }
    }

    public interface DeviceInfoCallback<T> {
        void onReceiveInfo(T info);
    }

    /* 处理通知 */

    protected void processNotification(byte command, byte[] data) {

        Class<? extends PayloadHandler<?>> callableCreator;
        Object resultCallback;
        synchronized (this) {
            callableCreator = notificationCallableCreatorMap.get(command);
            resultCallback = notificationCallbackMap.get(command);
        }
        if (callableCreator != null && resultCallback != null) {
            try {
                Constructor<? extends PayloadHandler<?>> constructor = callableCreator.getDeclaredConstructor(byte[].class);
                PayloadHandler<?> callable = constructor.newInstance((Object) data);
                Future<?> parseFuture = ThreadUtils.postOnBackgroundThread(() -> {
                    try {
                        Future<?> future = ThreadUtils.postOnBackgroundThread(callable);
                        Object result = future.get();
                        NotificationCallback<Object> callback = (NotificationCallback<Object>) resultCallback;
                        ThreadUtils.postOnMainThread(() -> callback.onReceiveNotification(result));
                    } catch (Exception e) {
                        Log.w(TAG, "Notification parse error", e);
                    }
                });
                parseFuture.get();
            } catch (Exception e) {
                Log.w(TAG, "NotificationCallable<?> error", e);
            }
        }
    }

    protected void processNotificationData(byte[] data) {

        ByteBuffer bb = ByteBuffer.wrap(data);

        while (bb.remaining() >= 2) {
            byte notificationCommand = bb.get();
            byte notificationLen = bb.get();

            if (notificationLen <= bb.remaining()) {
                byte[] notificationData = new byte[notificationLen];
                bb.get(notificationData);

                processNotification(notificationCommand, notificationData);
            }
        }
    }

    private void onReceiveNotification(Notification notification) {
        byte notificationCommand = notification.getCommand();
        final byte[] notificationData = notification.getPayload();

        // COMMAND_NOTIFY的格式属于TLV，单独处理
        if (notificationCommand == Command.COMMAND_NOTIFY) {
            processNotificationData(notificationData);
        } else {
            processNotification(notificationCommand, notificationData);
        }
    }

    public <T> void registerNotificationCallback(final byte notifyType,
                                                 @NonNull final Class<? extends PayloadHandler<T>> callableCreator,
                                                 @NonNull final NotificationCallback<T> callback) {
        synchronized (this) {
            notificationCallableCreatorMap.put(notifyType, callableCreator);
            notificationCallbackMap.put(notifyType, callback);
        }
    }

    public void unregisterNotificationCallback(final byte notifyType) {
        synchronized (this) {
            notificationCallableCreatorMap.remove(notifyType);
            notificationCallbackMap.remove(notifyType);
        }
    }

    public interface NotificationCallback<T> {
        void onReceiveNotification(T notification);
    }

    /* 处理回复 */

    private void onReceiveResponse(@NonNull final Response response) {
        currentRequest = null;
        // Handle next request when received response
        nextRequest();

        // Find corresponding request of the response in CallbackMap
        Request request = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
             request = requestCallbackMap.entrySet().stream().filter(entry ->
                         entry.getKey().getCommand() == response.getCommand()
                 ).findFirst().map(Map.Entry::getKey).orElse(null);
        }else{
            for (Map.Entry<Request, RequestCallback> entry : requestCallbackMap.entrySet()) {
                if (entry.getKey().getCommand() == response.getCommand()) {
                    request = entry.getKey();
                    break;
                }
            }
        }
        // Remove timeout handler
        if (request != null) {
            Runnable timeoutRunnable = timeoutMap.remove(request);
            if (timeoutRunnable != null) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
            }
        }

        final byte responseCommand = response.getCommand();
        final byte[] responseData = response.getPayload();

        // Handle response data
        if (responseCommand == Command.COMMAND_DEVICE_INFO) {
            // Remove callback if exist
            if (request != null) {
                requestCallbackMap.remove(request);
            }
            processDeviceInfoData(responseData);
        } else if (request != null) {
            // Callback
            RequestCallback requestCallback = requestCallbackMap.remove(request);
            if (requestCallback != null) {
                Class<? extends PayloadHandler<?>> responseCallableCreator = responseCallableCreatorMap.get(responseCommand);
                if (responseCallableCreator != null) {
                    try {
                        Constructor<? extends PayloadHandler<?>> constructor = responseCallableCreator.getDeclaredConstructor(byte[].class);
                        PayloadHandler<?> responseCallable = constructor.newInstance((Object) responseData);
                        Future<?> future = ThreadUtils.postOnBackgroundThread(responseCallable);
                        try {
                            Object result = future.get();
                            // Run in main thread
                            Request finalRequest = request;
                            ThreadUtils.postOnMainThread(() -> requestCallback.onComplete(finalRequest, result));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void onError(int errorCode) {
        if (responseErrorHandler != null) {
            responseErrorHandler.onError(errorCode);
        }
    }

    public void registerResponseCallable(final byte command,
                                         @NonNull Class<? extends PayloadHandler<?>> callableCreator) {
        responseCallableCreatorMap.put(command, callableCreator);
    }

    public void registerResponseCallables(@NonNull Map<Byte, Class<? extends PayloadHandler<?>>> creators) {
        responseCallableCreatorMap.putAll(creators);
    }

    public void unregisterResponseCallable(final byte command) {
        responseCallableCreatorMap.remove(command);
    }

    /* Response Handler */

    private boolean handleResponseMessage(Message msg) {
        switch (msg.what) {
            case ResponseHandler.RESPONSE_TYPE_RESPONSE:
                Response response = (Response) msg.obj;
                onReceiveResponse(response);
                return true;
            case ResponseHandler.RESPONSE_TYPE_NOTIFICATION:
                Notification notification = (Notification) msg.obj;
                onReceiveNotification(notification);
                return true;
            case ResponseHandler.RESPONSE_TYPE_ERROR:
                int errorCode = msg.arg1;
                onError(errorCode);
                return true;
        }
        return false;
    }

    /* Interface */

    public void setCommDelegate(DeviceCommDelegate commDelegate) {
        this.commDelegate = commDelegate;
    }

    public interface DeviceCommDelegate {
        void sendRequestData(byte[] data);
    }

    public void setResponseErrorHandler(DeviceResponseErrorHandler responseErrorHandler) {
        this.responseErrorHandler = responseErrorHandler;
    }

    public interface DeviceResponseErrorHandler {
        void onError(int errorCode);
    }

    public interface RequestCallback {
        void onComplete(@NonNull final Request request, @Nullable Object result);
        void onTimeout(@NonNull final Request request);
    }
}
