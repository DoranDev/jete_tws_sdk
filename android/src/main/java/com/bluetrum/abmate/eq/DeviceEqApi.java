package com.bluetrum.abmate.eq;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;

public class DeviceEqApi {

    private EqDataBase eqDataBase;

    public DeviceEqApi(final Context context) {
        initDb(context);
    }

    private void initDb(final Context context) {
        eqDataBase = EqDataBase.getDatabase(context);
    }

    void insertEqSetting(@NonNull final EqSetting eqSetting) {
        eqDataBase.insertEqSetting(eqSetting);
    }

    void updateEqSetting(@NonNull final EqSetting eqSetting) {
        eqDataBase.updateEqSetting(eqSetting);
    }

    void updateEqSetting(@NonNull final String name, @NonNull final byte[] gains) {
        eqDataBase.updateEqSetting(name, gains);
    }

    void deleteEqSetting(@NonNull final EqSetting eqSetting) {
        eqDataBase.deleteEqSetting(eqSetting);
    }

    void deleteEqSetting(@NonNull final String name) {
        eqDataBase.deleteEqSetting(name);
    }

    // 同步，数据库不大，影响不大
    List<EqSetting> getEqSettings() {
        return eqDataBase.getEqSettings();
    }

    LiveData<List<EqSetting>> getEqSettingsLiveData() {
        return eqDataBase.getEqSettingsLiveData();
    }

}
