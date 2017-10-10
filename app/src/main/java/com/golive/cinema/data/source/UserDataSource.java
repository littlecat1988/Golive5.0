package com.golive.cinema.data.source;

import android.support.annotation.NonNull;

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
import com.golive.network.response.SpecialDetailResponse;
import com.golive.network.response.TopupRechargeResponse;
import com.golive.network.response.VipComboResponse;

import java.util.List;

import rx.Observable;

/**
 * Main entry point for accessing user data.
 * <p>
 * Created by Wangzj on 2016/9/23.
 */
public interface UserDataSource {

    /**
     * Login
     *
     * @return An {@link rx.Observable} which will emit a {@link Login}.
     */
    Observable<Login> login(@NonNull String userid, @NonNull String pw, @NonNull String status,
            @NonNull String branchtype, @NonNull String kdmVersion, @NonNull String kdmPlatform);

    /**
     * Force to refresh the cached login data.
     */
    void refreshLogin();

    /**
     * Logout
     *
     * @return An {@link rx.Observable} which will emit a {@link Response}.
     */
    Observable<Response> logout();

    /**
     * Get user info.
     *
     * @return An {@link rx.Observable} which will emit a {@link UserInfo}.
     */
    Observable<UserInfo> getUserInfo();

    /**
     * Force to refresh the cached user info.
     */
    void refreshUserInfo();

    /**
     * Get user wallet.
     *
     * @return An {@link rx.Observable} which will emit a {@link Wallet}.
     */
    Observable<Wallet> getWallet();

    /**
     * Get user credit wallet.
     *
     * @return An {@link rx.Observable} which will emit a {@link Wallet}.
     */
    Observable<Wallet> getCreditWallet();

    /**
     * Force to refresh the cached wallet data.
     */
    void refreshWallet();

    /**
     * Force to refresh the cached credit wallet data.
     */
    void refreshCreditWallet();

    /**
     * Get Vip Package List.
     *
     * @return An {@link rx.Observable} which will emit a {@link Wallet}.
     */
//    Observable<List<VipPackage>> GetVipPackageList();
    Observable<VipComboResponse> GetVipPackageListNews();//新接口

    /**
     * 获取连续包月状态
     */
    Observable<VipMonthlyResult> getVipMonthlyStatus(String id, String type);

    void redoGetVipPackageList();

    /*
        Observable<List<Order>> GetHistoryList(@NonNull String productId,
                                                      @NonNull String productType, @NonNull
                                                      String beginTime, @NonNull String endTime);*/
    void redoGetHistoryList();

    /**
     * Get delete history.
     *
     * @return An {@link rx.Observable} which will emit a {@link Wallet}.
     */
    Observable<EditHistoryResult> DeleteHistory(@NonNull String serial, String title);

    /**
     * Get add history.
     *
     * @return An {@link rx.Observable} which will emit a {@link Wallet}.
     */
    Observable<EditHistoryResult> AddHistory(@NonNull String serial);

    /**
     * Get add history.
     */
    Observable<WechatInfo> GetWechatInfo();

    /**
     * Get ClientService
     */
    Observable<ClientService> GetClientService();

    /**
     * Get GetUserHead
     */
    Observable<UserHead> GetUserHead();

    /**
     * Get get wallet operations.
     *
     * @return An {@link rx.Observable} which will emit a {@link Wallet}.
     */
    Observable<List<WalletOperationItem>> GetWalletOperationList();

    void redoGetWalletOperationList();

    /**
     * Get TopupPriceList.
     *
     * @return An {@link rx.Observable} which will emit a {@link Wallet}.
     */
//    Observable<List<TopupOnePrice>> GetTopupPriceList();
    Observable<TopupRechargeResponse> GetTopupPriceListNews();// 新接口

    /**
     * 新接口 Get History List.
     *
     * @return The the orders of the product range from beginTime to endTime if exist.
     */
    Observable<HistoryResponse> GetHistoryListNews(@NonNull String productId,
            @NonNull String productType, @NonNull String beginTime, @NonNull String endTime);


    /***
     * 用户MAC重复验证后登录
     */
    Observable<Login> loginAgain(@NonNull String userid, @NonNull String pw,
            @NonNull String phone,
            @NonNull String verifyCode,
            @NonNull String status,
            @NonNull String logontype,
            @NonNull String branchtype,
            @NonNull String kdmVersion,
            @NonNull String kdmPlatform);

    /**
     * 查询用户信用支付账单
     */
    Observable<CreditOperation> queryUserCreditOperation();

    void redoUserCreditOperation();

    /**
     * 查询用户理财信息
     */
    Observable<FinanceMessage> queryUserFinanceMessage();

    void redoQueryUserFinanceMessage();

    /**
     * 获取用户引流推荐套餐
     */
    Observable<RecommendComboResponse> queryRecommendComboInfo();

    void refreshRecommendCombo();

    /**
     * 获取付费服务协议
     *
     * @param type 1 付费服务协议; 2 连续包月声明
     */
    Observable<AllAgreement> getAgreement(String type);
    /**
     * 退出引流
     * @return
     */
    Observable<ExitGuideResponse> queryExitGuideResponse(@NonNull final String encryptionType);
}
