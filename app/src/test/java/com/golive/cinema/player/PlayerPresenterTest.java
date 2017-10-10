package com.golive.cinema.player;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.golive.cinema.advert.domain.usecase.AdvertUseCase;
import com.golive.cinema.data.source.KdmDataSource;
import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.data.source.OrdersDataSource;
import com.golive.cinema.data.source.PlayerDataSource;
import com.golive.cinema.data.source.RecommendDataSource;
import com.golive.cinema.data.source.ServerInitDataSource;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.init.domain.usecase.GetMainConfigUseCase;
import com.golive.cinema.order.domain.usecase.GetValidOrderUseCase;
import com.golive.cinema.player.domain.usecase.GetKdmInitUseCase;
import com.golive.cinema.recommend.domain.usecase.GetMovieRecommendUseCase;
import com.golive.cinema.user.history.domain.usecase.AddHistoryUseCase;
import com.golive.cinema.user.myinfo.domain.usecase.GetUserInfoUseCase;
import com.golive.cinema.util.schedulers.ImmediateSchedulerProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Created by Wangzj on 2016/9/22.
 */
public class PlayerPresenterTest {

    @Mock
    private PlayerContract.View mView;

    @Mock
    private PlayerOperation mPlayerOperation;

    @Mock
    private RecommendDataSource mRecommendDataSource;

    @Mock
    private PlayerDataSource mPlayerDataSource;

    @Mock
    private UserDataSource mUserDataSource;

    @Mock
    private OrdersDataSource mOrdersDataSource;

    @Mock
    private KdmDataSource mKdmDataSource;

    @Mock
    private MainConfigDataSource mMainConfigDataSource;

    @Mock
    private ServerInitDataSource mServerInitDataSource;

    private PlayerPresenter mPlayerPresenter;
    private PlayerCallback mPlayerCallback;

    @Before
    public void setupPlayerPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // The presenter won't update the view unless it's active.
        when(mView.isActive()).thenReturn(true);

        mPlayerPresenter = givenPlayerPresenter();
    }

    @After
    public void cleanup() {
        mPlayerCallback = null;
    }

    @Test
    public void startPlayerSuccessAndCallbackToView() {

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (mPlayerCallback != null) {

                    // player start preparing
                    mPlayerCallback.onPlayerPreparing();

                    // simulate preparing media...
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // player prepared
                    mPlayerCallback.onPlayerPrepared();
                }
                return null;
            }
        }).when(mPlayerOperation).startPlayer();

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (mPlayerCallback != null) {

                    // player start
                    mPlayerCallback.onPlayerStart();

                    // simulate playing media...
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // player completed
                    mPlayerCallback.onPlayerCompleted();
                }
                return null;
            }
        }).when(mPlayerOperation).resumePlayer();

        // start player
        mPlayerPresenter.startPlayer();

        verify(mView).setPlayingIndicator(eq(true), anyInt(), false);
        verify(mView).setPlayingIndicator(eq(false), anyInt(), false);
        verify(mView, atLeast(4)).updatePausePlayUI();
        verify(mView).updatePlayerProgress();
        verify(mView).showPlayerCompleted();
    }

    @Test
    public void startPlayerFailedAndCallbackToView() {
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (mPlayerCallback != null) {

                    // player start preparing
                    mPlayerCallback.onPlayerPreparing();

                    // simulate preparing media...
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // player prepared
                    mPlayerCallback.onPlayerError(-1, -1, null);
                }
                return null;
            }
        }).when(mPlayerOperation).startPlayer();

        // start player
        mPlayerPresenter.startPlayer();

        verify(mView).setPlayingIndicator(eq(true), anyInt(), false);
        verify(mView).setPlayingIndicator(eq(false), anyInt(), false);
        verify(mView, atLeast(2)).updatePausePlayUI();
        verify(mView).showPlayerError(-1, -1, null);
    }

    private PlayerPresenter givenPlayerPresenter() {
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                // save the callback arg
                mPlayerCallback = (PlayerCallback) args[0];
                return null;
            }
        }).when(mPlayerOperation).setPlayerCallback(any(PlayerCallback.class));

        ImmediateSchedulerProvider schedulerProvider = new ImmediateSchedulerProvider();
        GetKdmInitUseCase getKdmInitUseCase = new GetKdmInitUseCase(schedulerProvider,
                mKdmDataSource, mMainConfigDataSource);
        GetMovieRecommendUseCase getMovieRecommendUseCase = new GetMovieRecommendUseCase(
                mRecommendDataSource, getKdmInitUseCase, schedulerProvider);
        AddHistoryUseCase addHistoryTask = new AddHistoryUseCase(mUserDataSource,
                schedulerProvider);
        GetUserInfoUseCase getUserInfoUseCase = new GetUserInfoUseCase(mUserDataSource,
                schedulerProvider);
        GetValidOrderUseCase getValidOrderUseCase = new GetValidOrderUseCase(mOrdersDataSource,
                schedulerProvider);
        AdvertUseCase advertUseCase = new AdvertUseCase(mMainConfigDataSource,
                mServerInitDataSource, schedulerProvider);
        GetMainConfigUseCase getMainConfigUseCase = new GetMainConfigUseCase(mMainConfigDataSource,
                schedulerProvider);
        PlayerPresenter playerPresenter = new PlayerPresenter(mView, getMainConfigUseCase,
                getMovieRecommendUseCase,
                getUserInfoUseCase, addHistoryTask, getValidOrderUseCase, advertUseCase,
                schedulerProvider, "", "", false);
        playerPresenter.setPlayerOperation(mPlayerOperation);
        return playerPresenter;
    }
}