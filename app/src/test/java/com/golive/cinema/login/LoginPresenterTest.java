package com.golive.cinema.login;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.login.domain.usecase.LoginUseCase;
import com.golive.cinema.util.schedulers.ImmediateSchedulerProvider;
import com.golive.network.entity.Login;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;

/**
 * Created by Wangzj on 2016/9/6.
 */
public class LoginPresenterTest {

    //@Rule
    //public RxSchedulersOverrideRule myRule = new RxSchedulersOverrideRule();

    @Mock
    private UserDataSource mUserDataSource;

    @Mock
    private LoginContract.View mView;

    private LoginPresenter mLoginPresenter;

    @Before
    public void setupTasksPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mLoginPresenter = givenLoginPresenter();

        // The presenter won't update the view unless it's active.
        when(mView.isActive()).thenReturn(true);
    }

    private LoginPresenter givenLoginPresenter() {
        LoginUseCase useCase = new LoginUseCase(mUserDataSource, new ImmediateSchedulerProvider());
        return new LoginPresenter(mView, useCase);
    }

    @Test
    public void loginSuccess() throws Exception {

        // mock
        Login login = new Login();
        when(mUserDataSource.login(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString())).thenReturn(
                Observable.just(login));

        // login
        mLoginPresenter.login(null, null, null);

        // Then progress indicator is shown
        verify(mView).setLoadingIndicator(true);
        // Then progress indicator is hidden and all tasks are shown in UI
        verify(mView).setLoadingIndicator(false);
        verify(mView).showLogin(login);
    }
}