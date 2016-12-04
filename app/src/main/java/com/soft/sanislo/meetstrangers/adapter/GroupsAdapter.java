package com.soft.sanislo.meetstrangers.adapter;

import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.soft.sanislo.meetstrangers.model.Group;
import com.soft.sanislo.meetstrangers.viewholders.GroupViewHolder;

/**
 * Created by root on 04.12.16.
 */

public class GroupsAdapter extends FirebaseRecyclerAdapter<Group, GroupViewHolder> {
    private OnClickListener mOnClickListener;

    public GroupsAdapter(Class<Group> modelClass, int modelLayout, Class<GroupViewHolder> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    public GroupsAdapter(Class<Group> modelClass, int modelLayout, Class<GroupViewHolder> viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(GroupViewHolder viewHolder, Group group, int position) {
        viewHolder.populate(group, position, mOnClickListener);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(View view, Group group, int position);
    }
}
