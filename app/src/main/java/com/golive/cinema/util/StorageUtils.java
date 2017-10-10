package com.golive.cinema.util;

import android.os.Environment;
import android.os.StatFs;

import com.initialjie.log.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

public class StorageUtils {
    private static final String SDCARD_ROOT = Environment
            .getExternalStorageDirectory().getAbsolutePath() + File.separator;

    private static final long LOW_STORAGE_THRESHOLD = 1024 * 1024 * 10;

    public static long getAvailableCapacity(String path) {
        if (null == path) {
            return 0;
        }
        try {
            StatFs stat = new StatFs(path);
            return ((long) stat.getAvailableBlocks() * (long) stat.getBlockSize());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public static long getTotalCapacity(String path) {
        if (null == path) {
            return 0;
        }
        try {
            StatFs stat = new StatFs(path);
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    /**
     * 根据路径，获取挂载点
     */
    public static String getMountPath(String filePath) {
        String mountPath = null;
        List<StorageInfo> storageInfos = StorageUtils.getStorageList();
        if (null != storageInfos) {
            filePath = filePath.toLowerCase();
            for (StorageInfo storageInfo : storageInfos) {
                String mount = storageInfo.path.toLowerCase();

                // 找到挂载点
                if (filePath.startsWith(mount)) {
                    mountPath = storageInfo.path;
                    break;
                }
            }
        }
        return mountPath;
    }

    public static List<StorageInfo> getStorageList() {
        List<StorageInfo> list = new ArrayList<>();
        // default sd path
        File storageDirectory = Environment.getExternalStorageDirectory();
        String def_path = storageDirectory != null ? storageDirectory.getPath() : "";
        boolean def_path_internal = !Environment.isExternalStorageRemovable();
        String def_path_state = Environment.getExternalStorageState();
        boolean def_path_readonly = def_path_state
                .equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        boolean def_path_available = def_path_state
                .equals(Environment.MEDIA_MOUNTED);
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream("/proc/mounts");
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            reader = new BufferedReader(isr);
            HashSet<String> paths = new HashSet<>();

            String line;
            int cur_display_number = 1;
            // Logger.d("/proc/mounts");
            while ((line = reader.readLine()) != null) {
                // Logger.d(line);

                // contain "/mnt", "/dev/block/vold"
                if (/* line.contains("vfat") || */line.contains("/mnt")
                        || line.contains("/dev/block/vold")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String device = tokens.nextToken(); // device
                    String mount_point = tokens.nextToken(); // mount point
                    if (paths.contains(mount_point)) {
                        continue;
                    }
                    String fileSystem = tokens.nextToken(); // file system
                    List<String> flags = Arrays.asList(tokens.nextToken()
                            .split(",")); // flags
                    boolean readonly = flags.contains("ro");

                    if (mount_point.equals(def_path)) {
                        paths.add(def_path);
                        StorageInfo storageInfo = new StorageInfo(device,
                                mount_point, fileSystem, true, readonly, -1);
                        // default sd path, add to first position
                        if (isPathAvailable(storageInfo.path)) {
                            list.add(0, storageInfo);
                        }
                    } else if (line.contains("/dev/block/vold")) {
                        if (!line.contains("/mnt/secure")
                                && !line.contains("/mnt/asec")
                                && !line.contains("/mnt/obb")
                                && !line.contains("/dev/mapper")
                                && !line.contains("tmpfs")) {
                            paths.add(mount_point);
                            StorageInfo storageInfo = new StorageInfo(device,
                                    mount_point, fileSystem, def_path_internal,
                                    readonly, cur_display_number++);
                            if (isPathAvailable(storageInfo.path)) {
                                list.add(storageInfo);
                            }
                        }
                    }
                }
            }

            if (!paths.contains(def_path) && def_path_available) {
                StorageInfo storageInfo = new StorageInfo(def_path, def_path,
                        "", true, def_path_readonly, -1);
                if (isPathAvailable(storageInfo.path)) {
                    list.add(0, storageInfo);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return list;
    }

    /**
     * 有些机型(康佳638)会报错Caused by: libcore.io.ErrnoException: statvfs failed: EACCES (Permission
     * denied)
     */
    public static boolean isPathAvailable(String path) {
        try {
            new StatFs(path);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 挂载为读写模式
     */
    public static boolean remountRW(String device) {
        Logger.d("remountRW, device : " + device);
        boolean retVal = false;
        Process suProcess;

        try {
            suProcess = Runtime.getRuntime().exec("mount -o remount " + device);
            int result = suProcess.waitFor();
            Logger.d("remountRW, result : " + result);

            if (255 != result) {
                retVal = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retVal;
    }

    public static class StorageInfo implements Serializable {

        public final String device;
        public final String path;
        public final String fileSystem;
        public final boolean internal;
        public final boolean readonly;
        public final int display_number;

        public StorageInfo(String device, String path, String fileSystem,
                boolean internal, boolean readonly, int display_number) {
            super();
            this.device = device;
            this.path = path;
            this.fileSystem = fileSystem;
            this.internal = internal;
            this.readonly = readonly;
            this.display_number = display_number;
        }

        public String getDisplayName() {
            StringBuilder res = new StringBuilder();
            if (internal) {
                res.append("Internal SD card");
            } else if (display_number > 1) {
                res.append("SD card ").append(display_number);
            } else {
                res.append("SD card");
            }
            if (readonly) {
                res.append(" (Read only)");
            }
            return res.toString();
        }
    }
}
