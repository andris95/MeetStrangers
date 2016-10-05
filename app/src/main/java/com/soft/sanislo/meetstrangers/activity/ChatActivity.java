package com.soft.sanislo.meetstrangers.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.adapter.ChatMessageAdapter;
import com.soft.sanislo.meetstrangers.model.ChatMessage;
import com.soft.sanislo.meetstrangers.model.ChatHeader;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.view.ChatMessageViewHolder;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
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

    private ChatMessageAdapter mChatMessageAdapter;

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
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private static final String TAG = ChatActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mAuthenticatedUserUID = firebaseUser.getUid();
        mChatPartnerUID = getIntent().getStringExtra(KEY_CHAT_PARTER_UID);

        mChatMessageAdapter = new ChatMessageAdapter(ChatMessage.class,
                R.layout.item_chat_message,
                ChatMessageViewHolder.class,
                database.child(Constants.F_CHATS)
                        .child(mAuthenticatedUserUID).child(mChatPartnerUID),
                this);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(mChatMessageAdapter);
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
        chatMessage.setAuthorUID(mAuthenticatedUserUID);
        chatMessage.setRecipientUID(mChatPartnerUID);

        database.child(Constants.F_CHATS)
                .child(mAuthenticatedUserUID)
                .child(mChatPartnerUID)
                .child(chatMessageKey)
                .setValue(chatMessage)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "onComplete: complete");
                updateChatHeader();
                edtChatMessage.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Log.d(TAG, "onFailure: ");
            }
        });
        database.child(Constants.F_CHATS)
                .child(mChatPartnerUID)
                .child(mAuthenticatedUserUID)
                .child(chatMessageKey)
                .setValue(chatMessage);
    }

    private void updateChatHeader() {
        ChatHeader chatHeader = new ChatHeader(edtChatMessage.getText().toString(),
                mAuthenticatedUserUID,
                mAuthenticatedUser.getFullName(),
                mChatPartnerUser.getAvatarURL(),
                new Date().getTime());

        database.child(Constants.F_CHATS_HEADERS)
                .child(mAuthenticatedUserUID)
                .child(mChatPartnerUID)
                .setValue(chatHeader);
        database.child(Constants.F_CHATS_HEADERS)
                .child(mChatPartnerUID)
                .child(mAuthenticatedUserUID)
                .setValue(chatHeader);
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
