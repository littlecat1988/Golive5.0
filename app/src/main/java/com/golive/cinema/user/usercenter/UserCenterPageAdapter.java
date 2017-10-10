package com.golive.cinema.user.usercenter;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.golive.cinema.R;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.views.metroviews.widget.SpannableGridLayoutManager;
import com.golive.network.response.RecommendResponse;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserCenterPageAdapter extends
        RecyclerView.Adapter<UserCenterPageAdapter.UserViewHolder> {
    int mTabFocusPosition = -1;
    int mLastFocusPosition;
    private final Fragment mFragment;
    private final List<RecommendResponse.Items> mItemsList;
    private final SparseArray<UserViewHolder> mViewArray = new SparseArray<>();
    private final SparseArray<UserFocusPref> mFocusArray = new SparseArray<>();
    private final LayoutInflater mLayoutInflater;
    private int mCount = 8;
    private int mMessageCount = -1;
    private int mXLmt, mYLmt;
    private final int[] mStrIds = {R.string.user_center_tab_vip_open,
            R.string.user_center_tab_my_account, R.string.topup,
            R.string.user_center_tab_history,
            R.string.user_center_tab_set_system,
            R.string.user_my_consumption,
            R.string.user_center_message,
            R.string.user_center_tab_tell_us};

    public UserCenterPageAdapter(Fragment fragment, List<RecommendResponse.Items> list) {
        mFragment = fragment;
        mItemsList = list;
        if (list != null) {
            mCount = list.size();
        }
        mLayoutInflater = LayoutInflater.from(mFragment.getContext());
        sortList();
        findLimit();
        findFocusPrefer();
    }

    private void sortList() {
        if (null == mItemsList) {
            return;
        }
        Collections.sort(mItemsList, new Comparator<RecommendResponse.Items>() {
            @Override
            public int compare(RecommendResponse.Items lhs, RecommendResponse.Items rhs) {
                int var0 = Integer.parseInt(String.valueOf(lhs.getLocation().getX())
                        + String.valueOf(lhs.getLocation().getY()));
                int var1 = Integer.parseInt(String.valueOf(rhs.getLocation().getX())
                        + String.valueOf(rhs.getLocation().getY()));
                if (var0 > var1) {
                    return 1;
                } else if (var0 == var1) {
                    return 0;
                }
                return -1;
            }
        });
    }

    public void setMessageTips(int messageTips) {
        if (mMessageCount == 0) {
            return;
        }
        if (mItemsList != null && !mItemsList.isEmpty()) {
            RecommendResponse.Items item;
            for (int i = 0; i < mCount; i++) {
                item = mItemsList.get(i);
                UserViewHolder userViewHolder = mViewArray.get(i);
                TextView message = null;
                if (userViewHolder != null) {
                    message = userViewHolder.message;
                }
                if (message != null) {
                    String actionContent = item.getActionContent();
                    if (!StringUtils.isNullOrEmpty(actionContent) && "7".equals(actionContent)) {
                        mMessageCount = messageTips;
                        if (mMessageCount > 0) {
                            if (mMessageCount > 0 && mMessageCount < 100) {
                                message.setText(String.valueOf(mMessageCount));
                            } else {
                                message.setText(mFragment.getString(R.string.user_msg_count_max));
                            }
                            message.setVisibility(View.VISIBLE);
                        } else {
                            message.setVisibility(View.GONE);
                        }
                    } else {
                        message.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    void setItemImage(boolean focus, int position) {
        RecommendResponse.Items items = mItemsList.get(position);
        if (null == items) {
            return;
        }

        String urlStr = focus ? items.getFocusBackgroundPic() : items.getBackgroundPic();
        if (!StringUtils.isNullOrEmpty(urlStr)) {
            Glide.with(mFragment)
                    .load(urlStr)
                    .error(R.color.item_default_color)
                    .into(mViewArray.get(position).image);
        }
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.user_center_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        mViewArray.put(position, holder);
        //holder.title.setText(mItemsList.get(position).getSubTitle());
        RecommendResponse.Items items = mItemsList.get(position);
        String urlStr = items.getBackgroundPic();
        if (StringUtils.isNullOrEmpty(urlStr)) {
            if (position < mStrIds.length) {
                holder.title.setText(mStrIds[position]);
            }
        }
//        else {
//        }
        Glide.with(mFragment)
                .load(urlStr)
                .error(R.color.item_default_color)
                .into(holder.image);

        View itemView = holder.itemView;
        SpannableGridLayoutManager.LayoutParams lp =
                (SpannableGridLayoutManager.LayoutParams) itemView.getLayoutParams();

        int row = items.getLocation().getH();
        int col = items.getLocation().getW();
        if (lp.rowSpan != row || lp.colSpan != col) {
            lp.rowSpan = row;
            lp.colSpan = col;
            itemView.setLayoutParams(lp);
        }
    }

    @Override
    public int getItemCount() {
        if (mItemsList != null) {
            return mItemsList.size();
        }
        return 0;
    }

    public boolean isFocusPrefer(int position, int keyCode) {
        UserFocusPref pref = mFocusArray.get(position);
        if (pref != null) {
            int pos = -1;
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    pos = pref.getDown();
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    pos = pref.getUp();
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    pos = pref.getLeft();
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    pos = pref.getRight();
                    break;
                default:
                    break;
            }

            if (pos != -1) {
                mViewArray.get(pos).itemView.requestFocus();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void findLimit() {
        for (int i = 0; i < mCount; i++) {
            RecommendResponse.Location location = mItemsList.get(i).getLocation();
            if (mXLmt < location.getX()) {
                mXLmt = location.getX();
                if (location.getY() == 0) {
                    mLastFocusPosition = i;
                }
            }

            if (mYLmt < location.getY()) {
                mYLmt = location.getY();
            }
        }
    }

    /**
     * 焦点偏好规则:大格子往小格子上下跳到相邻左边的小格子，左右跳到相邻上边的格子
     */
    private void findFocusPrefer() {
        int i, j, x, y, w, h, temp;
        for (j = 0; j < mCount; j++) {
            RecommendResponse.Location location = mItemsList.get(j).getLocation();
            x = location.getX();
            y = location.getY();
            w = location.getW();
            h = location.getH();

            //寻找离底部导航"用户中心"按钮最近的那个格子
            if (mTabFocusPosition == -1 && (h + y == 2) && (w + x == 5)) {
                mTabFocusPosition = j;
            }

            if (w > 1 || h > 1) {
                int xM = mXLmt, yM = mYLmt, pos1 = -1, pos2 = -1,
                        pos3 = -1, pos4 = -1, x0 = -1, y0 = -1;

                for (i = 0; i < mCount; i++) {
                    if (w > 1) {
                        if (x == mItemsList.get(i).getLocation().getX()) {
                            temp = mItemsList.get(i).getLocation().getY();
                            if (y < mYLmt && temp > y && temp < yM) {
                                yM = temp;
                                pos1 = i;
                            }
                            if (y > 0 && temp < y && temp > y0) {
                                y0 = temp;
                                pos2 = i;
                            }
                        }
                    }

                    if (h > 1) {
                        if (y == mItemsList.get(i).getLocation().getY()) {
                            temp = mItemsList.get(i).getLocation().getX();
                            if (x > 0 && temp < x && temp > x0) {
                                x0 = temp;
                                pos3 = i;
                            }
                            if (x < mXLmt && temp > x && temp < xM) {
                                xM = temp;
                                pos4 = i;
                            }
                        }
                    }
                }

                if (pos1 != -1 || pos2 != -1 || pos3 != -1 || pos4 != -1) {
                    UserFocusPref mFocusPref = new UserFocusPref(pos1, pos2, pos3, pos4);
                    mFocusArray.put(j, mFocusPref);
                }
            }
        }
    }


    public class UserFocusPref {
        private final int left;
        private final int right;
        private final int up;
        private final int down;

        public UserFocusPref(int down, int up, int left, int right) {
            this.down = down;
            this.up = up;
            this.left = left;
            this.right = right;
        }

        public int getLeft() {
            return left;
        }

        public int getRight() {
            return right;
        }

        public int getUp() {
            return up;
        }

        public int getDown() {
            return down;
        }
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final ImageView image;
        final TextView message;

        public UserViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            image = (ImageView) view.findViewById(R.id.image);
            message = (TextView) view.findViewById(R.id.user_message_tips_tv);
        }
    }
}
