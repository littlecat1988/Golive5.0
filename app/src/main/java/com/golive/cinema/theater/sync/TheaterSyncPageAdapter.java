package com.golive.cinema.theater.sync;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.LruCache;
import com.golive.cinema.R;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.views.RotateTextView;
import com.golive.network.response.FilmListResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheaterSyncPageAdapter extends
        RecyclerView.Adapter<TheaterSyncPageAdapter.SimpleViewHolder> {
    private final Fragment mFragment;
    private final LayoutInflater mLayoutInflater;
    private final List<FilmListResponse.Content> mContents;
    private final LruCache<Integer, SimpleViewHolder> mViewHolderLruCache = new LruCache<>(15);
    private final SparseArray<TheaterScript> mScriptArray = new SparseArray<>();
    private final Map<String, Integer> mSubscriptMap = new HashMap<>();
    private final StringBuilder mStringBuilder = new StringBuilder();

    public TheaterSyncPageAdapter(Fragment fragment, List<FilmListResponse.Content> contents) {
        mFragment = fragment;
        mLayoutInflater = LayoutInflater.from(mFragment.getContext());
        mContents = contents;
        mSubscriptMap.put("1", R.drawable.subscript_color1);
        mSubscriptMap.put("2", R.drawable.subscript_color2);
        mSubscriptMap.put("3", R.drawable.subscript_color3);
        mSubscriptMap.put("4", R.drawable.subscript_color4);
        mSubscriptMap.put("5", R.drawable.subscript_color5);
        initPosterScript();
    }

    private void initPosterScript() {
        int size = mContents.size();
        for (int i = 0; i < size; i++) {
            if (null == mContents.get(i).getScript()) {
                continue;
            }
            List<FilmListResponse.ScriptItems> scriptItems = mContents.get(i).getScript()
                    .getScriptItems();
            if (null == scriptItems || scriptItems.isEmpty()) {
                continue;
            }
            int bgResource = 0;
            String prompt = null, subscript_text = null;
            for (FilmListResponse.ScriptItems item : scriptItems) {
                if (StringUtils.isNullOrEmpty(item.getType())) {
                    continue;
                }
                switch (item.getType()) {
                    case "1":
                        prompt = item.getContent();
                        break;
                    case "2":
                        subscript_text = item.getContent();
                        bgResource = getSubscriptBgResource(item.getColor());
                        if (!StringUtils.isNullOrEmpty(subscript_text)) {
                            if (subscript_text.length() < 7) {
                                mStringBuilder.setLength(0);
                                mStringBuilder.append(subscript_text);
                                while (mStringBuilder.length() < 7) {
                                    mStringBuilder.insert(0, " ");
                                }
                                subscript_text = mStringBuilder.toString();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            TheaterScript item = new TheaterScript();
            item.setPrompt(prompt);
            item.setScriptBg(bgResource);
            item.setScriptText(subscript_text);
            mScriptArray.put(i, item);
        }
    }

    public void setIndexNum(int position, boolean visible) {
        SimpleViewHolder simpleViewHolder = mViewHolderLruCache.get(position);
        if (simpleViewHolder != null) {
            simpleViewHolder.shadow.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            simpleViewHolder.index.setText(visible ? position + 1 + "/" + mContents.size() : "");
        }
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.theater_sync_item, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {
        mViewHolderLruCache.put(position, holder);

        Glide.with(mFragment)
                .load(mContents.get(position).getBigposter())
                .placeholder(R.drawable.movie_init_bkg)
                .error(R.drawable.movie_init_bkg)
                .dontAnimate()
                .into(holder.poster);

        TheaterScript script = mScriptArray.get(position);
        if (script != null) {
            String prompt = script.getPrompt();
            if (!StringUtils.isNullOrEmpty(prompt)) {
                holder.prompt.setText(prompt);
                holder.prompt.setVisibility(View.VISIBLE);
            } else {
                holder.prompt.setVisibility(View.INVISIBLE);
            }

            int bg = script.getScriptBg();
            if (bg != 0) {
                holder.script_image.setImageResource(bg);
                holder.script_image.setVisibility(View.VISIBLE);
            } else {
                holder.script_image.setVisibility(View.INVISIBLE);
            }

            String text = script.getScriptText();
            if (!StringUtils.isNullOrEmpty(text)) {
                holder.script_text.setText(text);
            } else {
                holder.script_text.setText("");
            }
        } else {
            holder.prompt.setVisibility(View.INVISIBLE);
            holder.script_image.setVisibility(View.INVISIBLE);
            holder.script_text.setText("");
        }

        holder.itemView.setBackgroundResource((position > 1 && position < mContents.size() - 2)
                ? R.drawable.poster_item_bg_center : R.drawable.poster_item_bg);
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
        if (mContents != null) {
            return mContents.size();
        }
        return 0;
    }

    public class SimpleViewHolder extends RecyclerView.ViewHolder {
        final RotateTextView script_text;
        final ImageView poster;
        final ImageView script_image;
        final TextView index;
        final TextView prompt;
        final View shadow;

        public SimpleViewHolder(View view) {
            super(view);
            index = (TextView) view.findViewById(R.id.title);
            poster = (ImageView) view.findViewById(R.id.image);
            script_text = (RotateTextView) view.findViewById(R.id.script_text);
            script_image = (ImageView) view.findViewById(R.id.script_image);
            prompt = (TextView) view.findViewById(R.id.poster_prompt);
            shadow = view.findViewById(R.id.theater_poster_shadow);
        }
    }

    public class TheaterScript {
        private String prompt;
        private int scriptBg;
        private String scriptText;

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public int getScriptBg() {
            return scriptBg;
        }

        public void setScriptBg(int scriptBg) {
            this.scriptBg = scriptBg;
        }

        public String getScriptText() {
            return scriptText;
        }

        public void setScriptText(String scriptText) {
            this.scriptText = scriptText;
        }
    }
}
