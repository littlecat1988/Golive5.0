package com.golive.cinema;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.config.ACRAConfiguration;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderFactory;

/**
 * Created by Wangzj on 2016/12/16.
 */

public class CrashSenderFactory implements ReportSenderFactory {
    @NonNull
    @Override
    public ReportSender create(@NonNull Context context, @NonNull ACRAConfiguration config) {
        return new CrashReportSender();
    }
}
