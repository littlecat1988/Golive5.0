package com.golive.cinema.player;

/**
 * 播放鉴权回调
 *
 * @author wangzijie E-mail:initialjie90@gmail.com
 * @version V1.0
 * @Title: PlayerValidateCallback.java
 * @Package com.golive.player.callback
 * @Description: TODO
 * @date 2015年5月11日 下午2:06:05
 */
public interface PlayerValidateCallback {
    /**
     * 播放鉴权
     *
     * @return 返回true表示能够播放，否则表示不能播放
     */
    boolean onValidate();

    /**
     * 播放过期
     */
    void onOverdue();
}
