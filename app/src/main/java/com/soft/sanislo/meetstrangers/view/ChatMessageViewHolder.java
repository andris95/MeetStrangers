package com.soft.sanislo.meetstrangers.view;

/**
 * Created by root on 02.10.16.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.adapter.PostAdapter;
import com.soft.sanislo.meetstrangers.model.ChatMessage;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;

/**
 * Created by root on 24.09.16.
 */
public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = ChatMessageViewHolder.class.getSimpleName();

    private View mRootView;
    private ChatMessage mChatMessage;
    private Context mContext;

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

    public ChatMessageViewHolder(View itemView) {
        super(itemView);
        mRootView = itemView;
        //ButterKnife.bind(mContext, mRootView);

        ivMessageAuthorAvatar = (ImageView) mRootView.findViewById(R.id.iv_chat_author_avatar);
        tvChatMessage = (TextView) mRootView.findViewById(R.id.tv_chat_message);
        tvMessageDate = (TextView) mRootView.findViewById(R.id.tv_chat_message_date);
    }

    public void setMessage() {
        if (!TextUtils.isEmpty(mChatMessage.getMessage())) {
            tvChatMessage.setText(mChatMessage.getMessage());
        }
    }

    private void setMessageDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
        Date messageDate = new Date(mChatMessage.getTimestamp());
        String messageDateDisplay = dateFormat.format(messageDate);
        tvMessageDate.setText(messageDateDisplay);
    }

    public void setMessageAuthorAvatar() {
        if (mUser != null) imageLoader.displayImage(mUser.getAvatarURL(), ivMessageAuthorAvatar, displayImageOptions);
    }

    public void populate(Context context,
                         User user,
                         ChatMessage chatMessage,
                         final int position) {
        mChatMessage = chatMessage;
        mContext = context;
        mUser = user;

        setMessage();
        setMessageAuthorAvatar();
        setMessageDate();
    }
}
