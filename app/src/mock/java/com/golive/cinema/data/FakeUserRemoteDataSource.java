package com.golive.cinema.data;

import android.support.annotation.NonNull;

import com.golive.cinema.data.source.UserDataSource;
import com.golive.network.entity.ClientService;
import com.golive.network.entity.CreditOperation;
import com.golive.network.entity.EditHistoryResult;
import com.golive.network.entity.Error;
import com.golive.network.entity.FinanceMessage;
import com.golive.network.entity.Login;
import com.golive.network.entity.Order;
import com.golive.network.entity.Response;
import com.golive.network.entity.TopupOnePrice;
import com.golive.network.entity.UserHead;
import com.golive.network.entity.UserInfo;
import com.golive.network.entity.VipPackage;
import com.golive.network.entity.Wallet;
import com.golive.network.entity.WalletOperationItem;
import com.golive.network.entity.WechatInfo;
import com.golive.network.response.HistoryResponse;
import com.golive.network.response.RecommendComboResponse;
import com.golive.network.response.TopupRechargeResponse;
import com.golive.network.response.VipComboResponse;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by Wangzj on 2016/9/26.
 */

public class FakeUserRemoteDataSource implements UserDataSource {

    private static FakeUserRemoteDataSource INSTANCE;

    private final Login mLogin;
    private final Response mResponse;
    private final UserInfo mUserInfo;
    private final Wallet mWallet;
    private final List<VipPackage> mVipPackageList;
    private final List<TopupOnePrice> mTopupOnePriceList;
    private final List<Order> mHistoryOrders;

    private FakeUserRemoteDataSource() {
        Error pError = new Error();
        pError.setType("false");
        mLogin = new Login();
        mLogin.setError(pError);
        mResponse = new Response();
        mResponse.setError(pError);
        mUserInfo = new UserInfo();
        mWallet = new Wallet();
        mWallet.setValue("10");
        mWallet.setCreditLine("20");
        mWallet.setCreditDeadLineDays("180");
        mWallet.setCreditRemain("18000");
        mWallet.setCurrency("RMB");
        mVipPackageList = new ArrayList<>();
        mTopupOnePriceList = new ArrayList<>();
        mHistoryOrders = new ArrayList<>();
    }

    public static FakeUserRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeUserRemoteDataSource();
        }
        return INSTANCE;
    }

    @Override
    public Observable<Login> login(@NonNull String userid, @NonNull String pw,
            @NonNull String status, @NonNull String branchtype, @NonNull String kdmVersion,
            @NonNull String kdmPlatform) {
        return Observable.just(mLogin);
    }

    @Override
    public void refreshLogin() {

    }

    @Override
    public Observable<Response> logout() {
        return Observable.just(mResponse);
    }

    @Override
    public Observable<UserInfo> getUserInfo() {
        return Observable.just(mUserInfo);
    }

    @Override
    public void refreshUserInfo() {

    }

    @Override
    public Observable<Wallet> getWallet() {
        return Observable.just(mWallet);
    }

    @Override
    public Observable<Wallet> getCreditWallet() {
        return Observable.just(mWallet);
    }

    @Override
    public void refreshWallet() {

    }

    @Override
    public void refreshCreditWallet() {

    }

    @Override
    public Observable<VipComboResponse> GetVipPackageListNews() {
        return Observable.empty();
    }

    @Override
    public void redoGetVipPackageList() {

    }

    @Override
    public void redoGetHistoryList() {

    }

    @Override
    public Observable<EditHistoryResult> DeleteHistory(@NonNull String serial, String title) {
        return Observable.empty();
    }

    @Override
    public Observable<EditHistoryResult> AddHistory(@NonNull String serial) {
        return Observable.empty();
    }

    @Override
    public Observable<WechatInfo> GetWechatInfo() {
        return Observable.empty();
    }

    @Override
    public Observable<ClientService> GetClientService() {
        return Observable.empty();
    }

    @Override
    public Observable<UserHead> GetUserHead() {
        return Observable.empty();
    }

    @Override
    public Observable<List<WalletOperationItem>> GetWalletOperationList() {
        return Observable.empty();
    }

    @Override
    public void redoGetWalletOperationList() {
    }

    @Override
    public Observable<TopupRechargeResponse> GetTopupPriceListNews() {
        return null;
    }

    @Override
    public Observable<HistoryResponse> GetHistoryListNews(@NonNull String productId,
            @NonNull String productType, @NonNull String beginTime, @NonNull String endTime) {
        return null;
    }

    @Override
    public Observable<Login> loginAgain(@NonNull String userid, @NonNull String pw,
            @NonNull String phone, @NonNull String verifyCode, @NonNull String status,
            @NonNull String logontype, @NonNull String branchtype, @NonNull String kdmVersion,
            @NonNull String kdmPlatform) {
        return Observable.empty();
    }

    @Override
    public Observable<CreditOperation> queryUserCreditOperation() {
        return Observable.empty();
    }

    @Override
    public void redoUserCreditOperation() {
    }

    ;

    @Override
    public Observable<FinanceMessage> queryUserFinanceMessage() {
        return Observable.empty();
    }

    @Override
    public void redoQueryUserFinanceMessage() {
    }

    ;

    @Override
    public Observable<RecommendComboResponse> queryRecommendComboInfo() {
        return null;
    }
}
