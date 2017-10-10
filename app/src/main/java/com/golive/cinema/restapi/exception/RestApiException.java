package com.golive.cinema.restapi.exception;

import com.golive.cinema.util.StringUtils;

/**
 * Created by Wangzj on 2016/10/24.
 */

public class RestApiException extends Exception {
    private final String mType;

    private final String mNote;

    private final String mNoteMsg;

    private final String mServertime;

    private Object mObject;

    private String mMessage;

    public RestApiException(String type, String note, String noteMsg, String servertime) {
        mType = type;
        mNote = note;
        mNoteMsg = noteMsg;
        mServertime = servertime;
    }

    public RestApiException(String detailMessage, String type, String note, String noteMsg,
            String servertime) {
        super(detailMessage);
        mType = type;
        mNote = note;
        mNoteMsg = noteMsg;
        mServertime = servertime;
    }

    public RestApiException(String detailMessage, Throwable throwable, String type, String note,
            String noteMsg, String servertime) {
        super(detailMessage, throwable);
        mType = type;
        mNote = note;
        mNoteMsg = noteMsg;
        mServertime = servertime;
    }

    public RestApiException(Throwable throwable, String type, String note, String noteMsg,
            String servertime) {
        super(throwable);
        mType = type;
        mNote = note;
        mNoteMsg = noteMsg;
        mServertime = servertime;
    }

    @Override
    public String toString() {
        if (null == mMessage) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getNote());
            String noteMsg = getNoteMsg();
            if (!StringUtils.isNullOrEmpty(noteMsg)) {
                stringBuilder.append(", ");
                stringBuilder.append(noteMsg);
            }
            mMessage = stringBuilder.toString();
        }

        return mMessage;
    }

    @Override
    public String getMessage() {
        return toString();
    }

    public String getType() {
        return mType;
    }

    public String getNote() {
        return mNote;
    }

    public String getNoteMsg() {
        return mNoteMsg;
    }

    public String getServertime() {
        return mServertime;
    }

    public Object getObject() {
        return mObject;
    }

    public void setObject(Object object) {
        mObject = object;
    }
}
