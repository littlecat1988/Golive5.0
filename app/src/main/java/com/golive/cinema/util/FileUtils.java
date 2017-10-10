package com.golive.cinema.util;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * @author wangzijie E-mail:initialjie90@gmail.com
 * @version V1.0
 * @Title FileUtils.java
 * @Package com.initialjie.download.util
 * @Description TODO
 * @date 2015年8月31日 上午10:27:48
 */
public class FileUtils {

    /**
     * Deletes the given folder and all its files / subfolders. Is not
     * implemented in a recursive way. The "Recursively" in the name stems from
     * the filesystem command
     *
     * @param root The folder to delete recursively
     */
    public static void deleteRecursively(final File root) {
        LinkedList<File> deletionQueue = new LinkedList<>();
        deletionQueue.add(root);

        while (!deletionQueue.isEmpty()) {
            final File toDelete = deletionQueue.removeFirst();
            final File[] children = toDelete.listFiles();
            if (null == children || 0 == children.length) {
                // This is either a file or an empty directory -> deletion
                // possible
                toDelete.delete();
            } else {
                // Add the children before the folder because they have to be
                // deleted first
                deletionQueue.addAll(Arrays.asList(children));
                // Add the folder again because we can't delete it yet.
                deletionQueue.addLast(toDelete);
            }
        }
    }
}
