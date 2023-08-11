package com.bluetrum.devicemanager;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.bluetrum.devicemanager.db.BaseDevice;
import com.bluetrum.devicemanager.db.BaseDeviceDao;
import com.bluetrum.devicemanager.db.BlockRecord;
import com.bluetrum.devicemanager.db.BlockRecordDao;
import com.bluetrum.devicemanager.models.ABDevice;

import java.util.List;

/**
 * 负责底层设备数据库，和应用层设备有关的逻辑
 * 要做框架无关，就手动解析数据，没有使用Android Bluetooth框架的解析逻辑
 */
public class DeviceManagerApi {

    private static final String TAG = DeviceManagerApi.class.getSimpleName();

    private Context mContext;

    // 已配对设备
    private ABDeviceDb mAbDeviceDb;
    private BaseDeviceDao mBaseDeviceDao;
    // 屏蔽弹窗列表
    private ABBlockDb mAbBlockDb;
    private BlockRecordDao mBlockRecordDao;

    public DeviceManagerApi(final Context context) {
        this.mContext = context;
        // 初始化数据库
        initDb(context);
    }

    private void initDb(final Context context) {
        // 已配对设备
        mAbDeviceDb = ABDeviceDb.getDatabase(context);
        mBaseDeviceDao = mAbDeviceDb.baseDeviceDao();
        // 屏蔽弹窗列表
        mAbBlockDb = ABBlockDb.getDatabase(context);
        mBlockRecordDao = mAbBlockDb.blockRecordDao();
    }

    /* 数据库，已配对设备 */

    public LiveData<List<BaseDevice>> getBaseDevices() {
        return mBaseDeviceDao.getBaseDevices();
    }

    @Nullable
    public BaseDevice getBaseDevice(String address) {
        return mBaseDeviceDao.getBaseDevice(address);
        // todo: BtDevice转换为BaseDevice
//        BtPodDevice btPodDevice = new BtPodDevice()
    }

    public void insertBtDevice(@NonNull ABDevice device) {
        // BtDevice转换为BaseDevice
//        BaseDevice baseDevice = new BaseDevice(
//                device.getClassicAddress(),
//                device.getBtName(),
//                device.getRandom()
//        );
//        mAbDeviceDb.insertBaseDevice(baseDevice);
    }

    public void updatePodDevice(@NonNull ABDevice device) {
        // BtDevice转换为BaseDevice
//        BaseDevice baseDevice = new BaseDevice(
//                device.getClassicAddress(),
//                device.getBtName(),
//                device.getRandom()
//        );
//        mAbDeviceDb.updatePodDevice(baseDevice);
    }

    public void deleteBtDevice(@NonNull ABDevice device) {
//        mAbDeviceDb.deletePodDevice(device.getBtAddress());
    }

    /* 数据库，屏蔽弹窗列表 */

    public void insertBlockRecord(@NonNull String address, long blockTime) {
        mAbBlockDb.insertBlockRecord(address, blockTime);
    }

    public void updateBlockRecord(@NonNull String address, long blockTime) {
        mAbBlockDb.updateBlockRecord(address, blockTime);
    }

    public void deleteBlockRecord(@NonNull String address) {
        mAbBlockDb.deleteBlockRecord(address);
    }

    public long getBlockTime(@NonNull String address) {
        // 数据库不大，同步影响不大
        BlockRecord record = mAbBlockDb.getBlockRecord(address);
        if (record != null) {
            return record.getBlockTime();
        }
        return 0;
    }

}
