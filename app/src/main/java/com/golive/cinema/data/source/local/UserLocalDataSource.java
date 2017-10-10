package com.golive.cinema.data.source.local;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.content.Context;
import android.support.annotation.NonNull;

import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.AllAgreement;
import com.golive.network.entity.ClientService;
import com.golive.network.entity.CreditOperation;
import com.golive.network.entity.EditHistoryResult;
import com.golive.network.entity.FinanceMessage;
import com.golive.network.entity.Login;
import com.golive.network.entity.Response;
import com.golive.network.entity.UserHead;
import com.golive.network.entity.UserInfo;
import com.golive.network.entity.VipMonthlyResult;
import com.golive.network.entity.Wallet;
import com.golive.network.entity.WalletOperationItem;
import com.golive.network.entity.WechatInfo;
import com.golive.network.response.ExitGuideResponse;
import com.golive.network.response.HistoryResponse;
import com.golive.network.response.RecommendComboResponse;
import com.golive.network.response.TopupRechargeResponse;
import com.golive.network.response.VipComboResponse;

import java.util.List;

import rx.Observable;

/**
 * Created by Wangzj on 2016/9/23.
 */

public class UserLocalDataSource implements UserDataSource {

    private static UserLocalDataSource INSTANCE;

    private UserLocalDataSource(@NonNull Context context,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        checkNotNull(context, "context cannot be null");
        checkNotNull(schedulerProvider, "scheduleProvider cannot be null");
    }

    public static UserLocalDataSource getInstance(@NonNull Context context,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        if (INSTANCE == null) {
            INSTANCE = new UserLocalDataSource(context, schedulerProvider);
        }
        return INSTANCE;
    }

    @Override
    public Observable<Login> login(@NonNull String userid, @NonNull String pw,
            @NonNull String status, @NonNull String branchtype, @NonNull String kdmVersion,
            @NonNull String kdmPlatform) {
        return Observable.empty();
    }

    @Override
    public void refreshLogin() {
        // Not required because the {@link UserRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    @Override
    public Observable<Response> logout() {
        return Observable.empty();
    }

    @Override
    public Observable<UserInfo> getUserInfo() {
        return Observable.empty();
    }

    @Override
    public void refreshUserInfo() {

    }

    @Override
    public Observable<Wallet> getWallet() {
        return Observable.empty();
    }

    @Override
    public Observable<Wallet> getCreditWallet() {
        return Observable.empty();
    }

    @Override
    public void refreshWallet() {
        // Not required because the {@link UserRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    @Override
    public void refreshCreditWallet() {

    }

    /*
        @Override
        public Observable<List<VipPackage>> GetVipPackageList() {
            return Observable.empty();
        }

        @Override
        public Observable<List<Order>> GetHistoryList(@NonNull String productId,
                @NonNull String productType, @NonNull String beginTime, @NonNull String endTime) {
            return Observable.empty();
        }*/
    @Override
    public void redoGetHistoryList() {
    }

    @Override
    public Observable<EditHistoryResult> DeleteHistory(@NonNull String serial,
            @NonNull String title) {
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

    /*
    @Override
    public  Observable<List<TopupOnePrice>> GetTopupPriceList (){
        return Observable.empty();
    }*/
    //新接口
    @Override
    public Observable<TopupRechargeResponse> GetTopupPriceListNews() {
        return Observable.empty();
    }


    @Override
    public Observable<VipComboResponse> GetVipPackageListNews() {
        return Observable.empty();
    }

    @Override
    public Observable<VipMonthlyResult> getVipMonthlyStatus(String id, String type) {
        return Observable.empty();
    }

    @Override
    public void redoGetVipPackageList() {
    }


    @Override
    public Observable<HistoryResponse> GetHistoryListNews(@NonNull String productId,
            @NonNull String productType, @NonNull String beginTime, @NonNull String endTime) {
        return Observable.empty();
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

    @Override
    public Observable<FinanceMessage> queryUserFinanceMessage() {
        return Observable.empty();
    }

    @Override
    public void redoQueryUserFinanceMessage() {
    }

    @Override
    public Observable<RecommendComboResponse> queryRecommendComboInfo() {
        return Observable.empty();
    }

    @Override
    public void refreshRecommendCombo() {
    }

    @Override
    public Observable<AllAgreement> getAgreement(String type) {
        return Observable.empty();
    }

    @Override
    public Observable<ExitGuideResponse> queryExitGuideResponse(@NonNull String encryptionType) {
        return Observable.empty();
    }
}
