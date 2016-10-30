package com.soft.sanislo.meetstrangers.view;

/**
 * Created by root on 02.10.16.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.ChatMessage;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.DateUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 24.09.16.
 */
public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = ChatMessageViewHolder.class.getSimpleName();

    private View mRootView;
    private ChatMessage mChatMessage;

    @BindView(R.id.iv_chat_author_avatar)
    ImageView ivMessageAuthorAvatar;

    @BindView(R.id.tv_chat_message)
    TextView tvChatMessage;

    @BindView(R.id.tv_chat_message_date)
    TextView tvMessageDate;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnLoading(R.drawable.placeholder)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private User mUser;
    private int mPosition;

    public ChatMessageViewHolder(View itemView) {
        super(itemView);
        mRootView = itemView;
        ButterKnife.bind(this, mRootView);
    }

    private void setMessage() {
        if (!TextUtils.isEmpty(mChatMessage.getMessage())) {
            tvChatMessage.setText(mChatMessage.getMessage());
        }
    }

    private void setMessageDate() {
        String messageDateDisplay = DateUtils.getDateDisplay(mChatMessage.getTimestamp());
        tvMessageDate.setText(messageDateDisplay);
    }

    private void setMessageAuthorAvatar() {
        if (mUser != null) imageLoader.displayImage(mUser.getAvatarURL(), ivMessageAuthorAvatar, displayImageOptions);
    }

    public void populate(Context context,
                         User user,
                         ChatMessage chatMessage,
                         final int position) {
        mChatMessage = chatMessage;
        mPosition = position;
        mUser = user;

        setMessage();
        setMessageDate();
        if (mUser != null) {
            setMessageAuthorAvatar();
        }
    }
}
