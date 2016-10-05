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
    private static final int VIEW_TYPE_YOURSELF = 333;
    private static final int VIEW_TYPE_STRANGER = 444;
    private Class<ChatMessageViewHolder> mChatMessageViewHolderClass;

    public ChatMessageAdapter(Class<ChatMessage> modelClass, int modelLayout, Class<ChatMessageViewHolder> viewHolderClass, Query ref, Context context) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        mContext = context;
        mChatMessageViewHolderClass = viewHolderClass;
    }

    public ChatMessageAdapter(Class<ChatMessage> modelClass, int modelLayout, Class<ChatMessageViewHolder> viewHolderClass, DatabaseReference ref, Context context) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        mContext = context;
        mChatMessageViewHolderClass = viewHolderClass;
    }

    @Override
    protected void populateViewHolder(ChatMessageViewHolder viewHolder, ChatMessage model, int position) {
        if (mAuthenticatedUser.getId().equals(getItem(position).getAuthorUID())) {
            viewHolder.populate(mContext, mAuthenticatedUser, model, position);
        } else {
            viewHolder.populate(mContext, mChatPartnerUser, model, position);
        }
    }

    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewGroup view = null;
        switch (viewType) {
            case VIEW_TYPE_YOURSELF:
                view = (ViewGroup) layoutInflater.inflate(R.layout.item_chat_message, parent, false);
                break;
            case VIEW_TYPE_STRANGER:
                view = (ViewGroup) layoutInflater.inflate(R.layout.item_chat_message_stranger, parent, false);
                break;
        }

        try {
            Constructor<ChatMessageViewHolder> constructor = mChatMessageViewHolderClass.getConstructor(View.class);
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

    @Override
    public int getItemViewType(int position) {
        if (mAuthenticatedUser.getId().equals(getItem(position).getAuthorUID())) {
            return VIEW_TYPE_YOURSELF;
        } else {
            return VIEW_TYPE_STRANGER;
        }
    }

    public User getAuthenticatedUser() {
        return mAuthenticatedUser;
    }

    public void setAuthenticatedUser(User authenticatedUser) {
        mAuthenticatedUser = authenticatedUser;
        notifyDataSetChanged();
    }

    public User getChatPartnerUser() {
        return mChatPartnerUser;
    }

    public void setChatPartnerUser(User chatPartnerUser) {
        mChatPartnerUser = chatPartnerUser;
        notifyDataSetChanged();
    }
}
