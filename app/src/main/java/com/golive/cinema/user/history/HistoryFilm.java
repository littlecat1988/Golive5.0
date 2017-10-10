package com.golive.cinema.user.history;


import com.golive.cinema.util.StringUtils;

public class HistoryFilm {

    private String name;
    private String orderstatus;
    private String payType = "21";// 支付类型 21：在线播 22：下载
    private String valiTime = "48h";// 有效时间 remain（毫秒）
    private String buyTime;// 购买时间
    private String start_time; // 播放开始时间
    private String expirationTime; // 过期日期
    private String expired; // 是否过期 true/flase
    private String orderSerial;// 订单号
    private String orderMediaId = "";//订单对应的资源号
    private String kdmTypeMode = "0";//0不是kdm,21kdm在线 ,22kdm下载
    private String active = "1";//是否下架
    private String bigcover; // 大海报
    private String filmid; // 影片ID

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getValiTime() {
        return valiTime;
    }

    public void setValiTime(String valiTime) {
        this.valiTime = valiTime;
    }

    public String getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(String expirationTime) {
        this.expirationTime = expirationTime;
    }

    public String getBuyTime() {
        return buyTime;
    }

    public void setBuyTime(String buyTime) {
        this.buyTime = buyTime;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrderstatus() {
        return orderstatus;
    }

    public void setOrderstatus(String orderstatus) {
        this.orderstatus = orderstatus;
    }

    public String getOrderSerial() {
        return orderSerial;
    }

    public void setOrderSerial(String orderSerial) {
        this.orderSerial = orderSerial;
    }

    public String getOrderMediaId() {
        return orderMediaId;
    }

    public void setOrderMediaId(String orderMedia) {
        this.orderMediaId = orderMedia;
    }

    public String getExpired() {
        return expired;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }

    public String getKdmTypeMode() {
        return kdmTypeMode;
    }

    public void setKdmTypeMode(String val) {
        this.kdmTypeMode = val;
    }

    public String getActive() {
        return this.active;
    }

    public void setActive(String value) {
        this.active = value;
    }

    public String getBigcover() {
        return bigcover;
    }

    public void setBigcover(String bigcover) {
        this.bigcover = bigcover;
    }

    public String getFilmid() {
        return filmid;
    }

    public void setFilmid(String filmid) {
        this.filmid = filmid;
    }

    public boolean isTimeOut() {
        return !StringUtils.isNullOrEmpty(valiTime) && "0".equals(valiTime);
    }

    public boolean isKdmFilm() {
        return !StringUtils.isNullOrEmpty(valiTime) && "--".equals(valiTime);
    }

    public boolean isOffLine() {
        return !StringUtils.isNullOrEmpty(valiTime) && "0".equals(active);//0-已下架;1-上架
    }
}
