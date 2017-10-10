package com.golive.cinema.player.domain.model;

/**
 * The valid period of playback.
 * <p>
 * Created by Wangzj on 2016/9/21.
 */

public class PlaybackValidity {

    public static final int ERR_OK = 0;

    public static final int ERR_UNKNOWN = -1;
    public static final int ERR_OVERDUE = -2;
    public static final int ERR_NO_VALID_ORDER = -3;
    public static final int ERR_TYPE_NOT_SUPPORT = -4;

    public static final int ERR_MEDIAINFO_NOT_FOUND = -10;

    /* Download error */
    public static final int ERR_DOWNLOAD_NO_TASK = -20;
    public static final int ERR_DOWNLOAD_FILE_NOT_FOUND = -21;
    public static final int ERR_DOWNLOAD_NOT_FINISH = -22;

    /** Whether the playback is unlimited */
    private final boolean mIsUnlimited;

    /**
     * Whether within validity of the playback
     * <p/>
     * Only meaningful when {@code mIsUnlimited} is <code>false<code/>.
     */
    private final boolean mIsValid;

    /**
     * Error code. Only meaningful when {@code isValid} is <code>false<code/>.
     */
    private int mErrorCode = ERR_OK;

    /**
     * Remainder times of the playback.
     * <p/>
     * Only meaningful when {@code mIsUnlimited} is <code>false<code/>.
     */
    private final int mRemainTimes;

    /**
     * Remainder valid period.
     * <p/>
     * Only meaningful when {@code mIsUnlimited} is <code>false<code/>
     */
    private final long mRemainValidity;

    /**
     * The url to play.
     * <p/>
     * Only meaningful when {@code mIsUnlimited} or {@code mIsValid} is <code>true<code/>
     */
    private final String mPlayUrl;

    /**
     * @param isUnlimited    Whether the playback is unlimited
     * @param isValid        Whether within validity of the playback. Only meaningful when
     *                       {@code isUnlimited} is <code>false<code/>.
     * @param errorCode      Error code. Only meaningful when {@code isValid} is     *
     *                       <code>false<code/>.
     * @param remainTimes    Remainder times of the playback. Only meaningful when {@code      *
     *                       isUnlimited} is <code>false<code/>.
     * @param remainValidity Remainder valid period. Only meaningful when {@code isUnlimited} is
     *                       <code>false<code/>.
     * @param playUrl        The url to play. Only meaningful when {@code isUnlimited} or {@code
     *                       isValid} is <code>true<code/>
     */
    public PlaybackValidity(boolean isUnlimited, boolean isValid, int errorCode, int remainTimes,
            long remainValidity, String playUrl) {
        mIsUnlimited = isUnlimited;
        mPlayUrl = playUrl;

        if (isUnlimited) {
            mIsValid = true;
        } else {
            mIsValid = isValid;
        }

        mErrorCode = errorCode;

        mRemainTimes = remainTimes;
        mRemainValidity = remainValidity;
    }

    public static PlaybackValidity generatePlaybackNotValid(int errorCode) {
        return new PlaybackValidity(false, false, errorCode, 0, 0, null);
    }

    public boolean isUnlimited() {
        return mIsUnlimited;
    }

    public boolean isValid() {
        return mIsValid;
    }

    public int getRemainTimes() {
        return mRemainTimes;
    }

    public long getRemainValidity() {
        return mRemainValidity;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public String getPlayUrl() {
        return mPlayUrl;
    }
}
