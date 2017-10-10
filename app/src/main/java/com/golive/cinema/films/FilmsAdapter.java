package com.golive.cinema.films;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.golive.cinema.R;
import com.golive.network.entity.Film;

import java.util.List;

/**
 * Created by Wangzj on 2016/9/14.
 */

public class FilmsAdapter extends RecyclerView.Adapter<FilmsAdapter.FilmViewHolder> {

    private List<Film> mFilms;

    public FilmsAdapter(List<Film> films) {
        setList(films);
    }

    public void replaceData(List<Film> films) {
        setList(films);
        notifyDataSetChanged();
    }

    private void setList(List<Film> films) {
        mFilms = checkNotNull(films);
    }

    @Override
    public void onBindViewHolder(FilmsAdapter.FilmViewHolder holder, final int position) {
        final Film film = getItem(position);
        holder.mTitleTV.setText(film.getName());
    }

    @Override
    public FilmsAdapter.FilmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View rowView = inflater.inflate(R.layout.film_item, parent, false);
        return new FilmsAdapter.FilmViewHolder(rowView);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return mFilms.size();
    }

    public Film getItem(int position) {
        return mFilms.get(position);
    }

    class FilmViewHolder extends RecyclerView.ViewHolder {

        final TextView mTitleTV;

        FilmViewHolder(View itemView) {
            super(itemView);
            mTitleTV = (TextView) itemView.findViewById(R.id.title);
        }
    }
}
