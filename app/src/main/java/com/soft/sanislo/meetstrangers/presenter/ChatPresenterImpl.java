package com.soft.sanislo.meetstrangers.presenter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.soft.sanislo.meetstrangers.activity.ChatActivity;
import com.soft.sanislo.meetstrangers.model.ChatHeader;
import com.soft.sanislo.meetstrangers.model.ChatMessage;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.ChatView;

/**
 * Created by root on 28.10.16.
 */

public class ChatPresenterImpl implements ChatPresenter {
    private ChatActivity mContext;
    private ChatView mChatView;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User mAuthenticatedUser;
    private String mAuthenticatedUserUID;
    private User mChatPartnerUser;
    private String mChatPartnerUID;

    private ChatMessage mChatMessage;

    /** ValueEventListener for current authenticated user*/
    private ValueEventListener mAuthenticatedUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mAuthenticatedUser = dataSnapshot.getValue(User.class);
            mChatView.onAuthenticatedUserChange(mAuthenticatedUser);
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
            mChatView.onChatPartnerChange(mChatPartnerUser);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public ChatPresenterImpl(ChatActivity context, String chatPartnerUID) {
        mContext = context;
        mChatView = context;
        mDatabaseRef = Utils.getDatabase().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mAuthenticatedUserUID = firebaseUser.getUid();
        mChatPartnerUID = chatPartnerUID;
    }

    @Override
    public void pushChatMessage(ChatMessage chatMessage) {
        mChatMessage = chatMessage;
        mDatabaseRef.child(Constants.F_CHATS)
                .child(chatMessage.getAuthorUID())
                .child(chatMessage.getRecepientUID())
                .child(mChatMessage.getKey())
                .setValue(mChatMessage);
        mDatabaseRef.child(Constants.F_CHATS)
                .child(chatMessage.getRecepientUID())
                .child(chatMessage.getAuthorUID())
                .child(mChatMessage.getKey())
                .setValue(mChatMessage);
        updateChatHeader();
    }

    @Override
    public void onResume() {
        mDatabaseRef.child(Constants.F_USERS)
                .child(mAuthenticatedUserUID).addValueEventListener(mAuthenticatedUserListener);
        mDatabaseRef.child(Constants.F_USERS)
                .child(mChatPartnerUID).addValueEventListener(mChatPartnerUserListener);
    }

    @Override
    public void onPause() {
        mDatabaseRef.child(Constants.F_USERS)
                .child(mAuthenticatedUserUID).removeEventListener(mAuthenticatedUserListener);
        mDatabaseRef.child(Constants.F_USERS)
                .child(mChatPartnerUID).addValueEventListener(mChatPartnerUserListener);
    }

    @Override
    public void onDestroy() {

    }

    private void updateChatHeader() {
        ChatHeader chatHeader = new ChatHeader(mChatMessage.getMessage(),
                mChatMessage.getAuthorUID(),
                mChatPartnerUID,
                mChatMessage.getTimestamp());
        setChatHeader(chatHeader, mAuthenticatedUserUID, mChatPartnerUID);
        chatHeader.setChatPartnerUID(mAuthenticatedUserUID);
        setChatHeader(chatHeader, mChatPartnerUID, mAuthenticatedUserUID);
    }

    private void setChatHeader(ChatHeader chatHeader, String firstUserUID, String secondUserUID) {
        mDatabaseRef.child(Constants.F_CHATS_HEADERS)
                .child(firstUserUID)
                .child(secondUserUID)
                .setValue(chatHeader);
    }
}
