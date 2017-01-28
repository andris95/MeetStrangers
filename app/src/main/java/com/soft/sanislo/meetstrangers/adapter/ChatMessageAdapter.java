package com.soft.sanislo.meetstrangers.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.ChatMessage;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.viewholders.ChatMessageViewHolder;

/**
 * Created by root on 02.10.16.
 */
public class ChatMessageAdapter extends FirebaseRecyclerAdapter<ChatMessage, ChatMessageViewHolder> {
    private Context mContext;
    private String mAuthenticatedUID;

    private static final int VIEW_TYPE_YOURSELF = 333;
    private static final int VIEW_TYPE_PARTNER = 444;
    private LayoutInflater mLayoutInflater;

    public ChatMessageAdapter(Class<ChatMessage> modelClass, int modelLayout, Class<ChatMessageViewHolder> viewHolderClass, Query ref, Context context) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        mAuthenticatedUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    protected void populateViewHolder(ChatMessageViewHolder viewHolder, ChatMessage chatMessage, int position) {
        viewHolder.populate(chatMessage);
    }

    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_YOURSELF:
                view = mLayoutInflater.inflate(R.layout.item_chat_message, parent, false);
                break;
            case VIEW_TYPE_PARTNER:
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
            return VIEW_TYPE_PARTNER;
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
}
