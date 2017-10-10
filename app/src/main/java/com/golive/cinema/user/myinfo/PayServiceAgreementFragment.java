package com.golive.cinema.user.myinfo;

import static com.golive.cinema.user.myinfo.PayServiceAgreementContract.AGREEMENT_TYPE_MONTH;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;

/**
 * Created by Wangzj on 2017/3/6.
 */

public class PayServiceAgreementFragment extends BaseDialog implements
        PayServiceAgreementContract.View {

    private String mAgreementType;
    private TextView mTitleTv;
    private TextView mDetailTv;
    private ProgressDialog mLoadingDialog;
    private PayServiceAgreementContract.Presenter mPresenter;

    public static PayServiceAgreementFragment newInstance(String type) {
        PayServiceAgreementFragment fragment = new PayServiceAgreementFragment();
        Bundle args = new Bundle();
        args.putString(Constants.PAID_SERVICE_AGREEMENT_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAgreementType = getArguments().getString(Constants.PAID_SERVICE_AGREEMENT_TYPE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.user_setting_about_service, container, false);
        mTitleTv = (TextView) view.findViewById(R.id.user_setting_service_title);
        mDetailTv = (TextView) view.findViewById(R.id.user_setting_service_detail);
        Button button = (Button) view.findViewById(R.id.user_setting_service_btn);
        boolean isMonthAgreement = !StringUtils.isNullOrEmpty(mAgreementType)
                && AGREEMENT_TYPE_MONTH.equals(mAgreementType);
        mTitleTv.setText(
                isMonthAgreement ? R.string.vip_monthly : R.string.setting_pay_service_protocol);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Context context = getContext().getApplicationContext();
        PayServiceAgreementContract.Presenter presenter = new PayServiceAgreementPresenter(
                this, Injection.provideGetAgreementUseCase(context));
        presenter.start();
        presenter.getPayServiceAgreement(mAgreementType);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.unsubscribe();
        }
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (!isAdded()) {
            return;
        }

        if (active) {
            if (null == mLoadingDialog) {
                mLoadingDialog = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.please_wait));
            }

            if (!mLoadingDialog.isShowing()) {
                mLoadingDialog.show();
            }
        } else {
            if (mLoadingDialog != null) {
                UIHelper.dismissDialog(mLoadingDialog);
            }
        }
    }

    @Override
    public void showPayServiceAgreement(@Nullable String title, @Nullable String content) {
//        mTitleTv.setText(title);
        CharSequence charSequence;
        if (StringUtils.isNullOrEmpty(content)) {
            final String detailText = getString(R.string.pay_service_agreement);
            charSequence = Html.fromHtml(detailText);
        } else {
            charSequence = content;
        }
        mDetailTv.setText(charSequence);
    }

    @Override
    public void setPresenter(PayServiceAgreementContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }
}
