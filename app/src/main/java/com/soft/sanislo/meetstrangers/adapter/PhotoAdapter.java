package com.soft.sanislo.meetstrangers.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.MediaFile;
import com.soft.sanislo.meetstrangers.view.PhotViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by root on 27.11.16.
 */

public class PhotoAdapter extends RecyclerView.Adapter<PhotViewHolder> {
    private List<MediaFile> mMediaFiles;


    public PhotoAdapter(List<MediaFile> mediaFiles) {
        mMediaFiles = mediaFiles;
    }

    @Override
    public PhotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotViewHolder holder, int position) {
        holder.bind(mMediaFiles.get(position).getUrl());
    }

    @Override
    public int getItemCount() {
        return mMediaFiles == null ? 0 : mMediaFiles.size();
    }
}
