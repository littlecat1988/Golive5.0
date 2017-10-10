package com.golive.cinema.util;

import java.io.File;
import java.io.IOException;

import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by Wangzj on 2016/11/29.
 */

public class OkHttpUtils {

    /**
     * Write response body to file
     */
    public static void write2File(ResponseBody responseBody, File file) {
        BufferedSource source = null;
        BufferedSink sink = null;
        try {
            sink = Okio.buffer(Okio.appendingSink(file));
            source = responseBody.source();
            sink.writeAll(source);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (source != null) {
                try {
                    source.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sink != null) {
                try {
                    sink.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
