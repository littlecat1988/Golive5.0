package com.golive.cinema;

import com.initialjie.log.Logger;
import com.squareup.leakcanary.AnalysisResult;
import com.squareup.leakcanary.DisplayLeakService;
import com.squareup.leakcanary.HeapDump;

/**
 * Created by Wangzj on 2017/1/4.
 */

public class LeakUploadService extends DisplayLeakService {
    @Override
    protected void afterDefaultHandling(HeapDump heapDump, AnalysisResult result, String leakInfo) {
        if (!result.leakFound || result.excludedLeak) {
            return;
        }
//        myServer.uploadLeakBlocking(heapDump.heapDumpFile, leakInfo);
        Logger.e("Memory leak : " + leakInfo);
    }
}