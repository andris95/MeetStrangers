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

import java.util.Date;
import java.util.HashMap;

/**
 * Created by root on 28.10.16.
 */

public class ChatPresenterImpl implements ChatPresenter {
    private ChatView mChatView;
    private DatabaseReference mDatabaseRef;
    private User mAuthenticatedUser;
    private String mAuthenticatedUserUID;
    private User mChatPartnerUser;
    private String mChatPartnerUID;

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
        mChatView = context;
        mDatabaseRef = Utils.getDatabase().getReference();
        mAuthenticatedUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mChatPartnerUID = chatPartnerUID;
    }

    @Override
    public void pushChatMessage(String message) {
        ChatMessage chatMessage = createChatMessage(message);
        ChatHeader chatHeader = getChatHeader(chatMessage);

        HashMap<String, Object> chatMessageMap = getChatMessageMap(chatMessage);
        HashMap<String, Object> chatHeaderMap = getChatHeaderMap(chatHeader);

        HashMap<String, Object> chatDataUpdateMap = new HashMap<>();
        chatDataUpdateMap.putAll(chatMessageMap);
        chatDataUpdateMap.putAll(chatHeaderMap);

        mDatabaseRef.updateChildren(chatDataUpdateMap);
    }

    private ChatMessage createChatMessage(String message) {
        Date sendDate = new Date();
        String chatMessageKey = sendDate.getTime() + "";
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setKey(chatMessageKey);
        chatMessage.setMessage(message);
        chatMessage.setTimestamp(sendDate.getTime());
        chatMessage.setAuthorUID(mAuthenticatedUserUID);
        chatMessage.setAuthorAvatarURL(mAuthenticatedUser.getAvatarURL());
        return chatMessage;
    }

    @Override
    public void onResume() {
        mDatabaseRef.child(Constants.F_USERS)
                .child(mAuthenticatedUserUID)
                .addValueEventListener(mAuthenticatedUserListener);
        mDatabaseRef.child(Constants.F_USERS)
                .child(mChatPartnerUID)
                .addValueEventListener(mChatPartnerUserListener);
    }

    @Override
    public void onPause() {
        mDatabaseRef.child(Constants.F_USERS)
                .child(mAuthenticatedUserUID)
                .removeEventListener(mAuthenticatedUserListener);
        mDatabaseRef.child(Constants.F_USERS)
                .child(mChatPartnerUID)
                .addValueEventListener(mChatPartnerUserListener);
    }

    @Override
    public void onDestroy() {

    }

    private ChatHeader getChatHeader(ChatMessage chatMessage) {
        ChatHeader chatHeader = new ChatHeader(chatMessage.getMessage(),
                chatMessage.getAuthorUID(),
                mChatPartnerUID,
                chatMessage.getTimestamp());
        return chatHeader;
    }

    private HashMap<String, Object> getChatMessageMap(ChatMessage chatMessage) {
        HashMap<String, Object> chatMessageUpdateMap = new HashMap<>();
        chatMessageUpdateMap.put("/chats/" + mAuthenticatedUserUID + "/" +
                mChatPartnerUID + "/" +
                chatMessage.getKey(), chatMessage);
        chatMessageUpdateMap.put("/chats/" +
                mChatPartnerUID + "/" +
                mAuthenticatedUserUID + "/" +
                chatMessage.getKey(), chatMessage);
        return chatMessageUpdateMap;
    }

    private HashMap<String, Object> getChatHeaderMap(ChatHeader chatHeader) {
        HashMap<String, Object> headerUpdateMap = new HashMap<>();
        headerUpdateMap.put(
                "/chats_headers/" +
                mAuthenticatedUserUID +
                "/" + mChatPartnerUID, chatHeader);
        headerUpdateMap.put(
                "/chats_headers/" +
                mChatPartnerUID +
                "/" + mAuthenticatedUserUID, chatHeader);
        return headerUpdateMap;
    }
}
