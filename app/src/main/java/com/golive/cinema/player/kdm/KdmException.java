package com.golive.cinema.player.kdm;

import com.golive.player.kdm.KDMResCode;

/**
 * An exception used for describing KDM's error.
 *
 * @author Wangzj E-mail:initialjie90@gmail.com
 * @version V1.0
 * @Title KdmException.java
 * @Package com.golive.player.kdm.entity
 * @Description TODO
 * @date 2016年3月30日 下午4:37:39
 */
public class KdmException extends Exception {

    private final KDMResCode mKdmResCode;

    public KdmException(KDMResCode pKdmResCode) {
        mKdmResCode = pKdmResCode;
    }

    public KDMResCode getKdmResCode() {
        return mKdmResCode;
    }

    @Override
    public String getMessage() {
//        return super.getMessage();
        return "Kdm exception, errCode : " + mKdmResCode.getErrno();
    }
}
