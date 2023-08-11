package com.bluetrum.devicemanager;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bluetrum.devicemanager.cmd.Command;
import com.bluetrum.devicemanager.cmd.Notification;
import com.bluetrum.devicemanager.cmd.Response;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

class ResponseMerger {
	private static final String TAG = ResponseMerger.class.getSimpleName();

	private final Handler payloadHandler;

	private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	private Integer totalFrame = null;
	private byte currentCommand;
	private byte currentCommandType;
	private int expectedIndex;

	ResponseMerger(final Handler payloadHandler) {
		this.payloadHandler = payloadHandler;
	}

	boolean merge(final byte command, final byte commandType,
			@NonNull final byte[] payload, final int total, final int index) {

		// 如果totalFrame为空，则说明开始新一包
		if (totalFrame == null) {
			// 新的一包index必须从0开始
			if (index != 0) {
				Log.w(TAG, "Wrong seq number: Expected 0, but got " + total);
				sendErrorMessage(DeviceCommManager.RESPONSE_ERROR_SEQ_NUMBER_NOT_FROM_ZERO);
				return false;
			}
			totalFrame = total;
			currentCommand = command;
			currentCommandType = commandType;
			expectedIndex = 0;
		} else {
			// 判断是否丢包是否错包（命令、类型、total
			if (currentCommand != command
					&& currentCommandType != commandType
					&& totalFrame != total) {
				Log.w(TAG, "Merge response packet error (cmd, cmdType, total): " +
						String.format(Locale.getDefault(), "Expected (%d, %d, %d), but got (%d, %d, %d)",
								currentCommand, currentCommandType, totalFrame, command, commandType, total));
				sendErrorMessage(DeviceCommManager.RESPONSE_ERROR_PACKET_INFO_MISMATCH);
				reset();
				return false;
			}

			if (expectedIndex != index) {
				Log.w(TAG, "Frame seq mismatch: Expected " + expectedIndex + " but got parameter " + index);
				sendErrorMessage(DeviceCommManager.RESPONSE_ERROR_WRONG_FRAME_SEQ_NUMBER);
				reset();
				return false;
			}
		}

		buffer.write(payload, 0, payload.length);

		// 已经是最后一包
		if (index == totalFrame - 1) {
			// 判断Notify需要满足命令和命令类型两个条件
			if (commandType == Command.COMMAND_TYPE_NOTIFY) {
				Notification notification = new Notification(command, buffer.toByteArray());
				sendNotificationMessage(notification);
			}
			// Response只需要判断命令类型
			else if (commandType == Command.COMMAND_TYPE_RESPONSE) {
				Response response = new Response(command, buffer.toByteArray());
				sendResponseMessage(response);
			}
			reset();
			return true;
		}

		expectedIndex++;

		return true;
	}

	void reset() {
		buffer.reset();
		this.totalFrame = null;
		this.currentCommand = 0;
		this.currentCommandType = 0;
		this.expectedIndex = 0;
	}

	/* Message */

	private void sendResponseMessage(final Response response) {
		Message message = payloadHandler.obtainMessage(ResponseHandler.RESPONSE_TYPE_RESPONSE);
		message.obj = response;
		payloadHandler.sendMessage(message);
	}

	private void sendNotificationMessage(final Notification notification) {
		Message message = payloadHandler.obtainMessage(ResponseHandler.RESPONSE_TYPE_NOTIFICATION);
		message.obj = notification;
		payloadHandler.sendMessage(message);
	}

	private void sendErrorMessage(final int errorCode) {
		Message message = payloadHandler.obtainMessage(ResponseHandler.RESPONSE_TYPE_ERROR);
		message.arg1 = errorCode;
		payloadHandler.sendMessage(message);
	}

}
