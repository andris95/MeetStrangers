package com.soft.sanislo.meetstrangers.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.ChatMessage;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.view.ChatMessageViewHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by root on 02.10.16.
 */
public class ChatMessageAdapter extends FirebaseRecyclerAdapter<ChatMessage, ChatMessageViewHolder> {
    private Context mContext;
    private User mAuthenticatedUser;
    private User mChatPartnerUser;

    private String mAuthenticatedUID;
    private String mChatPartnerUID;

    private static final int VIEW_TYPE_YOURSELF = 333;
    private static final int VIEW_TYPE_STRANGER = 444;
    private LayoutInflater mLayoutInflater;

    public ChatMessageAdapter(Class<ChatMessage> modelClass, int modelLayout, Class<ChatMessageViewHolder> viewHolderClass, Query ref, Context context) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public ChatMessageAdapter(Class<ChatMessage> modelClass, int modelLayout, Class<ChatMessageViewHolder> viewHolderClass, DatabaseReference ref, Context context) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    protected void populateViewHolder(ChatMessageViewHolder viewHolder, ChatMessage model, int position) {
        if (mAuthenticatedUID.equals(getItem(position).getAuthorUID())) {
            viewHolder.populate(mContext, mAuthenticatedUser, model, position);
        } else {
            viewHolder.populate(mContext, mChatPartnerUser, model, position);
        }
    }

    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_YOURSELF:
                view = mLayoutInflater.inflate(R.layout.item_chat_message, parent, false);
                break;
            case VIEW_TYPE_STRANGER:
                view = mLayoutInflater.inflate(R.layout.item_chat_message_stranger, parent, false);
                break;
            default:
                view = mLayoutInflater.inflate(R.layout.item_chat_message, parent, false);
        }
        return new ChatMessageViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (mAuthenticatedUID.equals(getItem(position).getAuthorUID())) {
            return VIEW_TYPE_YOURSELF;
        } else {
            return VIEW_TYPE_STRANGER;
        }
    }

    public void setAuthenticatedUser(User authenticatedUser) {
        mAuthenticatedUser = authenticatedUser;
        notifyDataSetChanged();
    }

    public void setChatPartnerUser(User chatPartnerUser) {
        mChatPartnerUser = chatPartnerUser;
        notifyDataSetChanged();
    }

    public void setChatPartnerUID(String chatPartnerUID) {
        mChatPartnerUID = chatPartnerUID;
    }

    public void setAuthenticatedUID(String authenticatedUID) {
        mAuthenticatedUID = authenticatedUID;
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
}
