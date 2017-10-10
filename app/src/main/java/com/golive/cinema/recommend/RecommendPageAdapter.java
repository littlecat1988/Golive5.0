package com.golive.cinema.recommend;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.golive.cinema.R;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.views.RotateTextView;
import com.golive.cinema.views.metroviews.widget.SpannableGridLayoutManager;
import com.golive.network.response.RecommendResponse;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendPageAdapter extends
        RecyclerView.Adapter<RecommendPageAdapter.RecommendViewHolder> {
    public int mTabFocusPosition = -1;
    public int mLastFocusPosition;
    public boolean mIsMore;
    private int mCount = 9;
    private int mXLmt, mYLmt;
    private final float mFontScale;
    private final LayoutInflater mLayoutInflater;
    private final Fragment mFragment;
    private final List<RecommendResponse.Items> mItemsList;
//    private final SparseArray<RecommendViewHolder> mViewArray = new SparseArray<>();
    private final SparseArray<FocusPref> mFocusArray = new SparseArray<>();
    private final Map<String, Integer> mSubscriptMap = new HashMap<>();
    private final StringBuilder mStringBuilder = new StringBuilder();

    public RecommendPageAdapter(Fragment fragment, List<RecommendResponse.Items> list) {
        mFragment = fragment;
        mLayoutInflater = LayoutInflater.from(mFragment.getContext());
        mItemsList = list;
        mCount = list.size();
        mFontScale = fragment.getResources().getDisplayMetrics().scaledDensity;
        initData();
    }

    private void initData() {
        mSubscriptMap.put("1", R.drawable.subscript_color1);
        mSubscriptMap.put("2", R.drawable.subscript_color2);
        mSubscriptMap.put("3", R.drawable.subscript_color3);
        mSubscriptMap.put("4", R.drawable.subscript_color4);
        mSubscriptMap.put("5", R.drawable.subscript_color5);
        sortList();
        findLimit();
        findFocusPrefer();
    }

    private void sortList() {
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

    @Override
    public RecommendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.recommend_item, parent, false);
        return new RecommendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecommendViewHolder holder, int position) {
//        mViewArray.put(position, holder);
        RecommendResponse.Items items = mItemsList.get(position);
        if (null == items) {
            return;
        }
        String actionContent = items.getActionContent();
        boolean isNoContent = StringUtils.isNullOrEmpty(actionContent) || "-1".equals(
                actionContent);

        holder.loadTips.setVisibility(isNoContent ? View.VISIBLE : View.INVISIBLE);
        holder.defaultBg.setVisibility(!isNoContent ? View.VISIBLE : View.INVISIBLE);
        holder.image.setVisibility(isNoContent ? View.INVISIBLE : View.VISIBLE);
        holder.title.setText(isNoContent ? "" : items.getActionType() != 1 ? items.getTitle() : "");
        holder.prompt.setVisibility(View.INVISIBLE);
        holder.subscript_image.setVisibility(View.INVISIBLE);
        holder.subscript_text.setVisibility(View.INVISIBLE);

        if (!isNoContent) {
            Glide.with(mFragment)
                    .load(items.getBackgroundPic())
                    .error(android.R.color.transparent)
                    .into(holder.image);

            RecommendResponse.Script script = items.getScript();
            if (script != null && script.getScriptItems() != null
                    && !script.getScriptItems().isEmpty()) {
                List<RecommendResponse.ScriptItems> scriptItems = script.getScriptItems();
                for (RecommendResponse.ScriptItems item : scriptItems) {
                    String type = item.getType();
                    String content = item.getContent();
                    if (StringUtils.isNullOrEmpty(type)) {
                        continue;
                    }
                    switch (type) {
                        case "1":
                            if (!StringUtils.isNullOrEmpty(content)) {
                                holder.prompt.setVisibility(View.VISIBLE);
                                holder.prompt.setText(content);
                            }
                            break;
                        case "2":
                            if (!StringUtils.isNullOrEmpty(content)) {
                                if (content.length() < 7) {
                                    mStringBuilder.setLength(0);
                                    mStringBuilder.append(content);
                                    while (mStringBuilder.length() < 7) {
                                        mStringBuilder.insert(0, " ");
                                    }
                                    content = mStringBuilder.toString();
                                    item.setContent(content);
                                }

                                holder.subscript_image.setVisibility(View.VISIBLE);
                                holder.subscript_image.setImageResource(
                                        getSubscriptBgResource(item.getColor()));
                                holder.subscript_text.setVisibility(View.VISIBLE);
                                holder.subscript_text.setText(content);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        View itemView = holder.itemView;
        SpannableGridLayoutManager.LayoutParams lp =
                (SpannableGridLayoutManager.LayoutParams) itemView.getLayoutParams();

        RecommendResponse.Location location = items.getLocation();
        int row = location.getH();
        int col = location.getW();
        if (lp.rowSpan != row || lp.colSpan != col) {
            lp.rowSpan = row;
            lp.colSpan = col;
            itemView.setLayoutParams(lp);
            if (holder.prompt.getVisibility() == View.VISIBLE) {
                holder.prompt.setTextSize(px2sp(mFragment.getResources()
                        .getDimensionPixelSize(R.dimen.recommend_item2_prompt_text_size)));
            }

            int width = (int) mFragment.getResources().getDimension(
                    R.dimen.recommend_item2_subscript_width);
            if (holder.subscript_image.getVisibility() == View.VISIBLE) {
                FrameLayout.LayoutParams imageLp =
                        (FrameLayout.LayoutParams) holder.subscript_image.getLayoutParams();
                imageLp.width = width;
                imageLp.height = width;
                holder.subscript_image.setLayoutParams(imageLp);
            }

            if (holder.subscript_text.getVisibility() == View.VISIBLE) {
                int margin = (int) mFragment.getResources().getDimension(
                        R.dimen.recommend_item2_subscript_text_top);
                FrameLayout.LayoutParams textLp =
                        (FrameLayout.LayoutParams) holder.subscript_text.getLayoutParams();
                textLp.width = width;
                textLp.setMargins(0, margin, margin, 0);
                holder.subscript_text.setLayoutParams(textLp);
                holder.subscript_text.setTextSize(px2sp(mFragment.getResources()
                        .getDimensionPixelSize(R.dimen.recommend_item2_subscript_text_size)));
            }
        }
    }

    private int px2sp(float pxValue) {
        return (int) (pxValue / mFontScale + 0.5f);
    }

    private int getSubscriptBgResource(String type) {
        Integer res = mSubscriptMap.get(type);
        if (res != null) {
            return res;
        } else {
            return R.color.translucent;
        }
    }

    @Override
    public int getItemCount() {
        return mCount;
    }

//    public boolean isFocusPrefer(int position, int keyCode) {
//        FocusPref pref = mFocusArray.get(position);
//        if (pref != null) {
//            int pos = -1;
//            switch (keyCode) {
//                case KeyEvent.KEYCODE_DPAD_DOWN:
//                    pos = pref.getDown();
//                    break;
//                case KeyEvent.KEYCODE_DPAD_UP:
//                    pos = pref.getUp();
//                    break;
//                case KeyEvent.KEYCODE_DPAD_LEFT:
//                    pos = pref.getLeft();
//                    break;
//                case KeyEvent.KEYCODE_DPAD_RIGHT:
//                    pos = pref.getRight();
//                    break;
//                default:
//                    break;
//            }
//
//            if (pos != -1) {
//                RecommendViewHolder viewHolder = mViewArray.get(pos);
//                if (viewHolder != null) {
//                    viewHolder.itemView.requestFocus();
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }

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

            if (!mIsMore && (location.getX() + location.getW() > 5)) {
                mIsMore = true;
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

            //寻找离底部导航"推荐"按钮最近的那个格子
            if (mTabFocusPosition == -1 && (h + y == 3) && (w + x == 2)) {
                mTabFocusPosition = j;
            }

            if (w > 1 || h > 1) {
                int xM = mXLmt, yM = mYLmt, pos1 = -1, pos2 = -1,
                        pos3 = -1, pos4 = -1, x0 = -1, y0 = -1;

                for (i = 0; i < mCount; i++) {
                    RecommendResponse.Location location2 = mItemsList.get(i).getLocation();
                    if (w > 1) {
                        if (x == location2.getX()) {
                            temp = location2.getY();
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
                        if (y == location2.getY()) {
                            temp = location2.getX();
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
                    FocusPref mFocusPref = new FocusPref(pos1, pos2, pos3, pos4);
                    mFocusArray.put(j, mFocusPref);
                }
            }
        }
    }

    public class RecommendViewHolder extends RecyclerView.ViewHolder {
        final RotateTextView subscript_text;
        final TextView title;
        final TextView prompt;
        final TextView loadTips;
        final ImageView image;
        final ImageView subscript_image;
        final View defaultBg;

        public RecommendViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            loadTips = (TextView) view.findViewById(R.id.load_text);
            image = (ImageView) view.findViewById(R.id.image);
            subscript_text = (RotateTextView) view.findViewById(R.id.subscript_text);
            subscript_image = (ImageView) view.findViewById(R.id.subscript_image);
            prompt = (TextView) view.findViewById(R.id.poster_prompt);
            defaultBg = view.findViewById(R.id.default_iv);
        }
    }

    public class FocusPref {
        private final int left;
        private final int right;
        private final int up;
        private final int down;

        public FocusPref(int down, int up, int left, int right) {
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
}
