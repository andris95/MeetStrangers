package com.soft.sanislo.meetstrangers.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.adapter.ChatHeaderAdapter;
import com.soft.sanislo.meetstrangers.model.ChatHeader;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.DateUtils;
import com.soft.sanislo.meetstrangers.utilities.Utils;

/**
 * Created by root on 04.10.16.
 */
public class ChatHeaderViewHolder extends RecyclerView.ViewHolder {
    private Context mContext;
    private ChatHeader mChatHeader;
    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnLoading(R.drawable.placeholder)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();

    View mRootView;
    TextView tvSenderName;
    TextView tvLastMessage;
    TextView tvLastMessageDate;
    ImageView ivChatPartner;

    private ChatHeaderAdapter.OnClickListener mOnClickListener;
    private String mChatPartnerKey;

    public ChatHeaderViewHolder(View itemView) {
        super(itemView);
        tvSenderName = (TextView) itemView.findViewById(R.id.tv_sender_name);
        tvLastMessage = (TextView) itemView.findViewById(R.id.tv_last_message);
        tvLastMessageDate = (TextView) itemView.findViewById(R.id.tv_last_message_date);
        ivChatPartner = (ImageView) itemView.findViewById(R.id.iv_chat_header_avatar);
        mRootView = itemView;
    }

    public void populate(Context context,
                         ChatHeader chatHeader,
                         String chatPartnerKey,
                         final int position,
                         ChatHeaderAdapter.OnClickListener onClickListener) {
        mChatHeader = chatHeader;
        mContext = context;
        mChatPartnerKey = chatPartnerKey;
        mOnClickListener = onClickListener;
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(view, position, mChatHeader, mChatPartnerKey);
                }
            }
        });

        setSenderName();
        setLastMessage();
        setLastMessageDate();
        setHeaderAvatar();
    }

    private void setSenderName() {
        Utils.getDatabase().getReference().child(Constants.F_USERS)
                .child(mChatHeader.getAuthorUID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        tvSenderName.setText(user.getFullName());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void setLastMessageDate() {
        String messageDateDisplay = DateUtils.getDateDisplay(mChatHeader.getTimestamp());
        tvLastMessageDate.setText(messageDateDisplay);
    }

    private void setLastMessage() {
        if (!TextUtils.isEmpty(mChatHeader.getLastMessage())) {
            tvLastMessage.setText(mChatHeader.getLastMessage());
        }
    }

    private void setHeaderAvatar() {
        Utils.getDatabase().getReference().child(Constants.F_USERS)
                .child(mChatHeader.getChatPartnerUID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        imageLoader.displayImage(user.getAvatarURL(), ivChatPartner, displayImageOptions);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
