package com.soft.sanislo.meetstrangers.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.ChatMessage;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by root on 02.10.16.
 */
public class ChatActivity extends BaseActivity {
    @BindView(R.id.rv_chat)
    RecyclerView rvChat;
    @BindView(R.id.iv_send_message)
    ImageView ivSendMessage;
    @BindView(R.id.edt_chat_message)
    EditText edtChatMessage;

    public static final String KEY_CHAT_PARTER_UID = "KEY_CHAT_PARTER_UID";

    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User mAuthenticatedUser;
    private String mAuthenticatedUserUID;

    private User mChatPartnerUser;
    private String mChatPartnerUID;

    /** ValueEventListener for current authenticated user*/
    private ValueEventListener mAuthenticatedUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mAuthenticatedUser = dataSnapshot.getValue(User.class);
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
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mAuthenticatedUserUID = firebaseUser.getUid();
        mChatPartnerUID = getIntent().getStringExtra(KEY_CHAT_PARTER_UID);
    }

    @OnClick(R.id.iv_send_message)
    public void onClickSendMessage() {
        Date sendDate = new Date();
        String chatMessageKey = sendDate.getTime() + "";
        String message = edtChatMessage.getText().toString();

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatMessageKey(chatMessageKey);
        chatMessage.setMessage(message);
        chatMessage.setTimestamp(sendDate.getTime());
        chatMessage.setAuthorAvatarURL(mAuthenticatedUser.getAvatarURL());
        chatMessage.setAuthorUID(mAuthenticatedUserUID);
        chatMessage.setRecipientUID(mChatPartnerUID);

        database.child(Constants.F_CHATS)
                .child(mAuthenticatedUserUID)
                .child(chatMessageKey)
                .setValue(chatMessage);
        database.child(Constants.F_CHATS)
                .child(mChatPartnerUID)
                .child(chatMessageKey)
                .setValue(chatMessageKey);
    }

    @Override
    protected void onResume() {
        super.onResume();
        database.child(Constants.F_USERS)
                .child(mAuthenticatedUserUID).addValueEventListener(mAuthenticatedUserListener);
        database.child(Constants.F_USERS)
                .child(mChatPartnerUID).addValueEventListener(mChatPartnerUserListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        database.child(Constants.F_USERS)
                .child(mAuthenticatedUserUID).removeEventListener(mAuthenticatedUserListener);
        database.child(Constants.F_USERS)
                .child(mChatPartnerUID).addValueEventListener(mChatPartnerUserListener);
    }
}
