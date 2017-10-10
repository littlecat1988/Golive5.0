package com.golive.cinema.user.message;

import android.content.Context;
import android.os.Bundle;

import com.golive.cinema.BaseActivity;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.util.ActivityUtils;

public class MessageActivity extends BaseActivity {

    public static final String MESSAGE_LIST_TAG = "message_list_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_center_base);

//        Serializable homeMessages = getIntent().getSerializableExtra(MESSAGE_LIST_TAG);
//        Bundle bundle = null;
//        if (homeMessages != null) {
//            bundle = new Bundle();
//            bundle.putSerializable(MESSAGE_LIST_TAG, homeMessages);
//        }

        MessageFragment fragment = MessageFragment.newInstance();
        Context context = getApplication();
        new MessagePresenter(fragment,
                Injection.provideCreditOperationUseCase(context),
                Injection.provideFinanceMessageUseCase(context),
                Injection.provideServerStatusUseCase(context),
                Injection.provideSchedulerProvider());
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                fragment, R.id.userContentFrame);
    }
}
