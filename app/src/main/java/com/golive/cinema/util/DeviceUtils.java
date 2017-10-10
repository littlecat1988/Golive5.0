package com.golive.cinema.util;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Wangzj on 2017/1/6.
 */

public class DeviceUtils {
    private static final String FILE_ADDRESS_MAC = "/sys/class/net/wlan0/address";

    /**
     * Get device id
     */
    public static String getDeviceId(Context context) {
        String deviceId = null;

        // first get phone device id
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(
                    Context.TELEPHONY_SERVICE);
            if (tm != null) {
                deviceId = tm.getDeviceId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // get android id
        if (StringUtils.isNullOrEmpty(deviceId)) {
            deviceId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        }
        return deviceId;
    }

    /**
     * Serial Number, SN
     */
    public static String getSn() {
        return android.os.Build.SERIAL;
    }

    /**
     * Get wireless mac address
     */
    public static String getWirelessMac(Context context) {
        String mac = null;
        WifiManager wifiMan = (WifiManager) context.getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);
        // Android 6.0
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (wifiMan != null) {
                WifiInfo wifiInf = wifiMan.getConnectionInfo();
                if (wifiInf != null) {
                    mac = wifiInf.getMacAddress();
                }
            }
        } else {
            mac = getAddressMacByInterface("wlan0");
            if (StringUtils.isNullOrEmpty(mac)) {
                if (wifiMan != null) {
                    try {
                        mac = getAddressMacByFile(wifiMan);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return mac;
    }

    /**
     * Get bluetooth mac address
     */
    public static String getBlueToothMac(Context context) {
        String mac = null;
        BluetoothAdapter ba = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(
                    Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                ba = bluetoothManager.getAdapter();
            }
        } else {
            ba = BluetoothAdapter.getDefaultAdapter();
        }
        if (ba != null) {
            mac = ba.getAddress();
        }
        return mac;
    }

    public static String getCpuID() {
        String cpuId = null;
        try {
            Process process = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            String content = convertStreamToString(process.getInputStream());
            process.waitFor();
            if (!StringUtils.isNullOrEmpty(content)) {
                String regex = "Serial[\\s]{0,}:[\\s]{0,}([0-9,a-z,A-Z]{1,})$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(content);
                boolean ret = matcher.find();
                if (ret) {
                    cpuId = matcher.group(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cpuId;
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * Get screen density
     */
    public static float getScreenDensity(Context context) {
        DisplayMetrics mDisplayMetrics = context.getResources().getDisplayMetrics();
        return mDisplayMetrics.density;
    }

    /**
     * Get screen width in px
     */
    public static int getScreenW(Context context) {
        DisplayMetrics mDisplayMetrics = context.getResources().getDisplayMetrics();
        return mDisplayMetrics.widthPixels;
    }

    /**
     * Get screen height in px
     */
    public static int getScreenH(Context context) {
        DisplayMetrics mDisplayMetrics = context.getResources().getDisplayMetrics();
        return mDisplayMetrics.heightPixels;
    }

    /**
     * 获取当前可用运存大小
     */
    public static long getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //mi.availMem; 当前系统的可用内存
        return mi.availMem;
    }

    /**
     * 获取总运存大小
     */
    public static long getTotalMemory(Context context) {
        // 系统内存信息文件
        final String file = "/proc/meminfo";
        long totalMem = 0;
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            reader = new BufferedReader(isr);
            // 读取meminfo第一行，系统总内存大小
            String content = reader.readLine();
            if (!StringUtils.isNullOrEmpty(content)) {
                String[] arrayOfString = content.trim().split("\\s+");
                if (arrayOfString != null && arrayOfString.length > 1) {
                    // 获得系统总内存，单位是KB，乘以1024转换为Byte
                    totalMem = Integer.valueOf(arrayOfString[1]) << 10;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return totalMem;
    }

    /**
     * 获得机身内存总大小
     */
    public static long getRomTotalSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return blockSize * totalBlocks;
    }

    /**
     * 获得机身可用内存
     */
    public static String getRomAvailableSize(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(context, blockSize * availableBlocks);
    }

    private static String getAddressMacByInterface(String eth) {
        if (StringUtils.isNullOrEmpty(eth)) {
            return null;
        }
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (nif.getName().equalsIgnoreCase(eth)) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }

                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02X:", b));
                    }

                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getAddressMacByFile(WifiManager wifiMan) {
        if (null == wifiMan) {
            return null;
        }
        String mac = null;
        int wifiState = wifiMan.getWifiState();
        wifiMan.setWifiEnabled(true);
        File fl = new File(FILE_ADDRESS_MAC);
        try {
            FileInputStream fin = new FileInputStream(fl);
            mac = convertStreamToString(fin);
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean enabled = WifiManager.WIFI_STATE_ENABLED == wifiState;
        wifiMan.setWifiEnabled(enabled);
        return mac;
    }

    private static String convertStreamToString(InputStream is) {
        Reader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            final StringBuilder out = new StringBuilder();
            for (; ; ) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0) {
                    break;
                }
                out.append(buffer, 0, rsz);
            }
            return out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static String convertPartnerId(String partnerid) {
        // TODO Auto-generated method stub
        int id = Integer.parseInt(partnerid);
        String partner = "other";
        switch (id) {
            case 1:
                partner = "TCL";
                break;
            case 2:
                partner = "KONKA";
                break;
            case 3:
                partner = "CHANGHONG";
                break;
            case 4:
                partner = "Domy";
                break;
            case 5:
                partner = "TOSHIBA";
                break;
            case 6:
                partner = "COOCAA";
                break;
            case 7:
                partner = "LETV";
                break;
            default:
                partner = "other";
                break;

        }
        return partner;
    }
}
