package com.bluetrum.abmate.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.List;

public class Utils {

    public static final String HEX_PATTERN = "^[0-9a-fA-F]+$";

    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";
    public static final String ACTIVITY_RESULT = "RESULT_KEY";
    public static final String EXTRA_DATA = "EXTRA_DATA";

    private static final String PREFS_LOCATION_NOT_REQUIRED = "location_not_required";
    private static final String PREFS_PERMISSION_REQUESTED = "permission_requested";
    private static final String PREFS_READ_STORAGE_PERMISSION_REQUESTED = "read_storage_permission_requested";
    private static final String PREFS_WRITE_STORAGE_PERMISSION_REQUESTED = "write_storage_permission_requested";
    private static final String PREFS_BLUETOOTH_PERMISSION_REQUESTED = "PREFS_BLUETOOTH_PERMISSION_REQUESTED";

    public static final String PREFS_ENABLE_LAUNCHER_NOTIFICATION = "enable_launcher_notification";
    public static final String PREFS_ENABLE_QUICK_CONNECT = "enable_quick_connect";

    //Message timeout in case the message fails to lost/received
    public static final int MESSAGE_TIME_OUT = 10000;

    /**
     * Checks whether Bluetooth is enabled.
     *
     * @return true if Bluetooth is enabled, false otherwise.
     */
    public static boolean isBleEnabled() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    /**
     * Checks for required permissions.
     *
     * @return true if permissions are already granted, false otherwise.
     */
    public static boolean isBluetoothScanAndConnectPermissionsGranted(final Context context) {
        if(!isAndroid12OrAbove())
            return true;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Returns true if location permission has been requested at least twice and
     * user denied it, and checked 'Don't ask again'.
     *
     * @param activity the activity
     * @return true if permission has been denied and the popup will not come up any more, false otherwise
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    public static boolean isBluetoothPermissionDeniedForever(final Activity activity) {
        final SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(activity);

        return !isBluetoothScanAndConnectPermissionsGranted(activity) // Location permission must be denied
                && preferences.getBoolean(PREFS_BLUETOOTH_PERMISSION_REQUESTED, false) // Permission must have been requested before
                && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.BLUETOOTH_SCAN) // This method should return false
                && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.BLUETOOTH_CONNECT); // This method should return false
    }

    /**
     * The first time an app requests a permission there is no 'Don't ask again' checkbox and
     * {@link ActivityCompat#shouldShowRequestPermissionRationale(Activity, String)} returns false.
     * This situation is similar to a permission being denied forever, so to distinguish both cases
     * a flag needs to be saved.
     *
     * @param context the context
     */
    public static void markBluetoothPermissionsRequested(final Context context) {
        final SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(PREFS_BLUETOOTH_PERMISSION_REQUESTED, true).apply();
    }

    /**
     * Checks for required permissions.
     *
     * @return true if permissions are already granted, false otherwise.
     */
    public static boolean isLocationPermissionsGranted(final Context context) {
        if(isAndroid12OrAbove())
            return true;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Returns true if location permission has been requested at least twice and
     * user denied it, and checked 'Don't ask again'.
     *
     * @param activity the activity
     * @return true if permission has been denied and the popup will not come up any more, false otherwise
     */
    public static boolean isLocationPermissionDeniedForever(final Activity activity) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        return !isLocationPermissionsGranted(activity) // Location permission must be denied
                && preferences.getBoolean(PREFS_PERMISSION_REQUESTED, false) // Permission must have been requested before
                && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION); // This method should return false
    }

    /**
     * On some devices running Android Marshmallow or newer location services must be enabled in order to scan for Bluetooth LE devices.
     * This method returns whether the Location has been enabled or not.
     *
     * @return true on Android 6.0+ if location mode is different than LOCATION_MODE_OFF. It always returns true on Android versions prior to Marshmallow.
     */
    @SuppressWarnings("deprecation")
    public static boolean isLocationEnabled(final Context context) {
        if (isWithinMarshmallowAndR()) {
            int locationMode = Settings.Secure.LOCATION_MODE_OFF;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (final Settings.SettingNotFoundException e) {
                // do nothing
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        return true;
    }

    /**
     * Location enabled is required on some phones running Android Marshmallow or newer (for example on Nexus and Pixel devices).
     *
     * @param context the context
     * @return false if it is known that location is not required, true otherwise
     */
    public static boolean isLocationRequired(final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREFS_LOCATION_NOT_REQUIRED, isWithinMarshmallowAndR());
    }

    /**
     * When a Bluetooth LE packet is received while Location is disabled it means that Location
     * is not required on this device in order to scan for LE devices. This is a case of Samsung phones, for example.
     * Save this information for the future to keep the Location info hidden.
     *
     * @param context the context
     */
    public static void markLocationNotRequired(final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(PREFS_LOCATION_NOT_REQUIRED, false).apply();
    }

    /**
     * The first time an app requests a permission there is no 'Don't ask again' checkbox and
     * {@link ActivityCompat#shouldShowRequestPermissionRationale(Activity, String)} returns false.
     * This situation is similar to a permission being denied forever, so to distinguish both cases
     * a flag needs to be saved.
     *
     * @param context the context
     */
    public static void markLocationPermissionRequested(final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(PREFS_PERMISSION_REQUESTED, true).apply();
    }

    /**
     * Checks for required permissions.
     *
     * @return true if permissions are already granted, false otherwise.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isWriteExternalStoragePermissionsGranted(final Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * The first time an app requests a permission there is no 'Don't ask again' checkbox and
     * {@link ActivityCompat#shouldShowRequestPermissionRationale(Activity, String)} returns false.
     * This situation is similar to a permission being denied forever, so to distinguish both cases
     * a flag needs to be saved.
     *
     * @param context the context
     */
    public static void markWriteStoragePermissionRequested(final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(PREFS_WRITE_STORAGE_PERMISSION_REQUESTED, true).apply();
    }

    /**
     * Returns true if write external permission has been requested at least twice and
     * user denied it, and checked 'Don't ask again'.
     *
     * @param activity the activity
     * @return true if permission has been denied and the popup will not come up any more, false otherwise
     */
    public static boolean isWriteExternalStoragePermissionDeniedForever(final Activity activity) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        return !isWriteExternalStoragePermissionsGranted(activity) // Location permission must be denied
                && preferences.getBoolean(PREFS_WRITE_STORAGE_PERMISSION_REQUESTED, false) // Permission must have been requested before
                && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE); // This method should return false
    }

    /**
     * 设置启动通知栏
     * @param context the context
     * @param enable 是否启用启动通知栏
     */
    public static void setPrefsEnableLaunchNotification(final Context context, final boolean enable) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(PREFS_ENABLE_LAUNCHER_NOTIFICATION, enable).apply();
    }

    /**
     * 获取启动通知栏设置，默认启用
     * @param context the context
     * @return 返回是否使用
     */
    public static boolean getPrefsEnableLaunchNotification(final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREFS_ENABLE_LAUNCHER_NOTIFICATION, false);
    }

    public static void setPrefsEnableQuickConnect(final Context context, final boolean enable) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(PREFS_ENABLE_QUICK_CONNECT, enable).apply();
    }

    public static boolean getPrefsEnableQuickConnect(final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREFS_ENABLE_QUICK_CONNECT, false);
    }

    /**
     * 蓝牙扫描/连接需要的权限是否已经授予。
     * Android S及以上是Bluetooth Scan和Connect权限，Android S以下是位置权限。
     * @param context the context
     * @return 是否已经授予权限
     */
    public static boolean isBluetoothNeededPermissionGranted(final Context context) {
        if (isAndroid12OrAbove()) {
            return isBluetoothScanAndConnectPermissionsGranted(context);
        } else {
            return isLocationPermissionsGranted(context);
        }
    }

    public static boolean isAndroid12OrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }

    public static boolean isWithinMarshmallowAndR() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.R;
    }

    public static void showToast(final Context context, @StringRes final int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(final Context context, final String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean isValidUint8(final int i) {
        return ((i & 0xFFFFFF00) == 0 || (i & 0xFFFFFF00) == 0xFFFFFF00);
    }

    public static boolean checkIfVersionIsOreoOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * 将App在任务管理器中隐藏
     * @param context the context
     * @param exclude 是否隐藏
     */
    public static void setAppExcludeFromRecents(final Context context, final boolean exclude) {
        // 在任务管理器隐藏
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        if(am != null) {
            List<ActivityManager.AppTask> tasks = am.getAppTasks();
            if (tasks != null && tasks.size() > 0) {
                tasks.get(0).setExcludeFromRecents(exclude);
            }
        }
    }

    public static void enableControls(boolean enable, ViewGroup vg){
        vg.setEnabled(enable);
        for (int i = 0; i < vg.getChildCount(); i++){
            View child = vg.getChildAt(i);
            child.setEnabled(enable);
            if (child instanceof ViewGroup){
                enableControls(enable, (ViewGroup)child);
            }
        }
    }

}
