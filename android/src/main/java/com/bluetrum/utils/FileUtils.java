package com.bluetrum.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    private static final String PREF_KEY_LAST_POSITION = "last_position";

    /**
     * 读取文件，供OTA使用。因OTA文件较小，所以一次性读取。
     * @param filePath 文件路径
     * @return 返回文件字节数组
     * @throws IOException 文件操作可能产生的IO异常
     */
    public static byte[] readFile(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream fis = new FileInputStream(file);

        int length = fis.available();
        byte[] buffer = new byte[length];
        fis.read(buffer);

        fis.close();
        return buffer;
    }

    /**
     * 从文件路径中获取目录路径
     * @param path 文件路径
     * @return 目录路径
     */
    public static String getDirFromFilePath(String path) {
        File file = new File(path);
        return file.getParent();
    }

    /**
     * 在Preference中保存文件管理器最后打开的路径
     * @param context {@link Context}
     * @param path 文件管理器最后打开的路径
     */
    public static void saveLastPosition(Context context, String path) {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(PREF_KEY_LAST_POSITION, path);
        editor.apply();
    }

    /**
     * 从Preference中获取文件管理器最后打开的路径
     * @param context {@link Context}
     * @return 文件管理器最后打开的路径
     */
    public static String getLastPosition(Context context) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String lastPosition = sharedPref.getString(PREF_KEY_LAST_POSITION, "");
        return lastPosition;
    }

    /**
     * 列出Assets文件
     * @param context the context
     * @param path 目录路径
     * @param extension 扩展名，如果为null则不进行过滤
     * @return 返回文件路径列表
     */
    public static List<String> listAssetFiles(@NonNull Context context,
                                              @NonNull String path,
                                              @Nullable String extension) {
        List<String> fileList = new ArrayList<>();
        try {
            String[] fileArray = context.getAssets().list(path);
            if (fileArray != null) {
                for (String file : fileArray) {
                    if (extension != null && !file.endsWith(extension)) {
                        continue;
                    }
                    String filePath = path + "/" + file;
                    fileList.add(filePath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileList;
    }

}
