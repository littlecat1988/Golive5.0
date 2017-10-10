package com.golive.cinema.filmdetail;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.golive.cinema.data.source.DownloadDataSource;
import com.golive.cinema.data.source.FilmsDataSource;
import com.golive.cinema.data.source.KdmDataSource;
import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.data.source.OrdersDataSource;
import com.golive.cinema.data.source.PlayerDataSource;
import com.golive.cinema.data.source.RecommendDataSource;
import com.golive.cinema.data.source.StatisticsDataSource;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.download.domain.usecase.CheckDownloadFinishUseCase;
import com.golive.cinema.download.domain.usecase.GetDownloadTaskInfoUseCase;
import com.golive.cinema.filmdetail.domain.usecase.GetFilmDetailUseCase;
import com.golive.cinema.order.domain.usecase.CreateOrderUseCase;
import com.golive.cinema.order.domain.usecase.GetValidOrderUseCase;
import com.golive.cinema.order.domain.usecase.PayCreditOrderUseCase;
import com.golive.cinema.order.domain.usecase.PayOrderUseCase;
import com.golive.cinema.player.domain.model.PlaybackValidity;
import com.golive.cinema.player.domain.usecase.GetKdmInitUseCase;
import com.golive.cinema.player.domain.usecase.GetPlayTicketUseCase;
import com.golive.cinema.player.domain.usecase.GetPlaybackValidityUseCase;
import com.golive.cinema.player.kdm.KDM;
import com.golive.cinema.purchase.domain.usecase.PurchaseUseCase;
import com.golive.cinema.recommend.domain.usecase.GetMovieRecommendUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportEnterFilmDetailUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportExitFilmDetailUseCase;
import com.golive.cinema.user.myinfo.domain.usecase.GetUserInfoUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserCreditWalletUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserWalletUseCase;
import com.golive.cinema.util.schedulers.ImmediateSchedulerProvider;
import com.golive.network.entity.Ad;
import com.golive.network.entity.Film;
import com.golive.network.entity.Media;
import com.golive.network.entity.MovieRecommendFilm;
import com.golive.network.entity.Order;
import com.golive.network.entity.PayOrderResult;
import com.golive.network.entity.UserInfo;
import com.golive.network.entity.Wallet;
import com.golive.network.response.MovieRecommendResponse;
import com.initialjie.download.aidl.DownloadTaskInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by Wangzj on 2016/10/10.
 */
public class FilmDetailPresenterTest {

    private static final String FILM_ID = "123";
    private static final String MEDIA_ID_1 = "media_id_1";
    private static final String MEDIA_ID_2 = "media_id_2";
    private static final String URL = "http://www.baidu.com";
    private static final String PRICE = "5.0";
    private static final String VIP_PRICE = "0";

    @Mock
    private FilmsDataSource mFilmsDataSource;

    @Mock
    private RecommendDataSource mRecommendDataSource;

    @Mock
    private OrdersDataSource mOrdersDataSource;

    @Mock
    private Order mOrder;

    @Mock
    private UserDataSource mUserDataSource;

    @Mock
    private DownloadDataSource mDownloadDataSource;

    @Mock
    private PlayerDataSource mPlayerDataSource;

    @Mock
    private MainConfigDataSource mMainConfigDataSource;

    @Mock
    private KdmDataSource mKdmDataSource;

    @Mock
    private StatisticsDataSource mStatisticsDataSource;

    @Mock
    private KDM mKDM;

    @Mock
    private GetPlaybackValidityUseCase mGetPlaybackValidityUseCase;

    @Mock
    private FilmDetailContract.View mView;

    private FilmDetailPresenter mFilmDetailPresenter;
    private Film film;
    private List<Order> mOrderList;
    private List<MovieRecommendFilm> mRecommendFilms;

    @Before
    public void setupFilmDetailPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mFilmDetailPresenter = givenFilmDetailPresenter();

        // The presenter won't update the view unless it's active.
        when(mView.isActive()).thenReturn(true);

        film = new Film();
        film.setReleaseid(FILM_ID);
        film.setName(FILM_ID);
        film.setIntroduction(FILM_ID);

        film.setPrice(PRICE);
        film.setVipprice(VIP_PRICE);
        film.setOnlineprice(PRICE);
        film.setViponlineprice(VIP_PRICE);
        film.setDownloadprice(PRICE);
        film.setVipdownloadprice(VIP_PRICE);

        List<Media> medias = new ArrayList<>();
        Media media;

        media = new Media();
        media.setId(MEDIA_ID_1);
        media.setName(MEDIA_ID_1);
        media.setRankname("高清");
        media.setType(Media.MEDIA_TYPE_ONLINE);
        media.setUrl(URL);
        media.setEncryption(Media.TYPE_NO_ENCRYPT);
        medias.add(media);

        media = new Media();
        media.setId(MEDIA_ID_2);
        media.setRankname("标清");
        media.setType(Media.MEDIA_TYPE_DOWNLOAD);
        media.setUrl(URL);
        media.setEncryption(Media.TYPE_NO_ENCRYPT);
        medias.add(media);

        film.setMedias(medias);

        mOrderList = new ArrayList<>();
        mOrderList.add(new Order());

        mRecommendFilms = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            MovieRecommendFilm film = new MovieRecommendFilm();
            film.setName("film " + i);
            film.setBigposter("http://img.lanrentuku.com/img/allimg/1605/1464490533194.jpg");
            mRecommendFilms.add(film);
        }

        // Given an initialized TasksPresenter with initialized films
        when(mFilmsDataSource.getFilmDetail(FILM_ID)).thenReturn(Observable.just(film));
//        when(mOrdersDataSource.queryOrders(anyString(), anyString(), anyString(),
//                anyString())).thenReturn(Observable.just(mOrderList));
        when(mOrdersDataSource.getValidOrders(anyString(), anyString())).thenReturn(
                Observable.just(mOrderList));
        when(mUserDataSource.getUserInfo()).thenReturn(Observable.just(new UserInfo()));
        // return a wallet
        Wallet value = new Wallet();
        when(mUserDataSource.getWallet()).thenReturn(Observable.just(value));
        when(mUserDataSource.getCreditWallet()).thenReturn(Observable.just(value));
        MovieRecommendResponse recommendResponse = new MovieRecommendResponse();
        when(mRecommendDataSource.getMovieRecommendData(anyString(),
                anyString())).thenReturn(Observable.just(recommendResponse));
    }

    private FilmDetailPresenter givenFilmDetailPresenter() {
        ImmediateSchedulerProvider schedulerProvider = new ImmediateSchedulerProvider();
        GetFilmDetailUseCase getFilmDetailUseCase =
                new GetFilmDetailUseCase(mFilmsDataSource, schedulerProvider);
        GetKdmInitUseCase getKdmInitUseCase = new GetKdmInitUseCase(schedulerProvider,
                mKdmDataSource, mMainConfigDataSource);
        GetMovieRecommendUseCase getMovieRecommendUseCase = new GetMovieRecommendUseCase(
                mRecommendDataSource, getKdmInitUseCase, schedulerProvider);
        GetValidOrderUseCase getValidOrderUseCase = new GetValidOrderUseCase(mOrdersDataSource,
                schedulerProvider);
        CreateOrderUseCase createOrderUseCase = new CreateOrderUseCase(mOrdersDataSource,
                schedulerProvider);
        PayOrderUseCase payOrderUseCase = new PayOrderUseCase(mOrdersDataSource, schedulerProvider);
        PayCreditOrderUseCase payCreditOrderUseCase = new PayCreditOrderUseCase(mOrdersDataSource,
                schedulerProvider);
        GetUserWalletUseCase getUserWalletUseCase = new GetUserWalletUseCase(mUserDataSource,
                schedulerProvider);
        GetUserCreditWalletUseCase getUserCreditWalletUseCase = new GetUserCreditWalletUseCase(
                mUserDataSource, schedulerProvider);
        PurchaseUseCase purchaseFilmUseCase = new PurchaseUseCase(
                createOrderUseCase, payOrderUseCase, payCreditOrderUseCase, schedulerProvider);
        GetUserInfoUseCase getUserInfoUseCase = new GetUserInfoUseCase(mUserDataSource,
                schedulerProvider);
        GetDownloadTaskInfoUseCase getDownloadTaskInfoUseCase = new GetDownloadTaskInfoUseCase(
                mDownloadDataSource, schedulerProvider);
        CheckDownloadFinishUseCase checkDownloadFinishUseCase = new CheckDownloadFinishUseCase(
                mDownloadDataSource, schedulerProvider);
        GetPlayTicketUseCase getPlayTicketUseCase = new GetPlayTicketUseCase(schedulerProvider,
                mOrdersDataSource);
//        mGetPlaybackValidityUseCase =
//                new GetPlaybackValidityUseCase(mPlayerDataSource, mFilmsDataSource,
//                        mOrdersDataSource, mDownloadDataSource, mKDM, schedulerProvider);
        ReportEnterFilmDetailUseCase reportEnterFilmDetailUseCase =
                new ReportEnterFilmDetailUseCase(schedulerProvider, mStatisticsDataSource);
        ReportExitFilmDetailUseCase
                reportExitFilmDetailUseCase = new ReportExitFilmDetailUseCase(schedulerProvider,
                mStatisticsDataSource);
        return new FilmDetailPresenter(FILM_ID, null, mView, getFilmDetailUseCase,
                getMovieRecommendUseCase, getValidOrderUseCase,
                getUserWalletUseCase, getUserCreditWalletUseCase, purchaseFilmUseCase,
                getUserInfoUseCase, mGetPlaybackValidityUseCase, getDownloadTaskInfoUseCase,
                getPlayTicketUseCase, reportEnterFilmDetailUseCase, reportExitFilmDetailUseCase,
                schedulerProvider
        );
    }

    @Test
    public void loadFilmDetailFromRepositoryAndLoadIntoView() {

        Wallet value = new Wallet();
        value.setCreditLine("10");
        when(mUserDataSource.getWallet()).thenReturn(Observable.just(value));
        when(mUserDataSource.getCreditWallet()).thenReturn(Observable.just(value));

        // When loading of films is requested
        mFilmDetailPresenter.loadFilmDetail(true);

        // Callback is captured and invoked with stubbed films
        verify(mFilmsDataSource, atLeastOnce()).getFilmDetail(FILM_ID);

        // Then progress indicator is shown
        verify(mView).setLoadingIndicator(true);
        // Then progress indicator is hidden and all tasks are shown in UI
        verify(mView).setLoadingIndicator(false);
        verify(mView).showTitle(film.getName());
        verify(mView).showDescription(film.getIntroduction());
        verify(mView).showRecommendPosters(mRecommendFilms);
        verify(mView).setUpdatingViewIndicator(true);
        verify(mView).setUpdatingViewIndicator(false);
        verify(mView).showPurchaseFilm(eq(MEDIA_ID_1), eq(true), anyString(), eq(PRICE));
        verify(mView).showPurchaseFilm(eq(MEDIA_ID_2), eq(false), anyString(), eq(PRICE));
        verify(mView, never()).showPlayFilm(eq(MEDIA_ID_1), anyBoolean(), anyString());
        verify(mView, never()).showPlayFilm(eq(MEDIA_ID_2), anyBoolean(), anyString());
        verify(mView, atLeastOnce()).setCreditPayViewVisible(true);
        verify(mView, atLeastOnce()).setRegisterVip(PRICE, VIP_PRICE, PRICE, VIP_PRICE);
        //verify(mView).showLoadingFilmsError();
    }

    @Test
    public void purchaseFilmAndBalanceNotEnough() {

        Wallet value = new Wallet();
        when(mUserDataSource.getWallet()).thenReturn(Observable.just(value));
        when(mUserDataSource.getCreditWallet()).thenReturn(Observable.just(value));
        Order order = new Order();
        order.setPrice(PRICE);
        when(mOrdersDataSource.createOrder(anyString(), anyString(), anyString(), anyString(),
                anyString())).thenReturn(Observable.just(order));
        PayOrderResult payOrderResult = new PayOrderResult();
        payOrderResult.setNeeded(PRICE);
        when(mOrdersDataSource.payOrder(anyString())).thenReturn(Observable.just(payOrderResult));
        when(mView.showPurchaseFilmUI(anyString(), anyString(), anyBoolean(), anyString(),
                anyString(), anyString(), anyString(), eq(false))).thenReturn(
                Observable.just(false));

        // When loading of films is requested
        mFilmDetailPresenter.purchaseFilm(MEDIA_ID_1);

        // Callback is captured and invoked with stubbed films
        verify(mUserDataSource).getWallet();

        // Then progress indicator is shown
        verify(mView).setPurchasingIndicator(true);
        // Then progress indicator is hidden and all tasks are shown in UI
        verify(mView).setPurchasingIndicator(false);
        verify(mView).showPurchaseFilmUI(eq(FILM_ID), eq(MEDIA_ID_1), anyBoolean(), anyString(),
                anyString(), anyString(), anyString(), eq(false));
//        verify(mView).showBalanceNotEnough(FILM_ID, Double.parseDouble(PRICE), 0);
        verify(mOrdersDataSource, never()).createOrder(anyString(), anyString(), anyString(),
                anyString(), anyString());
        verify(mOrdersDataSource, never()).payOrder(anyString());
    }

    @Test
    public void purchaseFilmSuccess() {

        Wallet value = new Wallet();
        value.setValue(PRICE);
        value.setCurrency("RMB");
        when(mUserDataSource.getWallet()).thenReturn(Observable.just(value));
        when(mUserDataSource.getCreditWallet()).thenReturn(Observable.just(value));
        Order order = new Order();
        order.setSerial("xxx");
        order.setPrice(PRICE);
        order.setProductType(Order.PRODUCT_TYPE_THEATRE_ONLINE);
        order.setMediaResourceId(MEDIA_ID_1);
        when(mOrdersDataSource.createOrder(anyString(), anyString(), anyString(), anyString(),
                anyString())).thenReturn(Observable.just(order));
        PayOrderResult payOrderResult = new PayOrderResult();
        payOrderResult.setOrder(order);
        when(mOrdersDataSource.payOrder(anyString())).thenReturn(Observable.just(payOrderResult));
        when(mView.showPurchaseFilmUI(anyString(), anyString(), anyBoolean(), anyString(),
                anyString(), anyString(), anyString(), eq(false))).thenReturn(
                Observable.just(true));

        DownloadTaskInfo taskInfo = null;
        when(mDownloadDataSource.getDownloadTaskInfo(anyString(), anyString())).thenReturn(
                Observable.just(taskInfo));

        List<Order> validOrders = new ArrayList<>();
        order = new Order();
        order.setSerial("xxx");
        order.setProductType(Order.PRODUCT_TYPE_THEATRE_ONLINE);
        order.setStatus(Order.STATUS_PAY_SUCCESS);
        order.setRemain("18000000");
        order.setPrice(PRICE);
        order.setMediaResourceId(MEDIA_ID_1);
        validOrders.add(order);
        when(mOrdersDataSource.getValidOrders(anyString(), anyString())).thenReturn(
                Observable.just(validOrders));

        // When loading of films is requested
        mFilmDetailPresenter.purchaseFilm(MEDIA_ID_1);

        // Then progress indicator is shown
        verify(mView).setPurchasingIndicator(true);
        // Then progress indicator is hidden and all tasks are shown in UI
        verify(mView).setPurchasingIndicator(false);
//        double price = Double.parseDouble(PRICE);
        verify(mView).showPurchaseFilmUI(eq(FILM_ID), eq(MEDIA_ID_1), anyBoolean(), anyString(),
                anyString(), anyString(), eq(PRICE), eq(false));
        verify(mOrdersDataSource).getValidOrders(eq(FILM_ID), anyString());
        verify(mUserDataSource, atLeastOnce()).getCreditWallet();
        verify(mUserDataSource, atLeast(2)).getWallet();
        verify(mUserDataSource, atLeast(2)).getUserInfo();
        verify(mView).showPurchaseFilmSuccess(eq(MEDIA_ID_1), anyLong());
        verify(mView).showPlayFilm(eq(MEDIA_ID_1), anyBoolean(), anyString());
    }

    @Test
    public void purchaseFilmAndCreditExpire() {

        Wallet wallet = new Wallet();
        wallet.setValue("-5");
        wallet.setCreditLine("20");
        wallet.setCreditDeadLineDays("180");
        wallet.setCreditRemain("0");
        when(mUserDataSource.getWallet()).thenReturn(Observable.just(wallet));
        when(mUserDataSource.getCreditWallet()).thenReturn(Observable.just(wallet));

        // When loading of films is requested
        mFilmDetailPresenter.purchaseFilm(MEDIA_ID_1);

        // getCreditWallet is called
        verify(mUserDataSource).getCreditWallet();

        // Then progress indicator is shown
        verify(mView).setPurchasingIndicator(true);
        // Then progress indicator is hidden and all tasks are shown in UI
        verify(mView).setPurchasingIndicator(false);

        // showCreditPayExpire
        verify(mView).showCreditPayExpire(180, 5, 20);

        // showConfirmPurchaseFilm never called
        verify(mView, never()).showPurchaseFilmUI(anyString(), anyString(), anyBoolean(),
                anyString(), anyString(), anyString(), anyString(), anyBoolean());
        // createOrder never called
        verify(mOrdersDataSource, never()).createOrder(anyString(), anyString(), anyString(),
                anyString(), anyString());
        // payOrder never called
        verify(mOrdersDataSource, never()).payOrder(anyString());
    }

    @Test
    public void loadFilmDetailAndPlayFilm() {
        List<Order> orderList = new ArrayList<>();
        Order order = new Order();
        order.setStatus(Order.STATUS_PAY_SUCCESS);
        order.setMediaResourceId(MEDIA_ID_1);
        order.setProductType(Order.PRODUCT_TYPE_THEATRE_ONLINE);
        orderList.add(order);
        when(mOrdersDataSource.getValidOrders(anyString(), anyString())).thenReturn(
                Observable.just(orderList));

        PlaybackValidity playbackValidity = new PlaybackValidity(true, true,
                PlaybackValidity.ERR_OK, 0, 0, URL);
//        when(mPlayerDataSource.getPlaybackValidity(anyString(), anyString())).thenReturn(
//                Observable.just(playbackValidity));
        GetPlaybackValidityUseCase.ResponseValue responseValue =
                new GetPlaybackValidityUseCase.ResponseValue(playbackValidity);
        when(mGetPlaybackValidityUseCase.run(
                any(GetPlaybackValidityUseCase.RequestValues.class))).thenReturn(
                Observable.just(responseValue));

        // When loading of films is requested
        mFilmDetailPresenter.loadFilmDetail(true);

        // Callback is captured and invoked with stubbed films
        verify(mFilmsDataSource).getFilmDetail(FILM_ID);

        // Then progress indicator is shown
        verify(mView).setLoadingIndicator(true);
        // Then progress indicator is hidden and all tasks are shown in UI
        verify(mView).setLoadingIndicator(false);

        verify(mView, never()).showPurchaseFilm(eq(MEDIA_ID_1), eq(true), anyString(), eq(PRICE));
        verify(mView, never()).showPurchaseFilm(eq(MEDIA_ID_2), eq(false), anyString(), eq(PRICE));
        verify(mView).showPlayFilm(eq(MEDIA_ID_1), anyBoolean(), anyString());
        verify(mView, never()).showPlayFilm(eq(MEDIA_ID_2), anyBoolean(), anyString());

        // When play film is requested
        mFilmDetailPresenter.playFilm(MEDIA_ID_1);

        verify(mView).setPreparePlayingIndicator(true);
        verify(mView).setPreparePlayingIndicator(false);

        // getPlaybackValidity is call
//        verify(mPlayerDataSource).getPlaybackValidity(FILM_ID, MEDIA_ID_1);
        verify(mGetPlaybackValidityUseCase).run(
                any(GetPlaybackValidityUseCase.RequestValues.class));

        List<Media> medias = film.getMedias();
        List<String> urls = new ArrayList<>();
        List<String> ranks = new ArrayList<>();
        for (int i = 0; i < medias.size(); i++) {
            urls.add(medias.get(i).getUrl());
        }
        verify(mView).navigateToPlayerActivity(eq(FILM_ID), eq(MEDIA_ID_1), eq(FILM_ID),
                eq(true), eq(Media.TYPE_NO_ENCRYPT), eq(urls), eq(ranks), (List<Ad>) any(),
                anyString(), anyBoolean());
    }
}