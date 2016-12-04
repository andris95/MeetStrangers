package com.soft.sanislo.meetstrangers.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.soft.sanislo.meetstrangers.model.ChatHeader;
import com.soft.sanislo.meetstrangers.viewholders.ChatHeaderViewHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by root on 04.10.16.
 */
public class ChatHeaderAdapter extends FirebaseRecyclerAdapter<ChatHeader, ChatHeaderViewHolder> {
    private static final String TAG = ChatHeaderAdapter.class.getSimpleName();
    private Context mContext;
    private ChatHeaderAdapter.OnClickListener mOnClickListener;
    private Class<ChatHeaderViewHolder> mChatHeaderViewHolderClass;
    private LayoutInflater mLayoutInflater;

    public ChatHeaderAdapter(Class<ChatHeader> modelClass, int modelLayout, Class<ChatHeaderViewHolder> viewHolderClass, Query ref, Context context) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        mContext = context;
        mChatHeaderViewHolderClass = viewHolderClass;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public ChatHeaderAdapter(Class<ChatHeader> modelClass, int modelLayout, Class<ChatHeaderViewHolder> viewHolderClass, DatabaseReference ref, Context context) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        mContext = context;
        mChatHeaderViewHolderClass = viewHolderClass;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    protected void populateViewHolder(ChatHeaderViewHolder viewHolder, ChatHeader model, int position) {
        String chatPartnerKey = getRef(position).getKey();
        Log.d(TAG, "populateViewHolder: " + chatPartnerKey);
        viewHolder.populate(mContext, model, chatPartnerKey, position, mOnClickListener);
    }

    @Override
    public ChatHeaderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup view = (ViewGroup) mLayoutInflater.inflate(viewType, parent, false);
        try {
            Constructor<ChatHeaderViewHolder> constructor = mChatHeaderViewHolderClass.getConstructor(View.class);
            return constructor.newInstance(view);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(View view, int position, ChatHeader chatHeader, String chatPartnerKey);
    }
}
