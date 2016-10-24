package com.soft.sanislo.meetstrangers.activity;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.adapter.ChatMessageAdapter;
import com.soft.sanislo.meetstrangers.model.ChatMessage;
import com.soft.sanislo.meetstrangers.model.ChatHeader;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.ChatMessageViewHolder;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by root on 02.10.16.
 */
public class ChatActivity extends BaseActivity {
    @BindView(R.id.iv_chat_partner_avatar)
    ImageView ivChatPartnerAvatar;
    @BindView(R.id.iv_chat_partner_bg)
    ImageView ivChatPartnerBG;
    @BindView(R.id.rv_chat)
    RecyclerView rvChat;
    @BindView(R.id.iv_send_message)
    ImageView ivSendMessage;
    @BindView(R.id.edt_chat_message)
    EditText edtChatMessage;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    public static final String KEY_CHAT_PARTNER_UID = "KEY_CHAT_PARTNER_UID";

    private DatabaseReference mDatabaseRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User mAuthenticatedUser;
    private String mAuthenticatedUserUID;

    private User mChatPartnerUser;
    private String mChatPartnerUID;

    private ChatMessageAdapter mChatMessageAdapter;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnLoading(R.drawable.placeholder)
            .showImageForEmptyUri(R.drawable.mountains)
            .showImageOnFail(R.drawable.mountains)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();

    /** ValueEventListener for current authenticated user*/
    private ValueEventListener mAuthenticatedUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mAuthenticatedUser = dataSnapshot.getValue(User.class);
            mChatMessageAdapter.setAuthenticatedUser(mAuthenticatedUser);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    /** ValueEventListener for chat partner user*/
    private ValueEventListener mChatPartnerUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mChatPartnerUser = dataSnapshot.getValue(User.class);
            mChatMessageAdapter.setChatPartnerUser(mChatPartnerUser);
            imageLoader.displayImage(mChatPartnerUser.getAvatarURL(), ivChatPartnerAvatar, displayImageOptions, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    supportStartPostponedEnterTransition();
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    supportStartPostponedEnterTransition();
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    supportStartPostponedEnterTransition();
                }
            });
            imageLoader.displayImage(mChatPartnerUser.getAvatarBlurURL(), ivChatPartnerBG, displayImageOptions);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private static final String TAG = ChatActivity.class.getSimpleName();
    private ChatMessage mChatMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        }
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mDatabaseRef = Utils.getDatabase().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mAuthenticatedUserUID = firebaseUser.getUid();
        mChatPartnerUID = getIntent().getStringExtra(KEY_CHAT_PARTNER_UID);
        Log.d(TAG, "onCreate: " + mChatPartnerUID);
        initChat();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
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
    }

    @OnClick(R.id.iv_send_message)
    public void onClickSendMessage() {
        mChatMessage = createChatMessage();
        pushChatMessage(mAuthenticatedUserUID, mChatPartnerUID, null);
        pushChatMessage(mChatPartnerUID, mAuthenticatedUserUID, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateChatHeader();
            }
        });
    }

    private void pushChatMessage(String firstUserUID, String secondUserUID,
                                 OnCompleteListener<Void> onCompleteListener) {
        mDatabaseRef.child(Constants.F_CHATS)
                .child(firstUserUID)
                .child(secondUserUID)
                .child(mChatMessage.getKey())
                .setValue(mChatMessage).addOnCompleteListener(onCompleteListener);
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
        chatMessage.setRecipientUID(mChatPartnerUID);
        return chatMessage;
    }

    private void updateChatHeader() {
        ChatHeader chatHeader = new ChatHeader(
                edtChatMessage.getText().toString(),
                mAuthenticatedUserUID,
                mAuthenticatedUser.getFullName(),
                mChatPartnerUser.getAvatarURL(),
                new Date().getTime());

        setChatHeader(chatHeader, mAuthenticatedUserUID, mChatPartnerUID);
        setChatHeader(chatHeader, mChatPartnerUID, mAuthenticatedUserUID);
        edtChatMessage.setText("");
    }

    private void setChatHeader(ChatHeader chatHeader, String firstUserUID, String secondUserUID) {
        mDatabaseRef.child(Constants.F_CHATS_HEADERS)
                .child(firstUserUID)
                .child(secondUserUID)
                .setValue(chatHeader);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDatabaseRef.child(Constants.F_USERS)
                .child(mAuthenticatedUserUID).addValueEventListener(mAuthenticatedUserListener);
        mDatabaseRef.child(Constants.F_USERS)
                .child(mChatPartnerUID).addValueEventListener(mChatPartnerUserListener);
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
        mDatabaseRef.child(Constants.F_USERS)
                .child(mAuthenticatedUserUID).removeEventListener(mAuthenticatedUserListener);
        mDatabaseRef.child(Constants.F_USERS)
                .child(mChatPartnerUID).addValueEventListener(mChatPartnerUserListener);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        supportFinishAfterTransition();
    }
}
