package com.golive.cinema.data.source;


import static com.golive.cinema.util.Preconditions.checkNotNull;

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
import com.golive.network.response.TopupRechargeResponse;
import com.golive.network.response.VipComboResponse;

import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/9/23.
 */

public class UserRepository implements UserDataSource {

    private static UserRepository INSTANCE = null;

    private final UserDataSource mRemoteDataSource;
    private final UserDataSource mLocalDataSource;

    private boolean mLoginCacheIsDirty;
    private boolean mUserInfoCacheIsDirty;
    private boolean mWalletCacheIsDirty;
    private boolean mRecommendComboCacheIsDirty;
    private boolean mRedoHistoryList;
    private boolean mRedoVipPackageList;
    private boolean mRedoWalletOperationList;
    private boolean mCreditWalletCacheIsDirty;
    private boolean mIsRedoUserCreditOperation;
    private boolean mIsRedoUserFinanceMessage;
    private String mUserId = "";
    private String mPassword = "";
    private String mUserStatus = "";
    private String mBranchtype = "";
    private String mKdmVersion = "";
    private String mKdmPlatform = "";
    private Login mCachedLogin;
    private UserInfo mUserInfo;
    private WechatInfo mWechatInfo;
    private ClientService mClientService;
    private Wallet mWallet;
    private Wallet mCreditWallet;
    private UserHead mUserHead;
    private List<WalletOperationItem> mWalletOperationList;
    private HistoryResponse mHistoryResponse;
    private TopupRechargeResponse mTopupRechargeResponse;
    private VipComboResponse mVipComboResponse;
    private CreditOperation mCreditOperation;
    private FinanceMessage mFinanceMessage;
    private HashMap<String, AllAgreement> mAgreementCachedMap;
    private RecommendComboResponse mRecommendComboCache;
    private Observable<RecommendComboResponse> mRemoteTask;
    private Observable<UserInfo> mGetUserInfoObs;
    private Observable<Wallet> mGetWalletObs;
    private Observable<Wallet> mGetCreditWalletObs;

    private UserRepository(@NonNull UserDataSource remoteDataSource,
            @NonNull UserDataSource localDataSource) {
        this.mRemoteDataSource = checkNotNull(remoteDataSource);
        this.mLocalDataSource = checkNotNull(localDataSource);
//        mAgreementCachedMap = new HashMap<>();
    }

    public static UserRepository getInstance(UserDataSource remoteDataSource,
            UserDataSource localDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new UserRepository(remoteDataSource, localDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(UserDataSource, UserDataSource)} to create a new
     * instance next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Observable<Login> login(@NonNull final String userid, @NonNull final String pw,
            @NonNull final String status, @NonNull final String branchtype,
            @NonNull final String kdmVersion, @NonNull final String kdmPlatform) {

        checkNotNull(userid);
        checkNotNull(pw);
        checkNotNull(status);
        checkNotNull(branchtype);
        checkNotNull(kdmVersion);
        checkNotNull(kdmPlatform);

        boolean isLoginCacheDirty =
                isLoginCacheDirty(userid, pw, status, branchtype, kdmVersion, kdmPlatform);

        if (!isLoginCacheDirty && !mLoginCacheIsDirty && mCachedLogin != null) {
            return Observable.just(mCachedLogin);
        }

        return mRemoteDataSource.login(userid, pw, status, branchtype, kdmVersion, kdmPlatform)
                .first()
                .doOnNext(new Action1<Login>() {
                    @Override
                    public void call(Login login) {
                        mCachedLogin = login;
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        mLoginCacheIsDirty = false;

                        mUserId = userid;
                        mPassword = pw;
                        mUserStatus = status;
                        mBranchtype = branchtype;
                        mKdmVersion = kdmVersion;
                        mKdmPlatform = kdmPlatform;
                    }
                });
    }

    @Override
    public void refreshLogin() {
        mLoginCacheIsDirty = true;
    }

    @Override
    public Observable<Response> logout() {
        refreshLogin();
        return mRemoteDataSource.logout();
    }

    @Override
    public Observable<UserInfo> getUserInfo() {
        if (!mLoginCacheIsDirty && !mUserInfoCacheIsDirty && mUserInfo != null) {
            return Observable.just(mUserInfo);
        }

        /*
        * 1. Firstly login if need.
        * 2. Then, get the user info.*/
        // step1
        return loginInternal()
                // step2
                .flatMap(
                        new Func1<Login, Observable<UserInfo>>() {
                            @Override
                            public Observable<UserInfo> call(Login login) {
                                return getUserInfoInternal();
                            }
                        })
                .doOnNext(new Action1<UserInfo>() {
                    @Override
                    public void call(UserInfo userInfo) {
                        mUserInfo = userInfo;
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        mUserInfoCacheIsDirty = false;
                    }
                });
    }

    private Observable<UserInfo> getUserInfoInternal() {
        if (null == mGetUserInfoObs) {
            synchronized (this) {
                if (null == mGetUserInfoObs) {
                    mGetUserInfoObs = mRemoteDataSource.getUserInfo()
                            .replay(1)
                            .refCount();
                }
            }
        }
        return mGetUserInfoObs;
    }

    @Override
    public void refreshUserInfo() {
        mUserInfoCacheIsDirty = true;
    }

    @Override
    public Observable<Wallet> getWallet() {
        if (!mWalletCacheIsDirty && mWallet != null) {
            return Observable.just(mWallet);
        }

        /*
        * 1. Firstly login if need.
        * 2. Then, get the wallet info.*/
        // step1
        return loginInternal()
                // step2
                .flatMap(
                        new Func1<Login, Observable<Wallet>>() {
                            @Override
                            public Observable<Wallet> call(Login login) {
                                return getWalletInternal();
                            }
                        })
                .doOnNext(new Action1<Wallet>() {
                    @Override
                    public void call(Wallet wallet) {
                        mWallet = wallet;
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        mWalletCacheIsDirty = false;
                    }
                });
    }

    private Observable<Wallet> getWalletInternal() {
        if (null == mGetWalletObs) {
            synchronized (this) {
                if (null == mGetWalletObs) {
                    mGetWalletObs = mRemoteDataSource.getWallet()
                            .replay(1)
                            .refCount();
                }
            }
        }
        return mGetWalletObs;
    }

    @Override
    public Observable<Wallet> getCreditWallet() {
        if (!mCreditWalletCacheIsDirty && mCreditWallet != null) {
            return Observable.just(mCreditWallet);
        }

        /*
        * 1. Firstly login if need.
        * 2. Then, get the wallet info.*/
        // step1
        return loginInternal()
                // step2
                .flatMap(
                        new Func1<Login, Observable<Wallet>>() {
                            @Override
                            public Observable<Wallet> call(Login login) {
                                return getCreditWalletInternal();
                            }
                        })
                .doOnNext(new Action1<Wallet>() {
                    @Override
                    public void call(Wallet wallet) {
                        mCreditWallet = wallet;
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        mCreditWalletCacheIsDirty = false;
                    }
                });
    }

    private Observable<Wallet> getCreditWalletInternal() {
        if (null == mGetCreditWalletObs) {
            synchronized (this) {
                if (null == mGetCreditWalletObs) {
                    mGetCreditWalletObs = mRemoteDataSource.getCreditWallet()
                            .replay(1)
                            .refCount();
                }
            }
        }
        return mGetCreditWalletObs;
    }

    @Override
    public void refreshWallet() {
        mWalletCacheIsDirty = true;
    }

    @Override
    public void refreshCreditWallet() {
        mCreditWalletCacheIsDirty = true;
    }

    private boolean isLoginCacheDirty(@NonNull String userid, @NonNull String pw,
            @NonNull String status, @NonNull String branchtype, @NonNull String kdmVersion,
            @NonNull String kdmPlatform) {

        checkNotNull(userid);
        checkNotNull(pw);
        checkNotNull(status);
        checkNotNull(branchtype);
        checkNotNull(kdmVersion);
        checkNotNull(kdmPlatform);

        return !userid.equals(mUserId) || !status.equals(mPassword) || !branchtype.equals(
                mBranchtype) || !kdmVersion.equals(mKdmVersion)
                || !kdmPlatform.equals(mKdmPlatform);
    }

    @Override
    public void redoGetHistoryList() {
        mRedoHistoryList = true;
    }

    /**
     * Add History.
     */
    @Override
    public Observable<EditHistoryResult> AddHistory(@NonNull String serial) {
        checkNotNull(serial);
        return mRemoteDataSource.AddHistory(serial);
    }

    /**
     * Get History List.
     */
    @Override
    public Observable<EditHistoryResult> DeleteHistory(@NonNull String serial, String title) {
        checkNotNull(serial);

        return mRemoteDataSource.DeleteHistory(serial, title)
                .doOnNext(new Action1<EditHistoryResult>() {
                    @Override
                    public void call(EditHistoryResult editHistoryResult) {
                    }
                });
    }

    /**
     * Get Wechat Public Info
     */
    @Override
    public Observable<WechatInfo> GetWechatInfo() {

        if (mWechatInfo != null) {
            return Observable.just(mWechatInfo);
        }

        return mRemoteDataSource.GetWechatInfo()
                .doOnNext(new Action1<WechatInfo>() {
                    @Override
                    public void call(WechatInfo wechatInfo) {
                        mWechatInfo = wechatInfo;
                    }
                });
    }

    /**
     * Get Wechat Public Info
     */
    @Override
    public Observable<ClientService> GetClientService() {

        if (mClientService != null) {
            return Observable.just(mClientService);
        }

        return mRemoteDataSource.GetClientService()
                .doOnNext(new Action1<ClientService>() {
                    @Override
                    public void call(ClientService info) {
                        mClientService = info;
                    }
                });
    }

    /**
     * Get Wechat Public Info
     */
    @Override
    public Observable<UserHead> GetUserHead() {

        if (mUserHead != null) {
            return Observable.just(mUserHead);
        }

        return mRemoteDataSource.GetUserHead()
                .doOnNext(new Action1<UserHead>() {
                    @Override
                    public void call(UserHead info) {
                        mUserHead = info;
                    }
                });
    }

    /**
     * get wallet operations.
     */
    @Override
    public Observable<List<WalletOperationItem>> GetWalletOperationList() {
        if (mWalletOperationList != null && !mRedoWalletOperationList) {
            return Observable.just(mWalletOperationList);
        }

        return mRemoteDataSource.GetWalletOperationList()
                .doOnNext(new Action1<List<WalletOperationItem>>() {
                    @Override
                    public void call(List<WalletOperationItem> lists) {
                        mWalletOperationList = lists;
                        if (mRedoWalletOperationList) {
                            mRedoWalletOperationList = false;
                        }
                    }
                });
    }

    @Override
    public void redoGetWalletOperationList() {
        mRedoWalletOperationList = true;
    }


    /**
     * 新接口Get History List.
     */
    @Override
    public Observable<TopupRechargeResponse> GetTopupPriceListNews() {
        if (mTopupRechargeResponse != null) {
            return Observable.just(mTopupRechargeResponse);
        }

        return mRemoteDataSource.GetTopupPriceListNews()
                .doOnNext(new Action1<TopupRechargeResponse>() {
                    @Override
                    public void call(TopupRechargeResponse lists) {
                        mTopupRechargeResponse = lists;
                    }
                });
    }

    /**
     * 新接口Get History List.
     */
    @Override
    public Observable<VipComboResponse> GetVipPackageListNews() {
        if (mVipComboResponse != null && !mRedoVipPackageList) {
            return Observable.just(mVipComboResponse);
        }

        return mRemoteDataSource.GetVipPackageListNews()
                .doOnNext(new Action1<VipComboResponse>() {
                    @Override
                    public void call(VipComboResponse lists) {
                        mVipComboResponse = lists;
                        if (mRedoVipPackageList) {
                            mRedoVipPackageList = false;
                        }
                    }
                });
    }

    @Override
    public Observable<VipMonthlyResult> getVipMonthlyStatus(String id, String type) {
        return mRemoteDataSource.getVipMonthlyStatus(id, type);
    }

    @Override
    public void redoGetVipPackageList() {
        mRedoVipPackageList = true;
    }

    /**
     * 新接口Get History List.
     */
    @Override
    public Observable<HistoryResponse> GetHistoryListNews(@NonNull String productId,
            @NonNull String productType, @NonNull String beginTime, @NonNull String endTime) {
        checkNotNull(productId);
        checkNotNull(productType);

        if (mHistoryResponse != null && !mRedoHistoryList) {
            return Observable.just(mHistoryResponse);
        }

        return mRemoteDataSource.GetHistoryListNews(productId,
                productType,
                beginTime, endTime)
                .doOnNext(new Action1<HistoryResponse>() {
                    @Override
                    public void call(HistoryResponse lists) {
                        mHistoryResponse = lists;
                        if (mRedoHistoryList) {
                            mRedoHistoryList = false;
                        }
                    }
                });
    }

    @Override
    public Observable<Login> loginAgain(@NonNull String userid, @NonNull String pw,
            @NonNull String phone, @NonNull String verifyCode, @NonNull String status,
            @NonNull String logontype, @NonNull String branchtype, @NonNull String kdmVersion,
            @NonNull String kdmPlatform) {
        return mRemoteDataSource.loginAgain(userid, pw, phone, verifyCode, status, logontype,
                branchtype, kdmVersion, kdmPlatform);
    }

    @Override
    public Observable<CreditOperation> queryUserCreditOperation() {
        if (!mIsRedoUserCreditOperation && mCreditOperation != null) {
            return Observable.just(mCreditOperation);
        }

        return mRemoteDataSource.queryUserCreditOperation()
                .doOnNext(new Action1<CreditOperation>() {
                    @Override
                    public void call(CreditOperation operation) {
                        mCreditOperation = operation;
                        if (mIsRedoUserCreditOperation) {
                            mIsRedoUserCreditOperation = false;
                        }
                    }
                });
    }

    @Override
    public void redoUserCreditOperation() {
        mIsRedoUserCreditOperation = true;
    }


    @Override
    public Observable<FinanceMessage> queryUserFinanceMessage() {
        if (!mIsRedoUserFinanceMessage && mFinanceMessage != null) {
            return Observable.just(mFinanceMessage);
        }

        return mRemoteDataSource.queryUserFinanceMessage()
                .doOnNext(new Action1<FinanceMessage>() {
                    @Override
                    public void call(FinanceMessage operation) {
                        mFinanceMessage = operation;
                        if (mIsRedoUserFinanceMessage) {
                            mIsRedoUserFinanceMessage = false;
                        }
                    }
                });
    }

    @Override
    public void redoQueryUserFinanceMessage() {
        mIsRedoUserFinanceMessage = true;
    }

    @Override
    public Observable<RecommendComboResponse> queryRecommendComboInfo() {
        RecommendComboResponse recommendComboCache;
        if (!mRecommendComboCacheIsDirty
                && (recommendComboCache = getRecommendComboCache()) != null) {
            return Observable.just(recommendComboCache);
        }

        if (null == mRemoteTask) {
            synchronized (this) {
                if (null == mRemoteTask) {
                    mRemoteTask = mRemoteDataSource.queryRecommendComboInfo()
                            .doOnNext(new Action1<RecommendComboResponse>() {
                                @Override
                                public void call(RecommendComboResponse response) {
                                    if (response != null && response.isOk()) {
                                        mRecommendComboCacheIsDirty = false;
                                        setRecommendComboCache(response);
                                    }
                                }
                            })
                            .replay(1)
                            .refCount();
                }
            }
        }
        return mRemoteTask;
    }

    @Override
    public void refreshRecommendCombo() {
        mRecommendComboCacheIsDirty = true;
    }

    @Override
    public Observable<AllAgreement> getAgreement(final String type) {
        AllAgreement agreementCached = null;

        // has cached
        if (mAgreementCachedMap != null && (agreementCached = mAgreementCachedMap.get(type))
                != null) {
            return Observable.just(agreementCached);
        }

        return mRemoteDataSource.getAgreement(type)
                .doOnNext(new Action1<AllAgreement>() {
                    @Override
                    public void call(AllAgreement allAgreement) {
                        if (allAgreement != null && allAgreement.isOk()) {
                            if (null == mAgreementCachedMap) {
                                mAgreementCachedMap = new HashMap<>();
                            }
                            mAgreementCachedMap.put(type, allAgreement);
                        }
                    }
                });
    }

    private Observable<Login> loginInternal() {
        return login(mUserId, mPassword, mUserStatus, mBranchtype,
                mKdmVersion, mKdmPlatform);
    }

    private synchronized RecommendComboResponse getRecommendComboCache() {
        return mRecommendComboCache;
    }

    private synchronized void setRecommendComboCache(
            RecommendComboResponse recommendComboCache) {
        mRecommendComboCache = recommendComboCache;
    }

    @Override
    public Observable<ExitGuideResponse> queryExitGuideResponse(@NonNull String encryptionType) {
        return mRemoteDataSource.queryExitGuideResponse(encryptionType);
    }
}
