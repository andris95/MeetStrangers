package com.soft.sanislo.meetstrangers.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.adapter.ChatHeaderAdapter;
import com.soft.sanislo.meetstrangers.adapter.ChatMessageAdapter;
import com.soft.sanislo.meetstrangers.model.ChatHeader;
import com.soft.sanislo.meetstrangers.model.ChatMessage;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.view.ChatHeaderViewHolder;
import com.soft.sanislo.meetstrangers.view.ChatMessageViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 04.10.16.
 */
public class ChatHeaderActivity extends BaseActivity {

    @BindView(R.id.rv_chat_headers)
    RecyclerView rvChatHeaders;

    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference chatHeaderRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User mAuthenticatedUser;
    private String mAuthenticatedUserUID;

    private ChatHeaderAdapter mChatHeaderAdapter;

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
    private static final String TAG = ChatHeaderActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_header);
        ButterKnife.bind(this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            mAuthenticatedUserUID = firebaseUser.getUid();
        }
        Log.d(TAG, "onCreate: firebaseUser " + (firebaseUser == null));
        chatHeaderRef = database.child(Constants.F_CHATS_HEADERS).child(mAuthenticatedUserUID);

        mChatHeaderAdapter = new ChatHeaderAdapter(ChatHeader.class,
                R.layout.item_chat_header,
                ChatHeaderViewHolder.class,
                chatHeaderRef,
                this);
        mChatHeaderAdapter.setOnClickListener(new ChatHeaderAdapter.OnClickListener() {
            @Override
            public void onClick(View view, int position, ChatHeader chatHeader, String chatPartnerKey) {
                Log.d(TAG, "onClick: " + chatHeader);
                Intent intent = new Intent(ChatHeaderActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.KEY_CHAT_PARTER_UID, chatPartnerKey);
                startActivity(intent);
            }
        });
        rvChatHeaders.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        database.child(Constants.F_USERS).child(mAuthenticatedUserUID)
                .addValueEventListener(mAuthenticatedUserListener);
        rvChatHeaders.setAdapter(mChatHeaderAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        database.child(Constants.F_USERS).child(mAuthenticatedUserUID)
                .removeEventListener(mAuthenticatedUserListener);
        mChatHeaderAdapter.cleanup();
    }
}
