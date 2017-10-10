package com.golive.cinema.filmlibrary;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.golive.cinema.R;
import com.golive.cinema.adapter.RecyclerViewAdapterListener;
import com.golive.cinema.filmlibrary.dimain.model.PosterScript;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.views.RotateTextView;
import com.golive.network.response.FilmLibListResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilmLibraryAdapter extends RecyclerView.Adapter<FilmLibraryAdapter.LibraryViewHolder> {

    private final Fragment mFragment;
    private final LayoutInflater mLayoutInflater;
    private int mCount, pmPosVarsVar;
    private boolean mPosFlag, mFirstFlag;
    private List<FilmLibListResponse.Content> mContents;
    private final SparseArray<PosterScript> mScriptSparseArray = new SparseArray<>();
    private final Map<String, Integer> mSubscriptMap = new HashMap<>();

    private RecyclerViewAdapterListener mAdapterListener;

    public FilmLibraryAdapter(Fragment fragment) {
        mFragment = fragment;
        mLayoutInflater = LayoutInflater.from(mFragment.getContext());
        mFirstFlag = true;
        mSubscriptMap.put("1", R.drawable.subscript_color1);
        mSubscriptMap.put("2", R.drawable.subscript_color2);
        mSubscriptMap.put("3", R.drawable.subscript_color3);
        mSubscriptMap.put("4", R.drawable.subscript_color4);
        mSubscriptMap.put("5", R.drawable.subscript_color5);
    }

    private void updateCount(List<FilmLibListResponse.Content> contents) {
        mCount = contents != null ? contents.size() : 0;
    }

    public void setData(List<FilmLibListResponse.Content> listCnt) {
        pmPosVarsVar = 0;
        mPosFlag = true;
        mContents = listCnt;
        updateCount(listCnt);
        mScriptSparseArray.clear();
        initScript();
        if (!mFirstFlag) {
            notifyDataSetChanged();
        }
    }

    private void initScript() {
        for (int i = 0; i < mCount; i++) {
            FilmLibListResponse.Script script = mContents.get(i).getScript();
            if (null == script || null == script.getScriptItems()
                    || script.getScriptItems().isEmpty()) {
                continue;
            }
            List<FilmLibListResponse.ScriptItems> scriptItems = script.getScriptItems();
            int bgResource = 0;
            String prompt = null, str = null;
            for (FilmLibListResponse.ScriptItems item : scriptItems) {
                String type = item.getType();
                if (StringUtils.isNullOrEmpty(type)) {
                    continue;
                }
                if ("1".equals(type)) {
                    prompt = item.getContent();
                } else if ("2".equals(type)) {
                    bgResource = getScriptBgResource(item.getColor());
                    str = item.getContent();
                }
            }

            PosterScript mItem = new PosterScript();
            mItem.setPrompt(prompt);
            mItem.setScriptBg(bgResource);
            mItem.setScriptText(str);
            mScriptSparseArray.put(i, mItem);
        }
    }

    //翻页
    /*public void setNextPage(boolean type) {
        pmPosVarsVar = (type ? pmPosVarsVar - 7 : pmPosVarsVar + 7);
        notifyDataSetChanged();
    }*/

    public void clearData() {
        //mContents.clear();
        mScriptSparseArray.clear();
    }

    @Override
    public LibraryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mLayoutInflater.inflate(R.layout.film_library_item, parent,
                false);
        return new LibraryViewHolder(view);
    }

    public class LibraryViewHolder extends RecyclerView.ViewHolder {
        private final RotateTextView script_text;
        final ImageView poster;
        private final ImageView script_bg;
        private final TextView index;
        private final TextView prompt;

        public LibraryViewHolder(View view) {
            super(view);
            index = (TextView) view.findViewById(R.id.title);
            poster = (ImageView) view.findViewById(R.id.poster);
            script_text = (RotateTextView) view.findViewById(R.id.script_text);
            script_bg = (ImageView) view.findViewById(R.id.script_bg);
            prompt = (TextView) view.findViewById(R.id.poster_prompt);
        }
    }

    @Override
    public void onViewRecycled(LibraryViewHolder holder) {
        super.onViewRecycled(holder);
        if (mAdapterListener != null) {
            mAdapterListener.onViewRecycled(holder);
        }
    }

    @Override
    public void onViewAttachedToWindow(LibraryViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (mAdapterListener != null) {
            mAdapterListener.onViewAttachedToWindow(holder);
        }
    }

    @Override
    public void onViewDetachedFromWindow(LibraryViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (mAdapterListener != null) {
            mAdapterListener.onViewDetachedFromWindow(holder);
        }
    }

    @Override
    public void onBindViewHolder(LibraryViewHolder holder, int position) {
        if (mPosFlag) {
            mPosFlag = false;
            if (mCount > 2) {
                int firstPos = mCount - 3;
                if (mFirstFlag) {
                    mFirstFlag = false;
                    firstPos = 0;
                }
                while (((position + pmPosVarsVar) % mCount) != firstPos) {
                    if (pmPosVarsVar >= Integer.MAX_VALUE) {
                        pmPosVarsVar = 0;
                    }
                    pmPosVarsVar++;
                }
            } else {
                mFirstFlag = false;
            }
        }

        int pos = (position + pmPosVarsVar) % mCount;
        //holder.title.setText(pos + "\n" + mList.get(pos).getName());

        String posterUrl = mContents.get(pos).getBigposter();
        loadPoster(posterUrl, holder.poster);

        PosterScript script = mScriptSparseArray.get(pos);
        if (script != null) {
            String prompt = script.getPrompt();
            if (!StringUtils.isNullOrEmpty(prompt)) {
                holder.prompt.setText(prompt);
                holder.prompt.setVisibility(View.VISIBLE);
            } else {
                holder.prompt.setVisibility(View.INVISIBLE);
            }

            int scriptBg = script.getScriptBg();
            if (scriptBg != 0) {
                holder.script_bg.setImageResource(scriptBg);
                holder.script_bg.setVisibility(View.VISIBLE);
            } else {
                holder.script_bg.setVisibility(View.INVISIBLE);
            }

            String scriptText = script.getScriptText();
            if (!StringUtils.isNullOrEmpty(scriptText)) {
                holder.script_text.setText(scriptText);
            } else {
                holder.script_text.setText("");
            }
        } else {
            holder.prompt.setVisibility(View.INVISIBLE);
            holder.script_bg.setVisibility(View.INVISIBLE);
            holder.script_text.setText("");
        }
    }

    private void loadPoster(final String url, ImageView igv) {
        Glide.with(mFragment)
                .load(url)
                .asBitmap()
                .error(R.drawable.movie_init_bkg)
                .priority(Priority.HIGH)
                .into(igv);
    }

    private int getScriptBgResource(String type) {
        Integer res = mSubscriptMap.get(type);
        if (res != null) {
            return res;
        } else {
            return R.color.translucent;
        }
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public FilmLibListResponse.Content getItem(int position) {
        if (mContents != null) {
            return mContents.get(position);
        }

        return null;
    }

    public void setAdapterListener(RecyclerViewAdapterListener adapterListener) {
        mAdapterListener = adapterListener;
    }
}
