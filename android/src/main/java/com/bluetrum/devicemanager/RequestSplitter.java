package com.bluetrum.devicemanager;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

class RequestSplitter {

	private int maxPayloadSize;

	RequestSplitter(@IntRange(from = 1) final int maxPayloadSize) {
		this.maxPayloadSize = maxPayloadSize;
	}

	int getMaxPayloadSize() {
		return maxPayloadSize;
	}

	void setMaxPayloadSize(@IntRange(from = 1) final int maxPayloadSize) {
		this.maxPayloadSize = maxPayloadSize;
	}

	int getFragNum(int payloadLength) {
		return (payloadLength + maxPayloadSize - 1) / maxPayloadSize;
	}

	@NonNull
	byte[] chunk(@NonNull final byte[] payload,
						@IntRange(from = 0) final int index) {
		final int offset = index * maxPayloadSize;
		final int length = Math.min(maxPayloadSize, payload.length - offset);

		if (length <= 0)
			return new byte[0];

		final byte[] data = new byte[length];
		System.arraycopy(payload, offset, data, 0, length);
		return data;
	}
}
