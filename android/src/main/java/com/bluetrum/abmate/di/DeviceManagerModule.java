package com.bluetrum.abmate.di;

import android.content.Context;

import com.bluetrum.abmate.eq.DeviceEqApi;
import com.bluetrum.abmate.viewmodels.DefaultDeviceCommManager;
import com.bluetrum.devicemanager.DeviceManagerApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
class DeviceManagerModule {

    @Singleton
    @Provides
    DefaultDeviceCommManager provideDeviceCommManager() {
        return new DefaultDeviceCommManager();
    }

    @Singleton
    @Provides
    DeviceManagerApi provideDeviceManagerApi(@ApplicationContext final Context context) {
        return new DeviceManagerApi(context);
    }

    @Singleton
    @Provides
    DeviceEqApi provideDeviceEqApi(@ApplicationContext final Context context) {
        return new DeviceEqApi(context);
    }

}
