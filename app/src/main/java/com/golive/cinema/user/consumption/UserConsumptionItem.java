package com.golive.cinema.user.consumption;

/**
 * Created by Mowl on 2016/11/3.
 */


public class UserConsumptionItem {
    private String name;
    private String time;
    private String price;
    private String type;//0:充值 1 购买频道 21\22购买影片 3..
    private String payDetail;
    private String isCredit = "0";

    public UserConsumptionItem(String name, String time, String price, String type) {
        this.name = name;
        this.time = time;
        this.price = price;
        this.type = type;
    }

    public UserConsumptionItem() {

    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPayPrice() {
        return price;
    }

    public void setPayPrice(String price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String price) {
        this.type = price;
    }

    public String getPayDetail() {
        return payDetail;
    }

    public void setPayDetail(String value) {
        this.payDetail = value;
    }

    public String getCredit() {
        return isCredit;
    }

    public void setCredit(String value) {
        this.isCredit = value;
    }

}
