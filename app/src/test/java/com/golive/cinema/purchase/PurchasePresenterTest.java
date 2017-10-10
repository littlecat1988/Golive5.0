package com.golive.cinema.purchase;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.golive.cinema.data.source.OrdersDataSource;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.order.domain.usecase.CreateOrderUseCase;
import com.golive.cinema.order.domain.usecase.PayCreditOrderUseCase;
import com.golive.cinema.order.domain.usecase.PayOrderUseCase;
import com.golive.cinema.purchase.domain.usecase.PurchaseUseCase;
import com.golive.cinema.user.custom.domain.usecase.GetClientServiceUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserCreditWalletUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserWalletUseCase;
import com.golive.cinema.util.MathExtend;
import com.golive.cinema.util.schedulers.ImmediateSchedulerProvider;
import com.golive.network.entity.Order;
import com.golive.network.entity.PayOrderResult;
import com.golive.network.entity.Wallet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import rx.Observable;

/**
 * Created by Wangzj on 2016/11/12.
 */
public class PurchasePresenterTest {

    @Mock
    private PurchaseContract.View mView;

    @Mock
    private UserDataSource mUserDataSource;

    @Mock
    private OrdersDataSource mOrdersDataSource;

    private PurchasePresenter mPurchasePresenter;

    private final String mProductId = "test_id";
    private final String mProductType = "2";
    private final String mProductName = "test product";
    private final String mMediaId = "";

    @Before
    public void setupPresenter() {

        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

//        initPresenter();

        // The presenter won't update the view unless it's active.
        when(mView.isActive()).thenReturn(true);
    }

    @After
    public void cleanUp() {
        if (mPurchasePresenter != null) {
            mPurchasePresenter.unsubscribe();
            mPurchasePresenter = null;
        }
    }

    private PurchasePresenter initPresenter(String price, boolean creditPay, String encryptionType,
            boolean isOnline) {

        ImmediateSchedulerProvider schedulerProvider = new ImmediateSchedulerProvider();
        GetUserWalletUseCase getUserWalletUseCase = new GetUserWalletUseCase(mUserDataSource,
                schedulerProvider);
        GetUserCreditWalletUseCase getUserCreditWalletUseCase = new GetUserCreditWalletUseCase(
                mUserDataSource,
                schedulerProvider);
        CreateOrderUseCase createOrderUseCase = new CreateOrderUseCase(mOrdersDataSource,
                schedulerProvider);
        PayOrderUseCase payOrderUseCase = new PayOrderUseCase(mOrdersDataSource, schedulerProvider);
        PayCreditOrderUseCase payCreditOrderUseCase = new PayCreditOrderUseCase(mOrdersDataSource,
                schedulerProvider);
        PurchaseUseCase purchaseFilmUseCase = new PurchaseUseCase(createOrderUseCase,
                payOrderUseCase, payCreditOrderUseCase, schedulerProvider);
        GetClientServiceUseCase getClientServiceUseCase = new GetClientServiceUseCase(
                mUserDataSource, schedulerProvider);
        mPurchasePresenter = new PurchasePresenter(mView, mProductId, mProductType, mProductName,
                mMediaId, encryptionType, isOnline, price, 1, creditPay, getUserWalletUseCase,
                getUserCreditWalletUseCase, purchaseFilmUseCase, getClientServiceUseCase,
                schedulerProvider);

        return mPurchasePresenter;
    }

    @Test
    public void loadPurchaseDetail_walletNotEnough() {

        // give a wallet
        Wallet wallet = new Wallet();

        when(mUserDataSource.getWallet()).thenReturn(Observable.just(wallet));

        // init the presenter
        initPresenter("5", false, "", false);

        // When calling loadPurchaseDetail
        mPurchasePresenter.loadPurchaseDetail();

        // verify data source is called
        verify(mUserDataSource).getWallet();

        verify(mView).setLoadingIndicator(true);
        verify(mView).setLoadingIndicator(false);
        // verify show pay amount
        verify(mView).showPayAmount(0);
        // verify show need pay
        verify(mView).showNeedForPay(5);
        verify(mView, never()).showCreditBalance(anyDouble());
        verify(mView, never()).showCreditPayAmount(anyDouble());
        verify(mView, never()).setPurchaseVisible(false);
        verify(mView, never()).setRefundCreditVisible(anyBoolean());
    }

    @Test
    public void loadPurchaseDetail_creditWalletNotEnough() {

        // give a wallet
        Wallet wallet = new Wallet();
        wallet.setValue("-1");
        wallet.setCreditLine("2");

        when(mUserDataSource.getCreditWallet()).thenReturn(Observable.just(wallet));

        // init the presenter
        initPresenter("5", true, "", false);

        // When calling loadPurchaseDetail
        mPurchasePresenter.loadPurchaseDetail();

        // verify data source is called
        verify(mUserDataSource).getCreditWallet();

        verify(mView).setLoadingIndicator(true);
        verify(mView).setLoadingIndicator(false);
        // verify show pay amount
        verify(mView).showPayAmount(0);
        // verify show need pay
        verify(mView).showNeedForPay(4);
        // verify show credit balance
        verify(mView).showCreditBalance(1);
        // verify show credit need pay
        verify(mView).showCreditPayAmount(1);
        verify(mView).setPurchaseVisible(false);
        verify(mView).setRefundCreditVisible(true);
    }

    @Test
    public void purchase_walletEnough() {

        // give a wallet
        Wallet wallet = new Wallet();
        wallet.setValue("10");
        wallet.setCurrency("元");
        when(mUserDataSource.getWallet()).thenReturn(Observable.just(wallet));

        // give a order
        Order order = new Order();
        order.setSerial("123456");
        when(mOrdersDataSource.createOrder(anyString(), anyString(), anyString(), anyString(),
                anyString())).thenReturn(Observable.just(order));
        PayOrderResult value = new PayOrderResult();
        when(mOrdersDataSource.payOrder(anyString())).thenReturn(Observable.just(
                value));

        // init the presenter
        initPresenter("5", false, "", false);

        // When calling purchase
        mPurchasePresenter.purchase();

        // verify data source is called
        verify(mUserDataSource).getWallet();

        // verify createOrder is called
        verify(mOrdersDataSource).createOrder(anyString(), anyString(), anyString(), anyString(),
                anyString());

        // verify payOrder is called
        verify(mOrdersDataSource).payOrder(anyString());

        verify(mView).setPurchasingIndicator(true);
        verify(mView).setPurchasingIndicator(false);
        // verify show purchase success
        verify(mView).showPurchaseSuccess(null, null);
        verify(mView, never()).showPurchaseFailure(null);
    }

    @Test
    public void purchase_walletNotEnough() {

        // give a wallet
        Wallet wallet = new Wallet();
        wallet.setValue("1");
        wallet.setCurrency("元");
        when(mUserDataSource.getWallet()).thenReturn(Observable.just(wallet));

        // not pay
        when(mView.showQrCodePayUI(anyInt(), anyDouble(), anyDouble(), anyDouble(),
                anyDouble())).thenReturn(Observable.just(false));

        // init the presenter
        initPresenter("5", false, "", false);

        // When calling purchase
        mPurchasePresenter.purchase();

        // verify data source is called
        verify(mUserDataSource).getWallet();

        // verify createOrder is never called
        verify(mOrdersDataSource, never()).createOrder(anyString(), anyString(), anyString(),
                anyString(), anyString());

        // verify payOrder is never called
        verify(mOrdersDataSource, never()).payOrder(anyString());

        verify(mView).setPurchasingIndicator(true);
        verify(mView).setPurchasingIndicator(false);
        // verify show QrCode Pay UI
        verify(mView).showQrCodePayUI(eq(PurchaseContract.QR_CODE_PAY_TYPE_TOP_UP), eq(4.0),
                eq(1.0), anyDouble(), anyDouble());
        verify(mView, never()).showPurchaseSuccess(null, null);
        verify(mView, never()).showPurchaseFailure(null);
    }

    @Test
    public void purchase_walletNotEnough_qrCode_pay_success() {

        // init the presenter
        initPresenter("5", false, "", false);

        // give a wallet
        final Wallet wallet = new Wallet();
        wallet.setValue("1");
        wallet.setCurrency("元");
        when(mUserDataSource.getWallet()).thenReturn(Observable.just(wallet));

        // give a order
        Order order = new Order();
        order.setSerial("123456");
        when(mOrdersDataSource.createOrder(anyString(), anyString(), anyString(), anyString(),
                anyString())).thenReturn(Observable.just(order));
        PayOrderResult payOrderResult = new PayOrderResult();
        payOrderResult.setOrder(order);
        when(mOrdersDataSource.payOrder(anyString())).thenReturn(Observable.just(
                payOrderResult));

        Mockito.doAnswer(new Answer() {
            @Override
            public Observable<Boolean> answer(InvocationOnMock invocation) throws Throwable {

                Object[] arguments = invocation.getArguments();
                Double pay = (Double) arguments[1];

                // add pay amount to current wallet
                String value = MathExtend.add(wallet.getValue(), pay.toString());

                wallet.setValue(value);
                wallet.setCurrency("元");
                // update the wallet
                when(mUserDataSource.getWallet()).thenReturn(Observable.just(wallet));

                // QR code pay success
                return Observable.just(true);
            }
        }).when(mView).showQrCodePayUI(anyInt(), anyDouble(), anyDouble(), anyDouble(),
                anyDouble());

        // When calling purchase
        mPurchasePresenter.purchase();

        // verify data source is called at least 2 times
        verify(mUserDataSource, atLeast(2)).getWallet();

        // verify show QrCode Pay UI
        verify(mView).showQrCodePayUI(eq(PurchaseContract.QR_CODE_PAY_TYPE_TOP_UP), eq(4.0),
                eq(1.0), anyDouble(), anyDouble());

        // verify createOrder is called
        verify(mOrdersDataSource).createOrder(anyString(), anyString(), anyString(),
                anyString(), anyString());

        // verify payOrder is called
        verify(mOrdersDataSource).payOrder(anyString());

        verify(mView).setPurchasingIndicator(true);
        verify(mView).setPurchasingIndicator(false);

        verify(mView).showPurchaseSuccess(order, null);
        verify(mView, never()).showPurchaseFailure(null);
    }

}