package com.golive.cinema.data.source.remote;

import android.support.annotation.NonNull;

import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.restapi.RestApiErrorCheckFlatMap;
import com.golive.network.entity.AllAgreement;
import com.golive.network.entity.ClientService;
import com.golive.network.entity.CreditOperation;
import com.golive.network.entity.EditHistoryResult;
import com.golive.network.entity.FinanceMessage;
import com.golive.network.entity.Login;
import com.golive.network.entity.MainConfig;
import com.golive.network.entity.Response;
import com.golive.network.entity.UserHead;
import com.golive.network.entity.UserInfo;
import com.golive.network.entity.VipMonthlyResult;
import com.golive.network.entity.Wallet;
import com.golive.network.entity.WalletOperationItem;
import com.golive.network.entity.WalletOperationList;
import com.golive.network.entity.WechatInfo;
import com.golive.network.net.GoLiveRestApi;
import com.golive.network.response.ClientServiceResponse;
import com.golive.network.response.ExitGuideResponse;
import com.golive.network.response.HistoryResponse;
import com.golive.network.response.RecommendComboResponse;
import com.golive.network.response.TopupRechargeResponse;
import com.golive.network.response.UserHeadResponse;
import com.golive.network.response.VipComboResponse;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/9/23.
 */

public class UserRemoteDataSource implements UserDataSource {

    private static UserRemoteDataSource INSTANCE;

    private final GoLiveRestApi mGoLiveRestApi;
    private final MainConfigDataSource mMainConfigDataSource;

    private UserRemoteDataSource(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        this.mGoLiveRestApi = goLiveRestApi;
        this.mMainConfigDataSource = mainConfigDataSource;
    }

    public static UserRemoteDataSource getInstance(GoLiveRestApi goLiveRestApi,
            MainConfigDataSource mainConfigDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new UserRemoteDataSource(goLiveRestApi, mainConfigDataSource);
        }
        return INSTANCE;
    }

    @Override
    public Observable<Login> login(@NonNull final String userid, @NonNull final String pw,
            @NonNull final String status, @NonNull final String branchtype,
            @NonNull final String kdmVersion,
            @NonNull final String kdmPlatform) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<Login>>() {
                    @Override
                    public Observable<Login> call(MainConfig mainConfig) {
                        String url = mainConfig.getLogin();
                        return mGoLiveRestApi.login(url, userid, pw, status, branchtype, kdmVersion,
                                kdmPlatform);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<Login>());
    }

    @Override
    public void refreshLogin() {

    }

    @Override
    public Observable<Response> logout() {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<Response>>() {
                    @Override
                    public Observable<Response> call(MainConfig mainConfig) {
                        String url = mainConfig.getLogout();
                        return mGoLiveRestApi.logout(url);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<>());
    }

    @Override
    public Observable<UserInfo> getUserInfo() {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<UserInfo>>() {
                    @Override
                    public Observable<UserInfo> call(MainConfig mainConfig) {
                        String url = mainConfig.getGetuserinfo();
                        return mGoLiveRestApi.getUserInfo(url);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<UserInfo>());
    }

    @Override
    public void refreshUserInfo() {

    }

    @Override
    public Observable<Wallet> getWallet() {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<Wallet>>() {
                    @Override
                    public Observable<Wallet> call(MainConfig mainConfig) {
                        String url = mainConfig.getQuerywallet();
                        return mGoLiveRestApi.queryWallet(url);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<Wallet>());
    }

    @Override
    public Observable<Wallet> getCreditWallet() {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<Wallet>>() {
                    @Override
                    public Observable<Wallet> call(MainConfig mainConfig) {
                        String url = mainConfig.getQueryCreditwallet();
                        return mGoLiveRestApi.queryWallet(url);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<Wallet>());
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
        public Observable<List<VipPackage>> GetVipPackageList(){

            return mMainConfigDataSource.getMainConfig()
                    .flatMap(new Func1<MainConfig, Observable<VipPackageList>>() {
                        @Override
                        public Observable<VipPackageList> call(MainConfig mainConfig) {
                            String url = mainConfig.getQueryvipproducts();
                            return mGoLiveRestApi.GetVipPackageList(url);
                        }
                    })
                    .flatMap(new RestApiErrorCheckFlatMap<VipPackageList>())
                    .map(new Func1<VipPackageList, List<VipPackage>>() {
                        @Override
                        public List<VipPackage> call(VipPackageList lists) {
                            if (lists !=null){
                                List<VipPackage> viplists =lists.getList();
                                if (viplists ==null){
                                    viplists = new ArrayList<VipPackage>();
                                }
                                return  viplists;
                            }
                            return null;
                        }
                    });

        }

        @Override
        public Observable<List<Order>> GetHistoryList(@NonNull final String productId,
                                                      @NonNull final String productType, @NonNull
                                                       final String beginTime, @NonNull final
                                                       String endTime){

            return mMainConfigDataSource.getMainConfig()
                    .flatMap(new Func1<MainConfig, Observable<OrderList>>() {
                        @Override
                        public Observable<OrderList> call(MainConfig mainConfig) {
                            String url = mainConfig.getHistoryList();
                            return mGoLiveRestApi.GetHistoryList(url,productId,productType,
                            beginTime,endTime);
                        }
                    })
                    .flatMap(new RestApiErrorCheckFlatMap<OrderList>())
                    .map(new Func1<OrderList, List<Order>>() {
                        @Override
                        public List<Order> call(OrderList lists) {
                            if (lists !=null){
                                List<Order> orders =lists.getOrders();
                                if (orders ==null){
                                    orders = new ArrayList<Order>();
                                }
                                return orders;
                            }
                            return null;
                        }
                    });

        }*/
    @Override
    public void redoGetHistoryList() {
    }

    @Override
    public Observable<EditHistoryResult> AddHistory(@NonNull final String serial) {

        return mMainConfigDataSource.getMainConfig()
                .flatMap(
                        new Func1<MainConfig, Observable<EditHistoryResult>>() {
                            @Override
                            public Observable<EditHistoryResult> call(MainConfig mainConfig) {
                                String url = mainConfig.getAddhistory();
                                return mGoLiveRestApi.AddHistory(url, serial);
                            }
                        })
                .flatMap(new RestApiErrorCheckFlatMap<EditHistoryResult>());

    }

    @Override
    public Observable<EditHistoryResult> DeleteHistory(@NonNull final String serial,
            final String title) {

        return mMainConfigDataSource.getMainConfig()
                .flatMap(
                        new Func1<MainConfig, Observable<EditHistoryResult>>() {
                            @Override
                            public Observable<EditHistoryResult> call(MainConfig mainConfig) {
                                String url = mainConfig.getDeleteHistory();
                                return mGoLiveRestApi.DeleteHistory(url, serial, title);
                            }
                        })
                .flatMap(new RestApiErrorCheckFlatMap<EditHistoryResult>());


    }


    @Override
    public Observable<WechatInfo> GetWechatInfo() {

        return mMainConfigDataSource.getMainConfig()
                .flatMap(
                        new Func1<MainConfig, Observable<WechatInfo>>() {
                            @Override
                            public Observable<WechatInfo> call(MainConfig mainConfig) {
                                String url = mainConfig.getGetpublicinfo();
                                return mGoLiveRestApi.GetWechatInfo(url);
                            }
                        })
                .flatMap(new RestApiErrorCheckFlatMap<WechatInfo>());


    }

    @Override
    public Observable<ClientService> GetClientService() {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<ClientServiceResponse>>() {
                    @Override
                    public Observable<ClientServiceResponse> call(MainConfig mainConfig) {
                        String url = mainConfig.getGetClientService();
                        return mGoLiveRestApi.GetClientService(url);
                    }
                })
                .map(new Func1<ClientServiceResponse, ClientService>() {
                    @Override
                    public ClientService call(ClientServiceResponse clientRp) {
                        return clientRp.getInfo();
                    }
                });
    }

    @Override
    public Observable<UserHead> GetUserHead() {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<UserHeadResponse>>() {
                    @Override
                    public Observable<UserHeadResponse> call(MainConfig mainConfig) {
                        String url = mainConfig.getGetUserIcon();
                        return mGoLiveRestApi.GetUserHead(url);
                    }
                })
                .map(new Func1<UserHeadResponse, UserHead>() {
                    @Override
                    public UserHead call(UserHeadResponse userRp) {
                        return userRp.getUserHead();
                    }
                });
    }


    @Override
    public Observable<List<WalletOperationItem>> GetWalletOperationList() {

        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<WalletOperationList>>() {
                    @Override
                    public Observable<WalletOperationList> call(MainConfig mainConfig) {
                        String url = mainConfig.getQuerywalletoperations();
                        return mGoLiveRestApi.GetWalletOperationList(url);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<WalletOperationList>())
                .map(new Func1<WalletOperationList, List<WalletOperationItem>>() {
                    @Override
                    public List<WalletOperationItem> call(WalletOperationList lists) {
                        if (lists != null) {
                            List<WalletOperationItem> walletlists = lists.getList();
                            if (walletlists == null) {
                                walletlists = new ArrayList<>();
                            }
                            return walletlists;
                        }
                        return null;
                    }
                });

    }

    @Override
    public void redoGetWalletOperationList() {
    }
/*
    @Override
    public Observable<List<TopupOnePrice>> GetTopupPriceList(){
         final  String region ="CN";
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<TopupSellectionPrice>>() {
                    @Override
                    public Observable<TopupSellectionPrice> call(MainConfig mainConfig) {
                        String url = mainConfig.getQuerycoins();
                        return mGoLiveRestApi.GetTopupPriceList(url,region);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<TopupSellectionPrice>())
                .map(new Func1<TopupSellectionPrice, List<TopupOnePrice>>() {
                    @Override
                    public List<TopupOnePrice> call(TopupSellectionPrice lists) {
                        if (lists !=null){
                            List<TopupOnePrice> topuplists =lists.getPriceList();
                            if (topuplists ==null){
                                topuplists = new ArrayList<TopupOnePrice>();
                            }
                            return topuplists;
                        }
                        return null;
                    }
                });

    }*/

    /**
     * 新接口
     */
    @Override
    public Observable<TopupRechargeResponse> GetTopupPriceListNews() {
        final String region = "CN";
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<TopupRechargeResponse>>() {
                    @Override
                    public Observable<TopupRechargeResponse> call(MainConfig mainConfig) {
                        String url = mainConfig.getGetRechargeItem();
                        return mGoLiveRestApi.GetTopupPriceListNews(url, region);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<TopupRechargeResponse>());
    }

    /**
     * 新接口
     */
    @Override
    public Observable<VipComboResponse> GetVipPackageListNews() {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<VipComboResponse>>() {
                    @Override
                    public Observable<VipComboResponse> call(MainConfig mainConfig) {
                        String url = mainConfig.getGetComboItem();
                        return mGoLiveRestApi.GetVipPackageListNews(url);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<VipComboResponse>());

    }

    @Override
    public Observable<VipMonthlyResult> getVipMonthlyStatus(final String id, final String type) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<VipMonthlyResult>>() {
                    @Override
                    public Observable<VipMonthlyResult> call(MainConfig mainConfig) {
                        String url = mainConfig.getContinueMonthQueryUrl();
                        return mGoLiveRestApi.getVipMonthlyResult(url, id, type);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<VipMonthlyResult>());
    }

    @Override
    public void redoGetVipPackageList() {
    }

    /**
     * 新接口
     */
    @Override
    public Observable<HistoryResponse> GetHistoryListNews(@NonNull final String productId,
            @NonNull final String productType, @NonNull final String beginTime,
            @NonNull final String endTime) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<HistoryResponse>>() {
                    @Override
                    public Observable<HistoryResponse> call(MainConfig mainConfig) {
                        String url =
                                mainConfig.getGetViewingRecord();//"http://183.60.142
                        // .151:8063/goliveAPI/api5/order_getHistory.action";//
                        return mGoLiveRestApi.GetHistoryListNews(url, productId, productType,
                                beginTime, endTime);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<HistoryResponse>());


    }


    @Override
    public Observable<Login> loginAgain(@NonNull final String userid, @NonNull final String pw,
            @NonNull final String phone, @NonNull final String verifyCode,
            @NonNull final String status,
            @NonNull final String logontype, @NonNull final String branchtype,
            @NonNull final String kdmVersion,
            @NonNull final String kdmPlatform) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<Login>>() {
                    @Override
                    public Observable<Login> call(MainConfig mainConfig) {
                        return mGoLiveRestApi.loginAgain(mainConfig.getLogin_40(), userid, pw,
                                phone, verifyCode, status, logontype, branchtype, kdmVersion,
                                kdmPlatform);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<Login>());
    }

    @Override
    public Observable<CreditOperation> queryUserCreditOperation() {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<CreditOperation>>() {
                    @Override
                    public Observable<CreditOperation> call(MainConfig mainConfig) {
                        return mGoLiveRestApi.queryUserCreditOperation(
                                mainConfig.getQueryCreditwallet());
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<CreditOperation>());
    }

    @Override
    public void redoUserCreditOperation() {
    }

    @Override
    public Observable<FinanceMessage> queryUserFinanceMessage() {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<FinanceMessage>>() {
                    @Override
                    public Observable<FinanceMessage> call(MainConfig mainConfig) {
                        return mGoLiveRestApi.queryUserFinanceMessage(
                                mainConfig.getQuerymessages());
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<FinanceMessage>());
    }

    @Override
    public void redoQueryUserFinanceMessage() {
    }

    @Override
    public Observable<RecommendComboResponse> queryRecommendComboInfo() {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<RecommendComboResponse>>() {
                    @Override
                    public Observable<RecommendComboResponse> call(MainConfig mainConfig) {
                        return mGoLiveRestApi.queryRecommendComboInfo(
                                mainConfig.getRecommendComboItem());
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<RecommendComboResponse>());
    }

    @Override
    public void refreshRecommendCombo() {

    }

    @Override
    public Observable<AllAgreement> getAgreement(final String type) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<AllAgreement>>() {
                    @Override
                    public Observable<AllAgreement> call(MainConfig mainConfig) {
                        return mGoLiveRestApi.getAgreement(mainConfig.getGetAgreement(), type);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<AllAgreement>());
    }

    @Override
    public Observable<ExitGuideResponse> queryExitGuideResponse(
            @NonNull final String encryptionType) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<ExitGuideResponse>>() {
                    @Override
                    public Observable<ExitGuideResponse> call(MainConfig mainConfig) {
//                        String url =
//                                "http://172.19.0.125:8081/goliveAPI/api5/exit_appDrainage.action";
                        String drainageUrl = mainConfig.getGetDrainageUrl();
                        String url =mainConfig.getAppDrainage();
                        return mGoLiveRestApi.queryExitGuideResponse(encryptionType, url,
                                drainageUrl);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<ExitGuideResponse>());
    }
}
