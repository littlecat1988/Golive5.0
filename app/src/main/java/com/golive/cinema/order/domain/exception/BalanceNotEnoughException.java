package com.golive.cinema.order.domain.exception;

/**
 * Created by Wangzj on 2016/10/24.
 */

public class BalanceNotEnoughException extends Exception {
    private final String productId;
    private final double price;
    private final double balance;

    public BalanceNotEnoughException(Throwable throwable, String productId, double price,
            double balance) {
        super(throwable);
        this.productId = productId;
        this.price = price;
        this.balance = balance;
    }

    public BalanceNotEnoughException(String productId, double price, double balance) {
        this.productId = productId;
        this.price = price;
        this.balance = balance;
    }

    public BalanceNotEnoughException(String detailMessage, String productId, double price,
            double balance) {
        super(detailMessage);
        this.productId = productId;
        this.price = price;
        this.balance = balance;
    }

    public BalanceNotEnoughException(String detailMessage, Throwable throwable,
            String productId, double price, double balance) {
        super(detailMessage, throwable);
        this.productId = productId;
        this.price = price;
        this.balance = balance;
    }

    public String getProductId() {
        return productId;
    }

    public double getPrice() {
        return price;
    }

    public double getBalance() {
        return balance;
    }
}
