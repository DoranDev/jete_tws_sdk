package com.bluetrum.fota;

import com.bluetrum.fota.cmd.request.OtaSendDataRequest;
import com.bluetrum.fota.cmd.request.OtaStartRequest;

public final class OtaRequestGenerator {

    private OtaDataProvider dataProvider;

    public OtaRequestGenerator() {
    }

    /**
     * 构造器
     * @param dataProvider {@link OtaDataProvider}
     */
    public OtaRequestGenerator(OtaDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    /* Getter & Setter */

    /**
     * 设置{@link OtaDataProvider}
     * @param dataProvider {@link OtaDataProvider}
     */
    public void setDataProvider(OtaDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    /* Request */

    public OtaStartRequest getOtaStartRequest() {
        int startAddress = dataProvider.getStartAddress();
        int dataLen = dataProvider.getTotalLengthToBeSent();
        byte[] data = dataProvider.getStartData(4 + 4); // sizeof(startAddress) + sizeof(dataLen)
        return new OtaStartRequest(startAddress, dataLen, data);
    }

    public OtaSendDataRequest getOtaSendDataRequest() {
        byte[] data = dataProvider.getDataToBeSent(0);
        return new OtaSendDataRequest(data);
    }

}
