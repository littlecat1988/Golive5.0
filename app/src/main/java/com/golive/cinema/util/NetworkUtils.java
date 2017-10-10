package com.golive.cinema.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Wangzj on 2016/11/22.
 */

public class NetworkUtils {

    /**
     * Get ConnectivityManager
     */
    public static ConnectivityManager getConnectivityManager(Context context) {
        // Marshmallow bug!
        // ConnectivityManager is singleton in Marshmallow, so it will reference the context!
        Context applicationContext = context.getApplicationContext();
        return (ConnectivityManager) applicationContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);
    }

    /**
     * Check whether the network is available
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connManager = getConnectivityManager(context);
        if (null == connManager) {
            return false;
        }
        NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && (activeNetworkInfo.isAvailable()
                || activeNetworkInfo.isConnected());
    }

    /**
     * 根据url得到文件名
     */
    public static String getFileNameFromUrl(String url) {
        // 通过 ‘？’ 和 ‘/’ 判断文件名
        int index = url.lastIndexOf('?');
        String filename;
        if (index > 1) {
            filename = url.substring(url.lastIndexOf('/') + 1, index);
        } else {
            filename = url.substring(url.lastIndexOf('/') + 1);
        }
        return filename;
    }

    /**
     * 获取网络传输总数据
     *
     * @param Transmit true是下载,false是上传
     */
    public static long getTotalDataBytes(boolean Transmit) {
        String readLine;
        String[] DataPart;
        // int line = 0;
        long Data = 0;
        BufferedReader reader = null;
        try {
            // 使用BufferedReader打开文件
            FileInputStream fis = new FileInputStream("/proc/net/dev");
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            reader = new BufferedReader(isr);

            // 按行读取数据并相加
            while ((readLine = reader.readLine()) != null) {
                // // 跳过文件头两行
                // line++;
                // if (line <= 2) continue;

                readLine = readLine.trim();

                if (readLine.startsWith("rmnet") || readLine.startsWith("eth")
                        || readLine.startsWith("wlan")) {

                    // 使用split分割字符串
                    DataPart = readLine.split(":");
                    DataPart = DataPart[1].split("\\s+");

                    if (Transmit && DataPart.length >= 2) {
                        // 获取接收的总流量
                        Data += Long.parseLong(DataPart[1]);
                    } else {
                        if (DataPart.length >= 10) {
                            // 获取上传的总流量
                            Data += Long.parseLong(DataPart[9]);
                        }
                    }
                }

            }
            // 关闭文件
            reader.close();
            reader.close();
        } catch (IOException e) {
            // 获取失败则返回-1
            return -1;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 返回数据的总字节
        return Data;
    }
}
