package com.soft.sanislo.meetstrangers.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.adapter.ChatMessageAdapter;
import com.soft.sanislo.meetstrangers.model.ChatMessage;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.presenter.ChatPresenter;
import com.soft.sanislo.meetstrangers.presenter.ChatPresenterImpl;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.ChatMessageViewHolder;
import com.soft.sanislo.meetstrangers.view.ChatView;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by root on 02.10.16.
 */
public class ChatActivity extends BaseActivity implements ChatView {
    @BindView(R.id.iv_chat_partner_avatar)
    ImageView ivChatPartnerAvatar;

    @BindView(R.id.rv_chat)
    RecyclerView rvChat;

    @BindView(R.id.iv_send_message)
    Button ivSendMessage;

    @BindView(R.id.edt_chat_message)
    EditText edtChatMessage;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.tv_chat_partner)
    TextView tvChatPartner;

    public static final String KEY_CHAT_PARTNER_UID = "KEY_CHAT_PARTNER_UID";

    private ChatPresenter mChatPresenter;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private String mAuthenticatedUserUID;
    private String mChatPartnerUID;

    private ChatMessageAdapter mChatMessageAdapter;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnFail(R.drawable.placeholder)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();

    private static final String TAG = ChatActivity.class.getSimpleName();
    private ChatMessage mChatMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportPostponeEnterTransition();
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mDatabaseRef = Utils.getDatabase().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mAuthenticatedUserUID = firebaseUser.getUid();
        mChatPartnerUID = getIntent().getStringExtra(KEY_CHAT_PARTNER_UID);

        mChatPresenter = new ChatPresenterImpl(this, mChatPartnerUID);
        initChat();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            supportFinishAfterTransition(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    private void initChat() {
        mChatMessageAdapter = new ChatMessageAdapter(ChatMessage.class,
                R.layout.item_chat_message,
                ChatMessageViewHolder.class,
                mDatabaseRef.child(Constants.F_CHATS)
                        .child(mAuthenticatedUserUID).child(mChatPartnerUID),
                this);
        mChatMessageAdapter.setAuthenticatedUID(mAuthenticatedUserUID);
        mChatMessageAdapter.setChatPartnerUID(mChatPartnerUID);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(mChatMessageAdapter);
        rvChat.scrollToPosition(mChatMessageAdapter.getItemCount() - 1);
    }

    @OnClick(R.id.iv_send_message)
    public void onClickSendMessage() {
        mChatMessage = createChatMessage();
        mChatPresenter.pushChatMessage(mChatMessage);
        edtChatMessage.setText("");
    }

    private ChatMessage createChatMessage() {
        Date sendDate = new Date();
        String chatMessageKey = sendDate.getTime() + "";
        String message = edtChatMessage.getText().toString();

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setKey(chatMessageKey);
        chatMessage.setMessage(message);
        chatMessage.setTimestamp(sendDate.getTime());
        chatMessage.setAuthorUID(mAuthenticatedUserUID);
        chatMessage.setRecepientUID(mChatPartnerUID);
        return chatMessage;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mChatPresenter.onResume();
        if (rvChat.getAdapter() == null && mChatMessageAdapter != null) {
            rvChat.setAdapter(mChatMessageAdapter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        mChatMessageAdapter.cleanup();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mChatPresenter.onPause();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        supportFinishAfterTransition();
    }

    @Override
    public void sendChatMessage() {

    }

    @Override
    public void onAuthenticatedUserChange(User user) {
        mChatMessageAdapter.setAuthenticatedUser(user);
    }

    @Override
    public void onChatPartnerChange(User user) {
        mChatMessageAdapter.setChatPartnerUser(user);
        tvChatPartner.setText(user.getFullName());
        imageLoader.displayImage(user.getAvatarURL(), ivChatPartnerAvatar, displayImageOptions, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                supportStartPostponedEnterTransition();
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
                //transition
            }
        });
    }
}
